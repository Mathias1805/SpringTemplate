package Template.Api.Service;

import Template.Api.DTO.DTO_AuthResponse;
import Template.Api.DTO.DTO_LoginRequest;
import Template.Api.DTO.DTO_RegisterRequest;
import Template.Api.DTO.DTO_UserCode;

import java.util.Map;


public interface UsuarioFrontendService {
    DTO_AuthResponse Login(DTO_LoginRequest loginRequest);
    Map<String,Object> Register(DTO_RegisterRequest registerRequest);
    Map<String,Object>  ValidateUser(DTO_UserCode userCode);
}
