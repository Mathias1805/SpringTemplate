package Template.Api.Auth;

import Template.Api.DTO.*;
import Template.Api.Exception.UserInactiveException;
import Template.Api.JWT.JWTservice;
import Template.Api.Repository.DispositivoRepository;
import Template.Api.Repository.UsuarioFrontendRepository;
import Template.Api.Service.SesionService;
import Template.Api.User.Rol;
import Template.Api.User.RolRepository;
import Template.Api.User.UsuarioBackend;
import Template.Api.User.UsuarioBackendRepository;
import Template.Api.UserFrontend.Dispositivo;
import Template.Api.UserFrontend.Sesion;
import Template.Api.UserFrontend.Usuariofrontend;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService — Tests unitarios")
class AuthServiceTest {

    @InjectMocks
    private AuthService authService;

    @Mock private UsuarioBackendRepository usuarioBackendRepository;
    @Mock private UsuarioFrontendRepository usuarioFrontendRepository;
    @Mock private JWTservice jwTservice;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private RolRepository rolRepository;
    @Mock private DispositivoRepository dispositivoRepository;
    @Mock private SesionService sesionService;

    // ─── login routing ────────────────────────────────────────────────────────

    @Test
    @DisplayName("login: lanza IllegalStateException para tipousuario desconocido")
    void login_shouldThrow_whenTipoUsuarioInvalid() {
        DTO_LoginRequest req = new DTO_LoginRequest();
        req.setTipousuario(99);

        assertThatThrownBy(() -> authService.login(req))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("99");
    }

    // ─── login tipousuario=1 (cliente) ────────────────────────────────────────

    @Test
    @DisplayName("login tipo 1: retorna token cuando el dispositivo ya existe")
    void login_tipo1_shouldReturnToken_whenDeviceExists() {
        DTO_LoginRequest req = buildClientLoginRequest();
        UsuarioBackend backendUser = buildBackendUser("adminUser");

        Dispositivo dispositivo = new Dispositivo();
        dispositivo.setIp("127.0.0.1");

        when(dispositivoRepository.findDispositivoByIpAndNombre("127.0.0.1", "TestPC"))
                .thenReturn(Optional.of(dispositivo));
        when(sesionService.obtenerSesionesPorIp("127.0.0.1")).thenReturn(List.of());
        when(authenticationManager.authenticate(any())).thenReturn(null);
        when(usuarioBackendRepository.findUsuarioBackendByUsuario("adminUser"))
                .thenReturn(Optional.of(backendUser));
        when(jwTservice.getToken(backendUser)).thenReturn("jwt-cliente");

        DTO_AuthResponse result = authService.login(req);

        assertThat(result.getToken()).isEqualTo("jwt-cliente");
    }

    @Test
    @DisplayName("login tipo 1: registra nuevo dispositivo cuando no existe")
    void login_tipo1_shouldRegisterNewDevice_whenNotExists() {
        DTO_LoginRequest req = buildClientLoginRequest();
        UsuarioBackend backendUser = buildBackendUser("adminUser");
        Dispositivo savedDevice = new Dispositivo();
        savedDevice.setIp("127.0.0.1");

        when(dispositivoRepository.findDispositivoByIpAndNombre("127.0.0.1", "TestPC"))
                .thenReturn(Optional.empty());
        when(dispositivoRepository.save(any(Dispositivo.class))).thenReturn(savedDevice);
        when(sesionService.obtenerSesionesPorIp("127.0.0.1")).thenReturn(List.of());
        when(authenticationManager.authenticate(any())).thenReturn(null);
        when(usuarioBackendRepository.findUsuarioBackendByUsuario("adminUser"))
                .thenReturn(Optional.of(backendUser));
        when(jwTservice.getToken(backendUser)).thenReturn("jwt-new-device");

        DTO_AuthResponse result = authService.login(req);

        assertThat(result.getToken()).isEqualTo("jwt-new-device");
        verify(dispositivoRepository).save(any(Dispositivo.class));
    }

