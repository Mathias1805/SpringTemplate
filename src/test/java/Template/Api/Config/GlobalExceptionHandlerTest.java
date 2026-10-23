package Template.Api.Config;

import Template.Api.DTO.ErrorResponse;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("GlobalExceptionHandler — Tests unitarios")
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    // ─── Códigos HTTP ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("BadCredentialsException → 401")
    void handleBadCredentials_shouldReturn401() {
        ResponseEntity<ErrorResponse> response =
                handler.handleBadCredentials(new BadCredentialsException("invalid"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody().getStatus()).isEqualTo(401);
        assertThat(response.getBody().getError()).isEqualTo("Credenciales incorrectas");
    }

    @Test
    @DisplayName("InsufficientAuthenticationException → 401")
    void handleInsufficientAuth_shouldReturn401() {
        ResponseEntity<ErrorResponse> response =
                handler.handleInsufficientAuth(new InsufficientAuthenticationException("no auth"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody().getStatus()).isEqualTo(401);
    }

    @Test
    @DisplayName("UsernameNotFoundException → 404")
    void handleUserNotFound_shouldReturn404() {
        ResponseEntity<ErrorResponse> response =
                handler.handleUserNotFound(new UsernameNotFoundException("not found"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().getStatus()).isEqualTo(404);
        assertThat(response.getBody().getError()).isEqualTo("Usuario no encontrado");
    }

    @Test
    @DisplayName("IllegalStateException → 400")
    void handleIllegalState_shouldReturn400() {
        ResponseEntity<ErrorResponse> response =
                handler.handleIllegalState(new IllegalStateException("bad state"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getStatus()).isEqualTo(400);
    }

    @Test
    @DisplayName("RuntimeException → 400")
    void handleRuntime_shouldReturn400() {
        ResponseEntity<ErrorResponse> response =
                handler.handleRuntime(new RuntimeException("runtime error"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getStatus()).isEqualTo(400);
        assertThat(response.getBody().getError()).isEqualTo("Error de solicitud");
    }

    @Test
    @DisplayName("MalformedJwtException → 401")
    void handleMalformedJwt_shouldReturn401() {
        ResponseEntity<ErrorResponse> response =
                handler.handleMalformedJwt(new MalformedJwtException("bad jwt"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody().getError()).isEqualTo("Token inválido");
    }

    @Test
    @DisplayName("SignatureException → 401")
    void handleSignatureException_shouldReturn401() {
        ResponseEntity<ErrorResponse> response =
                handler.handleSignatureException(new SignatureException("bad sig"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody().getError()).isEqualTo("Token inválido");
    }

    @Test
    @DisplayName("Exception genérica → 500")
    void handleGeneric_shouldReturn500() {
        ResponseEntity<ErrorResponse> response =
                handler.handleGeneric(new Exception("server crashed"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().getStatus()).isEqualTo(500);
        assertThat(response.getBody().getError()).isEqualTo("Error interno del servidor");
    }

    // ─── Estructura del ErrorResponse ────────────────────────────────────────

    @Test
    @DisplayName("ErrorResponse contiene el mensaje de la excepción")
    void errorResponse_shouldContainExceptionMessage() {
        ResponseEntity<ErrorResponse> response =
                handler.handleRuntime(new RuntimeException("mensaje específico"));

        assertThat(response.getBody().getMessage()).isEqualTo("mensaje específico");
    }

    @Test
    @DisplayName("ErrorResponse contiene timestamp mayor a cero")
    void errorResponse_shouldContainPositiveTimestamp() {
        ResponseEntity<ErrorResponse> response =
                handler.handleRuntime(new RuntimeException("test"));

        assertThat(response.getBody().getTimestamp()).isGreaterThan(0L);
    }

    @Test
    @DisplayName("ErrorResponse contiene status consistente con el HTTP code")
    void errorResponse_statusShouldMatchHttpCode() {
        ResponseEntity<ErrorResponse> response404 =
                handler.handleUserNotFound(new UsernameNotFoundException("x"));
        ResponseEntity<ErrorResponse> response400 =
                handler.handleRuntime(new RuntimeException("x"));
        ResponseEntity<ErrorResponse> response500 =
                handler.handleGeneric(new Exception("x"));

        assertThat(response404.getBody().getStatus())
                .isEqualTo(response404.getStatusCode().value());
        assertThat(response400.getBody().getStatus())
                .isEqualTo(response400.getStatusCode().value());
        assertThat(response500.getBody().getStatus())
                .isEqualTo(response500.getStatusCode().value());
    }
}
