package Template.Api.Service;

import Template.Api.DTO.AuthResponse;
import Template.Api.DTO.LoginRequest;



public interface UsuarioFrontendService {
    AuthResponse Login(LoginRequest loginRequest);
}
