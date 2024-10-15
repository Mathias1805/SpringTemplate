package Template.Api.Service.Implement;

import Template.Api.DTO.AuthResponse;
import Template.Api.DTO.LoginRequest;
import Template.Api.Repository.SesionRepository;
import Template.Api.Repository.UsuarioFrontendRepository;
import Template.Api.Service.UsuarioFrontendService;
import Template.Api.Auth.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UsuarioFrontendServiceImpl  implements UsuarioFrontendService {
    @Autowired
    UsuarioFrontendRepository usuarioFrontendRepository;
    @Autowired
    SesionRepository sesionRepository;
    @Autowired
    AuthService authService;
    @Override
    public AuthResponse Login(LoginRequest loginRequest) {
        return authService.login(loginRequest);
    }



}
