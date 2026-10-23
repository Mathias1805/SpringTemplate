package Template.Api.Service;

import Template.Api.Auth.AuthService;
import Template.Api.DTO.*;
import Template.Api.JWT.JWTservice;
import Template.Api.Repository.UsuarioFrontendRepository;
import Template.Api.Service.Implement.UsuarioFrontendServiceImpl;
import Template.Api.UserFrontend.Usuariofrontend;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UsuarioFrontendServiceImpl — Tests unitarios")
class UsuarioFrontendServiceImplTest {

    @InjectMocks
    private UsuarioFrontendServiceImpl service;

    @Mock private EmailService emailService;
    @Mock private UsuarioFrontendRepository usuarioFrontendRepository;
    @Mock private AuthService authService;
    @Mock private JWTservice jwTservice;

    // ─── Register ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Register: crea usuario inactivo y envía email de verificación")
    void register_shouldCreateInactiveUserAndSendEmail() {
        DTO_RegisterRequest req = buildRegisterRequest();

        Usuariofrontend saved = new Usuariofrontend();
        saved.setId(1);
        saved.setEmail("juan@test.com");
        saved.setActivo(0);

        when(jwTservice.Cod("miContrasenia")).thenReturn("hashedPass");
        when(usuarioFrontendRepository.save(any())).thenReturn(saved);

        Map<String, Object> result = service.Register(req);

        assertThat(result.get("status")).isEqualTo("pending_verification");
        assertThat(result.get("userId")).isEqualTo(1);
        verify(emailService).sendSimpleEmailAsync(eq("juan@test.com"), any(), any());
    }

    @Test
    @DisplayName("Register: el password se hashea antes de guardar")
    void register_shouldHashPasswordBeforeSaving() {
        DTO_RegisterRequest req = buildRegisterRequest();
        Usuariofrontend saved = new Usuariofrontend();
        saved.setId(1);
        saved.setEmail("juan@test.com");

        when(jwTservice.Cod("miContrasenia")).thenReturn("sha256hash");
        when(usuarioFrontendRepository.save(any())).thenReturn(saved);

        service.Register(req);

        ArgumentCaptor<Usuariofrontend> captor = ArgumentCaptor.forClass(Usuariofrontend.class);
        verify(usuarioFrontendRepository).save(captor.capture());
        assertThat(captor.getValue().getContrasenia()).isEqualTo("sha256hash");
        assertThat(captor.getValue().getActivo()).isEqualTo(0);
    }

    @Test
    @DisplayName("Register: el código de verificación tiene 12 caracteres")
    void register_shouldGenerateTwelveCharVerificationCode() {
        DTO_RegisterRequest req = buildRegisterRequest();
        Usuariofrontend saved = new Usuariofrontend();
        saved.setId(1);
        saved.setEmail("juan@test.com");

        when(jwTservice.Cod(any())).thenReturn("hash");
        when(usuarioFrontendRepository.save(any())).thenReturn(saved);

        service.Register(req);

        ArgumentCaptor<Usuariofrontend> captor = ArgumentCaptor.forClass(Usuariofrontend.class);
        verify(usuarioFrontendRepository).save(captor.capture());
        assertThat(captor.getValue().getCodver()).hasSize(12);
    }

    // ─── ValidateUser ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("ValidateUser: activa el usuario y regenera codver")
    void validateUser_shouldActivateUserAndRegenerateCodver() {
        Usuariofrontend user = new Usuariofrontend();
        user.setActivo(0);
        user.setCodver("ABC123DEF456");

        when(usuarioFrontendRepository.findByUsuarioAndCodver("user1", "ABC123DEF456"))
                .thenReturn(Optional.of(user));
        when(usuarioFrontendRepository.save(any())).thenReturn(user);

        Map<String, Object> result = service.ValidateUser(new DTO_UserCode("ABC123DEF456", "user1"));

        assertThat(user.getActivo()).isEqualTo(1);
        assertThat(user.getCodver()).isNotEqualTo("ABC123DEF456"); // regenerado
        assertThat(result.get("status")).isEqualTo("validated");
    }

