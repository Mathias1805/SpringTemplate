package Template.Api.Service;

import Template.Api.DTO.DTO_AuthResponse;
import Template.Api.DTO.DTO_LoginRequest;
import Template.Api.DTO.DTO_PasswordReset;
import Template.Api.DTO.DTO_RegisterRequest;
import Template.Api.DTO.DTO_UserCode;
import Template.Api.UserFrontend.Usuariofrontend;

import java.util.Map;

public interface UsuarioFrontendService {
    DTO_AuthResponse Login(DTO_LoginRequest loginRequest);
    Map<String, Object> Register(DTO_RegisterRequest registerRequest);
    Map<String, Object> ValidateUser(DTO_UserCode userCode);
    Usuariofrontend getProfile(String usuario);
    Usuariofrontend updateProfile(String usuario, DTO_RegisterRequest updateRequest);
    Map<String, Object> solicitarResetPassword(String email);
    Map<String, Object> confirmarResetPassword(DTO_PasswordReset resetRequest);
}
