package Template.Api.Service.Implement;

import Template.Api.DTO.AuthResponse;
import Template.Api.DTO.LoginRequest;
import Template.Api.DTO.RegisterRequest;
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
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
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
    public AuthResponse Login(LoginRequest loginRequest) {
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
    public Map<String,Object> Register(RegisterRequest registerRequest) {

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
        newFrontendUser.setCod_ver(verificationCode);
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
    }




