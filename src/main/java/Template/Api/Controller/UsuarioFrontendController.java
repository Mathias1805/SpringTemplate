package Template.Api.Controller;

import Template.Api.Auth.TipoUsuario;
import Template.Api.DTO.DTO_LoginRequest;
import Template.Api.DTO.DTO_PasswordReset;
import Template.Api.DTO.DTO_RegisterRequest;
import Template.Api.DTO.DTO_UserCode;
import Template.Api.Service.UsuarioFrontendService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RequestMapping("/Usuario")
@RestController
@Tag(name = "Usuarios Frontend", description = "Registro, autenticación y gestión de perfil de usuarios finales")
public class UsuarioFrontendController {

    private final UsuarioFrontendService usuarioService;

    @Operation(summary = "Login usuario final", description = "Autentica un usuario final. Requiere credenciales backend (usuario/contrasenia) y credenciales frontend (usuariofrontend/contraseniafrontend).")
    @PostMapping("/Login")
    ResponseEntity<?> Login(@Valid @RequestBody DTO_LoginRequest loginRequest) {
        // Este endpoint es exclusivo para usuarios finales (tipo 2).
        // Para administradores/API usar POST /Auth/Login.
        loginRequest.setTipousuario(TipoUsuario.USUARIO_FRONTEND.getValor());
        return ResponseEntity.ok(usuarioService.Login(loginRequest));
    }

    @Operation(summary = "Registrar usuario frontend", description = "Crea un usuario frontend e inicia el flujo de verificación por email.")
    @PostMapping("/Register")
    ResponseEntity<?> Register(@Valid @RequestBody DTO_RegisterRequest registerRequest) {
        return ResponseEntity.ok(usuarioService.Register(registerRequest));
    }

    @Operation(summary = "Validar cuenta", description = "Activa la cuenta usando el código enviado por email.")
    @PostMapping("/Validate")
    ResponseEntity<?> Validate(@Valid @RequestBody DTO_UserCode userCode) {
        return ResponseEntity.ok(usuarioService.ValidateUser(userCode));
    }

    @Operation(summary = "Ver perfil", description = "Retorna los datos del usuario frontend.")
    @GetMapping("/Profile")
    ResponseEntity<?> getProfile(@RequestParam String usuariofrontend) {
        return ResponseEntity.ok(usuarioService.getProfile(usuariofrontend));
    }

    @Operation(summary = "Actualizar perfil", description = "Actualiza nombre, apellidos, dirección y teléfono.")
    @PutMapping("/Profile")
    ResponseEntity<?> updateProfile(
            @RequestParam String usuariofrontend,
            @RequestBody DTO_RegisterRequest updateRequest) {
        return ResponseEntity.ok(usuarioService.updateProfile(usuariofrontend, updateRequest));
    }

    @Operation(summary = "Solicitar reset de contraseña", description = "Envía un código de recuperación al email del usuario.")
    @PostMapping("/ResetPassword")
    ResponseEntity<?> solicitarReset(@RequestParam String email) {
        return ResponseEntity.ok(usuarioService.solicitarResetPassword(email));
    }

    @Operation(summary = "Confirmar reset de contraseña", description = "Cambia la contraseña usando el código de recuperación.")
    @PostMapping("/ConfirmReset")
    ResponseEntity<?> confirmarReset(@Valid @RequestBody DTO_PasswordReset resetRequest) {
        return ResponseEntity.ok(usuarioService.confirmarResetPassword(resetRequest));
    }
}
