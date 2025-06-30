package Template.Api.Service;

import Template.Api.DTO.AuthResponse;
import Template.Api.DTO.LoginRequest;
import Template.Api.DTO.RegisterRequest;
import Template.Api.DTO.UserCodeDTO;

import java.util.Map;


public interface UsuarioFrontendService {
    AuthResponse Login(LoginRequest loginRequest);
    Map<String,Object> Register(RegisterRequest registerRequest);
    Map<String,Object>  ValidateUser(UserCodeDTO userCode);
}