    @Test
    @DisplayName("login tipo 1: incluye sesiones en la respuesta cuando existen")
    void login_tipo1_shouldIncludeSessions_whenPresent() {
        DTO_LoginRequest req = buildClientLoginRequest();
        UsuarioBackend backendUser = buildBackendUser("adminUser");
        Dispositivo dispositivo = new Dispositivo();
        dispositivo.setIp("127.0.0.1");

        Sesion sesion = new Sesion();
        when(dispositivoRepository.findDispositivoByIpAndNombre(any(), any()))
                .thenReturn(Optional.of(dispositivo));
        when(sesionService.obtenerSesionesPorIp("127.0.0.1")).thenReturn(List.of(sesion));
        when(authenticationManager.authenticate(any())).thenReturn(null);
        when(usuarioBackendRepository.findUsuarioBackendByUsuario("adminUser"))
                .thenReturn(Optional.of(backendUser));
        when(jwTservice.getToken(backendUser)).thenReturn("jwt");

        DTO_AuthResponse result = authService.login(req);

        assertThat(result.getSesiones()).hasSize(1);
    }

    @Test
    @DisplayName("login tipo 1: falla si las credenciales del backend son incorrectas")
    void login_tipo1_shouldFail_whenBadCredentials() {
        DTO_LoginRequest req = buildClientLoginRequest();
        Dispositivo dispositivo = new Dispositivo();
        dispositivo.setIp("127.0.0.1");

        when(dispositivoRepository.findDispositivoByIpAndNombre(any(), any()))
                .thenReturn(Optional.of(dispositivo));
        when(sesionService.obtenerSesionesPorIp(any())).thenReturn(List.of());
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Credenciales incorrectas"));

        assertThatThrownBy(() -> authService.login(req))
                .isInstanceOf(BadCredentialsException.class);
    }

    // ─── login tipousuario=2 (usuario frontend) ───────────────────────────────

    @Test
    @DisplayName("login tipo 2: retorna token y sesión al autenticar usuario frontend")
    void login_tipo2_shouldReturnTokenAndSession() {
        DTO_LoginRequest req = buildFrontendLoginRequest();
        UsuarioBackend backendUser = buildBackendUser("apiUser");

        Usuariofrontend frontendUser = new Usuariofrontend();
        frontendUser.setUsuario("frontUser");
        frontendUser.setContrasenia("hashedPass");
        frontendUser.setActivo(1);

        Dispositivo dispositivo = new Dispositivo();
        dispositivo.setIp("10.0.0.1");
        Sesion sesion = new Sesion();

        when(authenticationManager.authenticate(any())).thenReturn(null);
        when(usuarioBackendRepository.findUsuarioBackendByUsuario("apiUser"))
                .thenReturn(Optional.of(backendUser));
        when(jwTservice.Cod("passfront")).thenReturn("hashedPass");
        when(usuarioFrontendRepository.findByUsuarioAndContrasenia("frontUser", "hashedPass"))
                .thenReturn(Optional.of(frontendUser));
        when(dispositivoRepository.findDispositivoByIpAndNombre("10.0.0.1", "TestPC"))
                .thenReturn(Optional.of(dispositivo));
        when(sesionService.crearSesion(frontendUser, dispositivo, null)).thenReturn(sesion);
        when(jwTservice.getToken(eq(backendUser), any(DTO_UserFrontend.class))).thenReturn("jwt-frontend");

        DTO_AuthResponse result = authService.login(req);

        assertThat(result.getToken()).isEqualTo("jwt-frontend");
        assertThat(result.getSesiones()).hasSize(1);
    }

