package Template.Api.Service.Implement;

import Template.Api.DTO.DTO_AuthResponse;
import Template.Api.DTO.DTO_LoginRequest;
import Template.Api.DTO.DTO_RegisterRequest;
import Template.Api.DTO.DTO_UserCode;
import Template.Api.JWT.JWTservice;
import Template.Api.Repository.UsuarioFrontendRepository;
import Template.Api.Service.EmailService;
import Template.Api.Service.UsuarioFrontendService;
import Template.Api.Auth.AuthService;
import Template.Api.UserFrontend.Usuariofrontend;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.security.SecureRandom;
import java.util.*;

@Service
@RequiredArgsConstructor
public class UsuarioFrontendServiceImpl  implements UsuarioFrontendService {
    @Autowired
    EmailService emailService;
    @Autowired
    UsuarioFrontendRepository usuarioFrontendRepository;
    @Autowired
    AuthService authService;
    @Autowired
    JWTservice jwTservice;

    @Override
    public DTO_AuthResponse Login(DTO_LoginRequest loginRequest) {
        return authService.login(loginRequest);
    }

    public String generateRandomCode() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[9]; // 9 bytes * 8/6 ≈ 12 caracteres Base64
        random.nextBytes(bytes);
        String code = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        return code.substring(0, 12);
    }

    @Override
    public Map<String,Object> Register(DTO_RegisterRequest registerRequest) {

        Usuariofrontend newFrontendUser = new Usuariofrontend();
        newFrontendUser.setUsuario(registerRequest.getUsuario());
        newFrontendUser.setContrasenia(jwTservice.Cod(registerRequest.getContrasenia()));
        newFrontendUser.setEmail(registerRequest.getEmail());
        newFrontendUser.setTelefono(registerRequest.getTelefono());
        newFrontendUser.setDni(registerRequest.getDni());
        newFrontendUser.setNombre(registerRequest.getNombre());
        newFrontendUser.setPaterno(registerRequest.getPaterno());
        newFrontendUser.setMaterno(registerRequest.getMaterno());
        newFrontendUser.setDireccion(registerRequest.getDireccion());
        String verificationCode = generateRandomCode();
        newFrontendUser.setCodver(verificationCode);
        newFrontendUser.setActivo(0);

        Usuariofrontend newUserFrontendSaved = usuarioFrontendRepository.save(newFrontendUser);

        // Enviar código de verificación, no la contraseña
        emailService.sendSimpleEmailAsync(
                newUserFrontendSaved.getEmail(),
                "Código De Verificación",
                "Tu código de verificación es: " + verificationCode
        );
        // Retornar respuesta útil
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Usuario registrado. Verifica tu email.");
        response.put("userId", newUserFrontendSaved.getId());
        response.put("status", "pending_verification");
        return response;
        }

    @Override
    public Map<String, Object> ValidateUser(DTO_UserCode userCode) {
        Optional<Usuariofrontend> Usuario = usuarioFrontendRepository.findByUsuarioAndCodver(userCode.getUser(), userCode.getCodver());
        if (Usuario.isEmpty()) {
            throw new RuntimeException("Usuario no encontrado o Código de verificacion incorrecto");
        }
        if(Objects.equals(Usuario.get().getCodver(), userCode.getCodver())){
            Usuario.get().setActivo(1);
            Usuario.get().setCodver(generateRandomCode());
            usuarioFrontendRepository.save(Usuario.get());
        }
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Usuario Validado. Puede Iniciar Sesión");
        response.put("status", "validated");

        return response;
    }


}




