package Template.Api.Auth;


import Template.Api.DTO.AuthResponse;
import Template.Api.DTO.LoginRequest;
import Template.Api.DTO.RegisterRequest;
import Template.Api.JWT.JWTservice;
import Template.Api.Repository.DispositivoRepository;
import Template.Api.Repository.SesionRepository;
import Template.Api.User.RolRepository;
import Template.Api.User.UsuarioBackend;
import Template.Api.User.UsuarioBackendRepository;
import Template.Api.UserFrontend.Dispositivo;
import Template.Api.UserFrontend.Sesion;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {
    @Autowired
    private final UsuarioBackendRepository usuarioBackendRepository;
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
        return AuthResponse.builder().token(token).sesiones(sesions).build();
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
