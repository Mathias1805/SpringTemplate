package Template.Api.JWT;

import Template.Api.DTO.DTO_UserFrontend;
import Template.Api.Repository.UsuarioFrontendRepository;
import Template.Api.UserFrontend.Usuariofrontend;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("JWTservice — Tests unitarios")
class JWTserviceTest {

    // Clave base64 válida de 32 bytes para HS256 (original del proyecto)
    private static final String TEST_JWT_SECRET = "PdqFzPtrNwmMx1zI5FaZhZXyOpl3MlLTkfCWucP95Wk";
    private static final String TEST_SALT = "testSalt123";

    @InjectMocks
    private JWTservice jwtService;

    @Mock
    private UsuarioFrontendRepository usuarioFrontendRepository;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(jwtService, "jwtSecret", TEST_JWT_SECRET);
        ReflectionTestUtils.setField(jwtService, "passwordSalt", TEST_SALT);
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", 86400000L);
    }

    // ─── getToken(UserDetails) ───────────────────────────────────────────────

    @Test
    @DisplayName("getToken: genera token no vacío")
    void getToken_shouldReturnNonEmptyToken() {
        UserDetails user = mockUser("testuser");

        String token = jwtService.getToken(user);

        assertThat(token).isNotNull().isNotEmpty();
    }

    @Test
    @DisplayName("getToken: el subject del token es el username")
    void getToken_subjectShouldBeUsername() {
        UserDetails user = mockUser("adminUser");

        String token = jwtService.getToken(user);
        String extracted = jwtService.getUsuarioFromToken(token);

        assertThat(extracted).isEqualTo("adminUser");
    }

    // ─── getToken(UserDetails, DTO_UserFrontend) ─────────────────────────────

    @Test
    @DisplayName("getToken con DTO: incluye claims del usuario frontend")
    void getToken_withFrontendDTO_shouldEmbedFrontendClaims() {
        UserDetails backendUser = mockUser("backendUser");
        Usuariofrontend frontendUser = buildFrontendUser();

        when(usuarioFrontendRepository.findByUsuario("frontuser"))
                .thenReturn(Optional.of(frontendUser));

        DTO_UserFrontend dto = new DTO_UserFrontend("frontuser", "hashedpass");
        String token = jwtService.getToken(backendUser, dto);

        assertThat(token).isNotNull();
        // Verificar que los claims custom están presentes extrayéndolos
        String claimUserFrontend = jwtService.getClaim(token, c -> c.get("UserFrontend", String.class));
        String claimEmail = jwtService.getClaim(token, c -> c.get("Email", String.class));
        assertThat(claimUserFrontend).isEqualTo("frontuser");
        assertThat(claimEmail).isEqualTo("test@test.com");
    }

    @Test
    @DisplayName("getToken con DTO: lanza excepción si el usuario frontend no existe")
    void getToken_withFrontendDTO_shouldThrow_whenUserNotFound() {
        // No usamos mockUser() para evitar un stub innecesario: getUsername() nunca se llama
        // porque la excepción se lanza antes de llegar a getToken(claims, user)
        UserDetails backendUser = mock(UserDetails.class);
        when(usuarioFrontendRepository.findByUsuario("noexiste")).thenReturn(Optional.empty());

        DTO_UserFrontend dto = new DTO_UserFrontend("noexiste", "pass");

        assertThatThrownBy(() -> jwtService.getToken(backendUser, dto))
                .isInstanceOf(RuntimeException.class);
    }

    // ─── isTokenValid ────────────────────────────────────────────────────────

    @Test
    @DisplayName("isTokenValid: retorna true para token fresco del mismo usuario")
    void isTokenValid_shouldReturnTrue_forFreshToken() {
        UserDetails user = mockUser("user1");

        String token = jwtService.getToken(user);

        assertThat(jwtService.isTokenValid(token, user)).isTrue();
    }

    @Test
    @DisplayName("isTokenValid: retorna false cuando el username no coincide")
    void isTokenValid_shouldReturnFalse_whenUsernameMismatch() {
        UserDetails tokenOwner = mockUser("user1");
        UserDetails otherUser = mockUser("user2");

        String token = jwtService.getToken(tokenOwner);

        assertThat(jwtService.isTokenValid(token, otherUser)).isFalse();
    }

    // ─── isTokenExpired ──────────────────────────────────────────────────────

    @Test
    @DisplayName("isTokenExpired: token recién generado no está expirado")
    void isTokenExpired_shouldReturnFalse_forFreshToken() {
        UserDetails user = mockUser("user1");
        String token = jwtService.getToken(user);

        assertThat(jwtService.isTokenExpired(token)).isFalse();
    }

    @Test
    @DisplayName("isTokenExpired: parsear un token vencido lanza ExpiredJwtException")
    void isTokenExpired_shouldThrowExpiredJwtException_forExpiredToken() {
        // JJWT lanza ExpiredJwtException al parsear el token, antes de poder leer la fecha.
        // Eso es el comportamiento correcto: el GlobalExceptionHandler captura esta excepción.
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", -1000L);
        UserDetails user = mockUser("user1");
        String expiredToken = jwtService.getToken(user);

        assertThatThrownBy(() -> jwtService.isTokenExpired(expiredToken))
                .isInstanceOf(io.jsonwebtoken.ExpiredJwtException.class);
    }

    // ─── Cod (hash SHA-256) ──────────────────────────────────────────────────

    @Test
    @DisplayName("Cod: el hash tiene 64 caracteres hex (SHA-256)")
    void cod_shouldReturn64CharHexString() {
        String hash = jwtService.Cod("password123");

        assertThat(hash).hasSize(64).matches("[a-f0-9]+");
    }

    @Test
    @DisplayName("Cod: la misma contraseña produce el mismo hash (determinístico)")
    void cod_shouldBeDeterministic() {
        String hash1 = jwtService.Cod("mismaContrasenia");
        String hash2 = jwtService.Cod("mismaContrasenia");

        assertThat(hash1).isEqualTo(hash2);
    }

    @Test
    @DisplayName("Cod: contraseñas distintas producen hashes distintos")
    void cod_shouldProduceDifferentHashesForDifferentPasswords() {
        String hash1 = jwtService.Cod("pass1");
        String hash2 = jwtService.Cod("pass2");

        assertThat(hash1).isNotEqualTo(hash2);
    }

    @Test
    @DisplayName("Cod: el salt afecta al hash (mismo password, diferente salt → diferente hash)")
    void cod_saltShouldAffectHash() {
        String hashWithSalt1 = jwtService.Cod("password");

        ReflectionTestUtils.setField(jwtService, "passwordSalt", "otroSalt");
        String hashWithSalt2 = jwtService.Cod("password");

        assertThat(hashWithSalt1).isNotEqualTo(hashWithSalt2);
    }

    // ─── refreshToken ────────────────────────────────────────────────────────

    @Test
    @DisplayName("refreshToken: genera un token nuevo y válido con las mismas claims")
    void refreshToken_shouldReturnNewValidToken() {
        UserDetails user = mockUser("user1");
        String original = jwtService.getToken(user);

        String refreshed = jwtService.refreshToken(original, user);

        assertThat(refreshed).isNotNull().isNotEmpty();
        assertThat(jwtService.isTokenValid(refreshed, user)).isTrue();
        assertThat(jwtService.getUsuarioFromToken(refreshed)).isEqualTo("user1");
    }

    @Test
    @DisplayName("refreshToken: preserva los claims custom del token original")
    void refreshToken_shouldPreserveCustomClaims() {
        UserDetails backendUser = mockUser("backend");
        Usuariofrontend frontendUser = buildFrontendUser();
        when(usuarioFrontendRepository.findByUsuario("frontuser"))
                .thenReturn(Optional.of(frontendUser));

        DTO_UserFrontend dto = new DTO_UserFrontend("frontuser", "hash");
        String original = jwtService.getToken(backendUser, dto);

        String refreshed = jwtService.refreshToken(original, backendUser);

        String claimUserFrontend = jwtService.getClaim(refreshed, c -> c.get("UserFrontend", String.class));
        assertThat(claimUserFrontend).isEqualTo("frontuser");
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private UserDetails mockUser(String username) {
        UserDetails user = mock(UserDetails.class);
        when(user.getUsername()).thenReturn(username);
        return user;
    }

    private Usuariofrontend buildFrontendUser() {
        Usuariofrontend u = new Usuariofrontend();
        u.setId(1);
        u.setUsuario("frontuser");
        u.setEmail("test@test.com");
        u.setTelefono(12345678);
        u.setDni(87654321);
        u.setNombre("Juan");
        u.setPaterno("García");
        u.setMaterno("Pérez");
        return u;
    }
}
