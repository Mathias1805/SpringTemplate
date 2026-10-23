package Template.Api.Service.Implement;

import Template.Api.Auth.AuthService;
import Template.Api.DTO.*;
import Template.Api.JWT.JWTservice;
import Template.Api.Repository.UsuarioFrontendRepository;
import Template.Api.Service.EmailService;
import Template.Api.Service.UsuarioFrontendService;
import Template.Api.UserFrontend.Usuariofrontend;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.*;

@Service
@RequiredArgsConstructor
public class UsuarioFrontendServiceImpl implements UsuarioFrontendService {

    private final EmailService emailService;
    private final UsuarioFrontendRepository usuarioFrontendRepository;
    private final AuthService authService;
    private final JWTservice jwTservice;

    @Override
    public DTO_AuthResponse Login(DTO_LoginRequest loginRequest) {
        return authService.login(loginRequest);
    }

    private String generateRandomCode() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[9]; // 9 bytes * 8/6 ≈ 12 caracteres Base64
        random.nextBytes(bytes);
        String code = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        return code.substring(0, 12);
    }

    @Override
    @Transactional
    public Map<String, Object> Register(DTO_RegisterRequest registerRequest) {
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

        Usuariofrontend savedUser = usuarioFrontendRepository.save(newFrontendUser);

        emailService.sendSimpleEmailAsync(
                savedUser.getEmail(),
                "Código de Verificación",
                "Tu código de verificación es: " + verificationCode
        );

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Usuario registrado. Verifica tu email para activar tu cuenta.");
        response.put("userId", savedUser.getId());
        response.put("status", "pending_verification");
        return response;
    }

    @Override
    @Transactional
    public Map<String, Object> ValidateUser(DTO_UserCode userCode) {
        Optional<Usuariofrontend> usuario = usuarioFrontendRepository
                .findByUsuarioAndCodver(userCode.getUser(), userCode.getCodver());
        if (usuario.isEmpty()) {
            throw new RuntimeException("Usuario no encontrado o código de verificación incorrecto");
        }
        usuario.get().setActivo(1);
        usuario.get().setCodver(generateRandomCode());
        usuarioFrontendRepository.save(usuario.get());

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Usuario validado. Ya podés iniciar sesión.");
        response.put("status", "validated");
        return response;
    }

    @Override
    public Usuariofrontend getProfile(String usuario) {
        return usuarioFrontendRepository.findByUsuario(usuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + usuario));
    }

    @Override
    @Transactional
    public Usuariofrontend updateProfile(String usuario, DTO_RegisterRequest updateRequest) {
        Usuariofrontend existing = usuarioFrontendRepository.findByUsuario(usuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + usuario));

        if (updateRequest.getNombre() != null) existing.setNombre(updateRequest.getNombre());
        if (updateRequest.getPaterno() != null) existing.setPaterno(updateRequest.getPaterno());
        if (updateRequest.getMaterno() != null) existing.setMaterno(updateRequest.getMaterno());
        if (updateRequest.getDireccion() != null) existing.setDireccion(updateRequest.getDireccion());
        if (updateRequest.getTelefono() != null) existing.setTelefono(updateRequest.getTelefono());

        return usuarioFrontendRepository.save(existing);
    }

    @Override
    @Transactional
    public Map<String, Object> solicitarResetPassword(String email) {
        Usuariofrontend usuario = usuarioFrontendRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("No existe una cuenta registrada con ese email"));

        String resetCode = generateRandomCode();
        usuario.setCodver(resetCode);
        usuarioFrontendRepository.save(usuario);

        emailService.sendSimpleEmailAsync(
                email,
                "Código de Recuperación de Contraseña",
                "Tu código para restablecer la contraseña es: " + resetCode +
                        "\n\nEste código es de un solo uso."
        );

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Se envió un código de recuperación al email registrado");
        response.put("status", "reset_requested");
        return response;
    }

    @Override
    @Transactional
    public Map<String, Object> confirmarResetPassword(DTO_PasswordReset resetRequest) {
        Optional<Usuariofrontend> usuario = usuarioFrontendRepository
                .findByUsuarioAndCodver(resetRequest.getUser(), resetRequest.getCodver());

        if (usuario.isEmpty()) {
            throw new RuntimeException("Usuario o código de recuperación incorrecto");
        }

        usuario.get().setContrasenia(jwTservice.Cod(resetRequest.getNuevaContrasenia()));
        usuario.get().setCodver(generateRandomCode()); // invalidar código después del uso
        usuarioFrontendRepository.save(usuario.get());

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Contraseña actualizada correctamente");
        response.put("status", "password_reset");
        return response;
    }
}
