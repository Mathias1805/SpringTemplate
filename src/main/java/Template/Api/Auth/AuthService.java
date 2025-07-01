package Template.Api.Auth;


import Template.Api.DTO.AuthResponse;
import Template.Api.DTO.LoginRequest;
import Template.Api.DTO.RegisterRequest;
import Template.Api.DTO.UserFrontendDTO;
import Template.Api.JWT.JWTservice;
import Template.Api.Repository.DispositivoRepository;
import Template.Api.Repository.SesionRepository;
import Template.Api.Repository.UsuarioFrontendRepository;
import Template.Api.User.RolRepository;
import Template.Api.User.UsuarioBackend;
import Template.Api.User.UsuarioBackendRepository;
import Template.Api.UserFrontend.Dispositivo;
import Template.Api.UserFrontend.Sesion;
import Template.Api.UserFrontend.Usuariofrontend;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {
    @Autowired
    private final UsuarioBackendRepository usuarioBackendRepository;
    @Autowired
    private final UsuarioFrontendRepository usuarioFrontendRepository;
    @Autowired
    private final JWTservice jwTservice;
    @Autowired
    private final PasswordEncoder passwordEncoder;
    @Autowired
    private final AuthenticationManager authenticationManager;
    @Autowired
    private final RolRepository rolRepository;
    @Autowired
    private final DispositivoRepository dispositivoRepository;
    @Autowired
    private final SesionRepository sesionRepository;

    public AuthResponse login(LoginRequest request) {

        return switch (request.getTipousuario()){
            case 1 -> procesarLoginClient(request);
            case 2 -> procesarLoginFrontendUser(request);
            default -> throw new IllegalStateException("Unexpected value: " + request.getTipousuario());
        };
    }

    private AuthResponse procesarLoginClient(LoginRequest request) {

        if(dispositivoRepository.findDispositivoByIpAndNombre(request.getIp(), request.getNombreDispositivo()).isEmpty()){
            Dispositivo dispositivo = new Dispositivo();
            dispositivo.setNombre(request.getNombreDispositivo());
            dispositivo.setIp(request.getIp());
            dispositivo.setVersionapp(request.getVersionapp());
            dispositivo.setSistemaoperativo(request.getSistemaOperativo());
            dispositivo.setUbicacion(request.getUbicacion());
            dispositivo.setEstado(1);
            Dispositivo dispositivoNuevo =dispositivoRepository.save(dispositivo);
        }
        List<Sesion> sesions = sesionRepository.findSesionsByIddispositivo_Ip(request.getIp());

        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getUsuario(), request.getContrasenia()));
        UserDetails user = usuarioBackendRepository.findUsuarioBackendByUsuario(request.getUsuario()).orElseThrow();
        String token = jwTservice.getToken(user);

        if (sesions.isEmpty()){
            return AuthResponse.builder().token(token).build();
        }
        return AuthResponse.builder().token(token).sesiones(sesions).build();
    }

    private AuthResponse procesarLoginFrontendUser(LoginRequest request) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getUsuario(), request.getContrasenia()));
        UserDetails user = usuarioBackendRepository.findUsuarioBackendByUsuario(request.getUsuario()).orElseThrow();
        Optional<Usuariofrontend> usuarioFrontend = usuarioFrontendRepository.findByUsuarioAndContrasenia(request.getUsuariofrontend(),jwTservice.Cod(request.getContraseniafrontend()));

        if(usuarioFrontend.isEmpty()){
            throw new InsufficientAuthenticationException("Usuario o Contraseña Incorrecta");
        }
        if (usuarioFrontend.get().getActivo() == 0){
            throw new RuntimeException("Usuario no activo");
        }
        UserFrontendDTO UserDto = new UserFrontendDTO();
        UserDto.setUsuario(usuarioFrontend.get().getUsuario());
        UserDto.setContrasenia(usuarioFrontend.get().getContrasenia());
        String token = jwTservice.getToken(user,UserDto);
        return AuthResponse.builder().token(token).build();
    }

    public AuthResponse register(RegisterRequest request) {
        UsuarioBackend user = UsuarioBackend.builder()
                .usuario(request.getUsuario())
                .contraseia(passwordEncoder.encode(request.getContrasenia()))
                .rol(rolRepository.findByRolLike(request.getRol()).orElseThrow())
                .build();
        usuarioBackendRepository.save(user);
        return AuthResponse.builder().token(jwTservice.getToken(user)).build();
    }
}