    @Test
    @DisplayName("login tipo 2: lanza excepción si el usuario frontend no existe")
    void login_tipo2_shouldThrow_whenFrontendUserNotFound() {
        DTO_LoginRequest req = buildFrontendLoginRequest();
        UsuarioBackend backendUser = buildBackendUser("apiUser");

        when(authenticationManager.authenticate(any())).thenReturn(null);
        when(usuarioBackendRepository.findUsuarioBackendByUsuario("apiUser"))
                .thenReturn(Optional.of(backendUser));
        when(jwTservice.Cod("passfront")).thenReturn("hashedPass");
        when(usuarioFrontendRepository.findByUsuarioAndContrasenia("frontUser", "hashedPass"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(req))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("login tipo 2: lanza excepción si el usuario frontend no está activo")
    void login_tipo2_shouldThrow_whenFrontendUserInactive() {
        DTO_LoginRequest req = buildFrontendLoginRequest();
        UsuarioBackend backendUser = buildBackendUser("apiUser");

        Usuariofrontend inactiveUser = new Usuariofrontend();
        inactiveUser.setActivo(0);

        when(authenticationManager.authenticate(any())).thenReturn(null);
        when(usuarioBackendRepository.findUsuarioBackendByUsuario("apiUser"))
                .thenReturn(Optional.of(backendUser));
        when(jwTservice.Cod("passfront")).thenReturn("hashedPass");
        when(usuarioFrontendRepository.findByUsuarioAndContrasenia("frontUser", "hashedPass"))
                .thenReturn(Optional.of(inactiveUser));

        assertThatThrownBy(() -> authService.login(req))
                .isInstanceOf(UserInactiveException.class);
    }

    // ─── register ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("register: crea el usuario backend y retorna un token")
    void register_shouldCreateUserAndReturnToken() {
        DTO_RegisterRequest req = new DTO_RegisterRequest();
        req.setUsuario("nuevoAdmin");
        req.setContrasenia("pass123");
        req.setRol("ADMIN");

        Rol rol = new Rol();
        rol.setRol("ADMIN");

        when(rolRepository.findByRolLike("ADMIN")).thenReturn(Optional.of(rol));
        when(passwordEncoder.encode("pass123")).thenReturn("bcryptHash");
        when(usuarioBackendRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(jwTservice.getToken(any(UsuarioBackend.class))).thenReturn("token-admin");

        DTO_AuthResponse result = authService.register(req);

        assertThat(result.getToken()).isEqualTo("token-admin");
        verify(usuarioBackendRepository).save(any(UsuarioBackend.class));
        verify(passwordEncoder).encode("pass123");
    }

    @Test
    @DisplayName("register: lanza excepción si el rol no existe")
    void register_shouldThrow_whenRolNotFound() {
        DTO_RegisterRequest req = new DTO_RegisterRequest();
        req.setUsuario("user");
        req.setContrasenia("pass");
        req.setRol("ROL_INEXISTENTE");

        when(rolRepository.findByRolLike("ROL_INEXISTENTE")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.register(req))
                .isInstanceOf(RuntimeException.class);
    }

    // ─── refreshToken ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("refreshToken: retorna nuevo token cuando el token es válido")
    void refreshToken_shouldReturnNewToken_whenTokenValid() {
        UsuarioBackend user = buildBackendUser("admin");

        when(jwTservice.getUsuarioFromToken("valid-token")).thenReturn("admin");
        when(usuarioBackendRepository.findUsuarioBackendByUsuario("admin"))
                .thenReturn(Optional.of(user));
        when(jwTservice.isTokenValid("valid-token", user)).thenReturn(true);
        when(jwTservice.refreshToken("valid-token", user)).thenReturn("new-token");

        String result = authService.refreshToken("valid-token");

        assertThat(result).isEqualTo("new-token");
    }

    @Test
    @DisplayName("refreshToken: lanza excepción cuando el token está vencido o es inválido")
    void refreshToken_shouldThrow_whenTokenInvalid() {
        UsuarioBackend user = buildBackendUser("admin");

        when(jwTservice.getUsuarioFromToken("bad-token")).thenReturn("admin");
        when(usuarioBackendRepository.findUsuarioBackendByUsuario("admin"))
                .thenReturn(Optional.of(user));
        when(jwTservice.isTokenValid("bad-token", user)).thenReturn(false);

        assertThatThrownBy(() -> authService.refreshToken("bad-token"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("inválido");
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private UsuarioBackend buildBackendUser(String username) {
        UsuarioBackend user = new UsuarioBackend();
        user.setUsuario(username);
        user.setContraseia("encodedPass");
        return user;
    }

    private DTO_LoginRequest buildClientLoginRequest() {
        DTO_LoginRequest req = new DTO_LoginRequest();
        req.setTipousuario(1);
        req.setUsuario("adminUser");
        req.setContrasenia("pass");
        req.setIp("127.0.0.1");
        req.setNombreDispositivo("TestPC");
        req.setVersionapp("1.0");
        req.setSistemaOperativo("Windows");
        req.setUbicacion("Buenos Aires");
        return req;
    }

    private DTO_LoginRequest buildFrontendLoginRequest() {
        DTO_LoginRequest req = new DTO_LoginRequest();
        req.setTipousuario(2);
        req.setUsuario("apiUser");
        req.setContrasenia("passbackend");
        req.setUsuariofrontend("frontUser");
        req.setContraseniafrontend("passfront");
        req.setIp("10.0.0.1");
        req.setNombreDispositivo("TestPC");
        req.setVersionapp("2.0");
        req.setSistemaOperativo("Android");
        req.setUbicacion("Córdoba");
        return req;
    }
}