    @Test
    @DisplayName("ValidateUser: lanza excepción si el código es incorrecto")
    void validateUser_shouldThrow_whenCodeIsWrong() {
        when(usuarioFrontendRepository.findByUsuarioAndCodver("user1", "wrong"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.ValidateUser(new DTO_UserCode("wrong", "user1")))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("código de verificación incorrecto");
    }

    // ─── getProfile ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("getProfile: retorna el usuario cuando existe")
    void getProfile_shouldReturnUser_whenFound() {
        Usuariofrontend user = new Usuariofrontend();
        user.setUsuario("testuser");
        when(usuarioFrontendRepository.findByUsuario("testuser")).thenReturn(Optional.of(user));

        Usuariofrontend result = service.getProfile("testuser");

        assertThat(result.getUsuario()).isEqualTo("testuser");
    }

    @Test
    @DisplayName("getProfile: lanza excepción si el usuario no existe")
    void getProfile_shouldThrow_whenUserNotFound() {
        when(usuarioFrontendRepository.findByUsuario("noexiste")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getProfile("noexiste"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Usuario no encontrado");
    }

    // ─── updateProfile ────────────────────────────────────────────────────────

    @Test
    @DisplayName("updateProfile: actualiza solo los campos no-nulos")
    void updateProfile_shouldUpdateOnlyNonNullFields() {
        Usuariofrontend existing = new Usuariofrontend();
        existing.setNombre("Nombre Viejo");
        existing.setPaterno("Paterno Viejo");
        existing.setDireccion("Dirección Vieja");
        existing.setTelefono(111111111);

        when(usuarioFrontendRepository.findByUsuario("user1")).thenReturn(Optional.of(existing));
        when(usuarioFrontendRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        DTO_RegisterRequest update = new DTO_RegisterRequest();
        update.setNombre("Nombre Nuevo");
        // paterno, direccion, telefono quedan null → no deben cambiar

        Usuariofrontend result = service.updateProfile("user1", update);

        assertThat(result.getNombre()).isEqualTo("Nombre Nuevo");
        assertThat(result.getPaterno()).isEqualTo("Paterno Viejo");
        assertThat(result.getDireccion()).isEqualTo("Dirección Vieja");
        assertThat(result.getTelefono()).isEqualTo(111111111);
    }

    @Test
    @DisplayName("updateProfile: lanza excepción si el usuario no existe")
    void updateProfile_shouldThrow_whenUserNotFound() {
        when(usuarioFrontendRepository.findByUsuario("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateProfile("ghost", new DTO_RegisterRequest()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Usuario no encontrado");
    }

    // ─── solicitarResetPassword ───────────────────────────────────────────────

    @Test
    @DisplayName("solicitarResetPassword: genera nuevo codver y envía email")
    void solicitarResetPassword_shouldSetCodeAndSendEmail() {
        Usuariofrontend user = new Usuariofrontend();
        user.setEmail("user@test.com");
        user.setCodver("oldCode");

        when(usuarioFrontendRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));
        when(usuarioFrontendRepository.save(any())).thenReturn(user);

        Map<String, Object> result = service.solicitarResetPassword("user@test.com");

        assertThat(result.get("status")).isEqualTo("reset_requested");
        assertThat(user.getCodver()).isNotEqualTo("oldCode"); // debe haber cambiado
        verify(emailService).sendSimpleEmailAsync(eq("user@test.com"), any(), any());
    }

    @Test
    @DisplayName("solicitarResetPassword: lanza excepción si el email no está registrado")
    void solicitarResetPassword_shouldThrow_whenEmailNotFound() {
        when(usuarioFrontendRepository.findByEmail("noexiste@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.solicitarResetPassword("noexiste@test.com"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("cuenta registrada");
    }

    // ─── confirmarResetPassword ───────────────────────────────────────────────

    @Test
    @DisplayName("confirmarResetPassword: actualiza la contraseña e invalida el código")
    void confirmarResetPassword_shouldUpdatePasswordAndInvalidateCode() {
        Usuariofrontend user = new Usuariofrontend();
        user.setContrasenia("oldHash");
        user.setCodver("resetCode123");

        when(usuarioFrontendRepository.findByUsuarioAndCodver("user1", "resetCode123"))
                .thenReturn(Optional.of(user));
        when(jwTservice.Cod("nuevaPass")).thenReturn("newHash");
        when(usuarioFrontendRepository.save(any())).thenReturn(user);

        DTO_PasswordReset resetReq = new DTO_PasswordReset("user1", "resetCode123", "nuevaPass");
        Map<String, Object> result = service.confirmarResetPassword(resetReq);

        assertThat(result.get("status")).isEqualTo("password_reset");
        assertThat(user.getContrasenia()).isEqualTo("newHash");
        assertThat(user.getCodver()).isNotEqualTo("resetCode123"); // invalidado
    }

    @Test
    @DisplayName("confirmarResetPassword: lanza excepción si el código es incorrecto")
    void confirmarResetPassword_shouldThrow_whenCodeIsWrong() {
        when(usuarioFrontendRepository.findByUsuarioAndCodver("user1", "badCode"))
                .thenReturn(Optional.empty());

        DTO_PasswordReset req = new DTO_PasswordReset("user1", "badCode", "newPass");

        assertThatThrownBy(() -> service.confirmarResetPassword(req))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("código de recuperación incorrecto");
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private DTO_RegisterRequest buildRegisterRequest() {
        DTO_RegisterRequest req = new DTO_RegisterRequest();
        req.setUsuario("juanito");
        req.setContrasenia("miContrasenia");
        req.setEmail("juan@test.com");
        req.setTelefono(123456789);
        req.setDni(12345678);
        req.setNombre("Juan");
        req.setPaterno("García");
        req.setMaterno("Pérez");
        return req;
    }
}
