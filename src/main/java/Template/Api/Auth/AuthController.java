package Template.Api.Auth;

import Template.Api.DTO.DTO_AuthResponse;
import Template.Api.DTO.DTO_LoginRequest;
import Template.Api.DTO.DTO_RegisterRequest;
import Template.Api.Service.SesionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/Auth")
@RequiredArgsConstructor
@Tag(name = "Autenticación Backend", description = "Endpoints para usuarios backend (admin/API). Para usuarios finales usar /Usuario/Login.")
public class AuthController {

    private final AuthService authService;
    private final SesionService sesionService;

    @Operation(summary = "Login administrador/API", description = "Autentica un usuario backend (admin, integraciones). No requiere usuario frontend.")
    @PostMapping("/Login")
    public ResponseEntity<DTO_AuthResponse> login(@Valid @RequestBody DTO_LoginRequest request) {
        // Este endpoint es exclusivo para usuarios backend (tipo 1).
        // Para usuarios finales usar POST /Usuario/Login.
        request.setTipousuario(TipoUsuario.CLIENTE.getValor());
        return ResponseEntity.ok(authService.login(request));
    }

    @Operation(summary = "Registrar usuario backend", description = "Crea un nuevo usuario backend. Requiere rol ADMIN.")
    @PostMapping("/Register")
    public ResponseEntity<DTO_AuthResponse> register(@Valid @RequestBody DTO_RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @Operation(summary = "Renovar token", description = "Emite un nuevo JWT a partir de un token vigente.")
    @PostMapping("/Refresh")
    public ResponseEntity<DTO_AuthResponse> refresh(HttpServletRequest request) {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String newToken = authService.refreshToken(authHeader.substring(7));
        return ResponseEntity.ok(DTO_AuthResponse.builder().token(newToken).build());
    }

    @Operation(summary = "Cerrar sesión", description = "Cierra la sesión del usuario frontend indicada por idSesion.")
    @PostMapping("/Logout")
    public ResponseEntity<Map<String, Object>> logout(@RequestParam Integer idSesion) {
        sesionService.cerrarSesion(idSesion);
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Sesión cerrada correctamente");
        response.put("status", "logged_out");
        return ResponseEntity.ok(response);
    }
}
