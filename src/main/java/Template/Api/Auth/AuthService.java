package Template.Api.Auth;

import Template.Api.DTO.DTO_AuthResponse;
import Template.Api.DTO.DTO_LoginRequest;
import Template.Api.DTO.DTO_RegisterRequest;
import Template.Api.DTO.DTO_UserFrontend;
import Template.Api.Exception.InvalidTokenException;
import Template.Api.Exception.UserInactiveException;
import Template.Api.JWT.JWTservice;
import Template.Api.Repository.DispositivoRepository;
import Template.Api.Repository.UsuarioFrontendRepository;
import Template.Api.Service.SesionService;
import Template.Api.User.RolRepository;
import Template.Api.User.UsuarioBackend;
import Template.Api.User.UsuarioBackendRepository;
import Template.Api.UserFrontend.Dispositivo;
import Template.Api.UserFrontend.Sesion;
import Template.Api.UserFrontend.Usuariofrontend;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UsuarioBackendRepository usuarioBackendRepository;
    private final UsuarioFrontendRepository usuarioFrontendRepository;
    private final JWTservice jwTservice;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final RolRepository rolRepository;
    private final DispositivoRepository dispositivoRepository;
    private final SesionService sesionService;

    // @Transactional en el método público para que Spring AOP pueda interceptarlo.
    // Anotarlo en los métodos privados no tiene efecto (Spring usa proxies).
    @Transactional
    public DTO_AuthResponse login(DTO_LoginRequest request) {
        return switch (TipoUsuario.fromValor(request.getTipousuario())) {
            case CLIENTE -> procesarLoginClient(request);
            case USUARIO_FRONTEND -> procesarLoginFrontendUser(request);
        };
    }

    private DTO_AuthResponse procesarLoginClient(DTO_LoginRequest request) {
        log.info("[AUTH] Intento login CLIENTE: usuario='{}', ip='{}'", request.getUsuario(), request.getIp());
        try {
            Dispositivo dispositivo = dispositivoRepository
                    .findDispositivoByIpAndNombre(request.getIp(), request.getNombreDispositivo())
                    .orElseGet(() -> {
                        Dispositivo d = new Dispositivo();
                        d.setNombre(request.getNombreDispositivo());
                        d.setIp(request.getIp());
                        d.setVersionapp(request.getVersionapp());
                        d.setSistemaoperativo(request.getSistemaOperativo());
                        d.setUbicacion(request.getUbicacion());
                        d.setEstado(1);
                        return dispositivoRepository.save(d);
                    });

            List<Sesion> sesions = sesionService.obtenerSesionesPorIp(dispositivo.getIp());

            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsuario(), request.getContrasenia()));
            UserDetails user = usuarioBackendRepository
                    .findUsuarioBackendByUsuario(request.getUsuario()).orElseThrow();
            String token = jwTservice.getToken(user);

            log.info("[AUTH] Login exitoso CLIENTE: usuario='{}'", request.getUsuario());
            return DTO_AuthResponse.builder()
                    .token(token)
                    .sesiones(sesions.isEmpty() ? null : sesions)
                    .build();
        } catch (Exception ex) {
            log.warn("[AUTH] Login fallido CLIENTE: usuario='{}', ip='{}', razón='{}'",
                    request.getUsuario(), request.getIp(), ex.getMessage());
            throw ex;
        }
    }

    private DTO_AuthResponse procesarLoginFrontendUser(DTO_LoginRequest request) {
        // Validación condicional: estos campos son obligatorios para tipousuario=2
        // pero opcionales para tipousuario=1, por eso no están en el DTO con @NotBlank.
        if (request.getUsuariofrontend() == null || request.getUsuariofrontend().isBlank()) {
            throw new IllegalStateException("El campo usuariofrontend es obligatorio para login de usuario frontend");
        }
        if (request.getContraseniafrontend() == null || request.getContraseniafrontend().isBlank()) {
            throw new IllegalStateException("El campo contraseniafrontend es obligatorio para login de usuario frontend");
        }

        log.info("[AUTH] Intento login FRONTEND_USER: backend='{}', frontend='{}', ip='{}'",
                request.getUsuario(), request.getUsuariofrontend(), request.getIp());
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsuario(), request.getContrasenia()));
            UserDetails user = usuarioBackendRepository
                    .findUsuarioBackendByUsuario(request.getUsuario()).orElseThrow();

            Optional<Usuariofrontend> usuarioFrontend = usuarioFrontendRepository
                    .findByUsuarioAndContrasenia(request.getUsuariofrontend(),
                            jwTservice.Cod(request.getContraseniafrontend()));

            if (usuarioFrontend.isEmpty()) {
                throw new InsufficientAuthenticationException("Usuario o contraseña incorrecta");
            }
            if (usuarioFrontend.get().getActivo() == 0) {
                throw new UserInactiveException(request.getUsuariofrontend());
            }

            Dispositivo dispositivo = dispositivoRepository
                    .findDispositivoByIpAndNombre(request.getIp(), request.getNombreDispositivo())
                    .orElseGet(() -> {
                        Dispositivo d = new Dispositivo();
                        d.setNombre(request.getNombreDispositivo());
                        d.setIp(request.getIp());
                        d.setVersionapp(request.getVersionapp());
                        d.setSistemaoperativo(request.getSistemaOperativo());
                        d.setUbicacion(request.getUbicacion());
                        d.setEstado(1);
                        return dispositivoRepository.save(d);
                    });

            Sesion sesion = sesionService.crearSesion(usuarioFrontend.get(), dispositivo, null);

            DTO_UserFrontend userDto = new DTO_UserFrontend();
            userDto.setUsuario(usuarioFrontend.get().getUsuario());
            userDto.setContrasenia(usuarioFrontend.get().getContrasenia());
            String token = jwTservice.getToken(user, userDto);

            log.info("[AUTH] Login exitoso FRONTEND_USER: backend='{}', frontend='{}'",
                    request.getUsuario(), request.getUsuariofrontend());
            return DTO_AuthResponse.builder()
                    .token(token)
                    .sesiones(List.of(sesion))
                    .build();
        } catch (Exception ex) {
            log.warn("[AUTH] Login fallido FRONTEND_USER: backend='{}', frontend='{}', razón='{}'",
                    request.getUsuario(), request.getUsuariofrontend(), ex.getMessage());
            throw ex;
        }
    }

    @Transactional
    public DTO_AuthResponse register(DTO_RegisterRequest request) {
        UsuarioBackend user = UsuarioBackend.builder()
                .usuario(request.getUsuario())
                .contraseia(passwordEncoder.encode(request.getContrasenia()))
                .rol(rolRepository.findByRolLike(request.getRol()).orElseThrow())
                .build();
        usuarioBackendRepository.save(user);
        log.info("[AUTH] Usuario backend registrado: '{}'", request.getUsuario());
        return DTO_AuthResponse.builder().token(jwTservice.getToken(user)).build();
    }

    public String refreshToken(String token) {
        String usuario = jwTservice.getUsuarioFromToken(token);
        UserDetails userDetails = usuarioBackendRepository
                .findUsuarioBackendByUsuario(usuario).orElseThrow();
        if (!jwTservice.isTokenValid(token, userDetails)) {
            throw new InvalidTokenException();
        }
        return jwTservice.refreshToken(token, userDetails);
    }
}
