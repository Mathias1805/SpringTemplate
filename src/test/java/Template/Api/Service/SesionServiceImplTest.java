package Template.Api.Service;

import Template.Api.Repository.SesionRepository;
import Template.Api.Service.Implement.SesionServiceImpl;
import Template.Api.UserFrontend.Dispositivo;
import Template.Api.UserFrontend.Sesion;
import Template.Api.UserFrontend.Usuariofrontend;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SesionServiceImpl — Tests unitarios")
class SesionServiceImplTest {

    @InjectMocks
    private SesionServiceImpl sesionService;

    @Mock
    private SesionRepository sesionRepository;

    // ─── crearSesion ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("crearSesion: guarda la sesión con estado=1")
    void crearSesion_shouldSaveWithEstadoActivo() {
        Usuariofrontend usuario = new Usuariofrontend();
        Dispositivo dispositivo = new Dispositivo();
        when(sesionRepository.save(any(Sesion.class))).thenAnswer(inv -> inv.getArgument(0));

        Sesion result = sesionService.crearSesion(usuario, dispositivo, null);

        assertThat(result.getEstado()).isEqualTo(1);
        verify(sesionRepository).save(any(Sesion.class));
    }

    @Test
    @DisplayName("crearSesion: usa idioma 'es' por defecto cuando se pasa null")
    void crearSesion_shouldDefaultIdiomaToEs_whenNull() {
        when(sesionRepository.save(any(Sesion.class))).thenAnswer(inv -> inv.getArgument(0));

        Sesion result = sesionService.crearSesion(new Usuariofrontend(), new Dispositivo(), null);

        assertThat(result.getIdioma()).isEqualTo("es");
    }

    @Test
    @DisplayName("crearSesion: usa el idioma proporcionado cuando no es null")
    void crearSesion_shouldUseProvidedIdioma() {
        when(sesionRepository.save(any(Sesion.class))).thenAnswer(inv -> inv.getArgument(0));

        Sesion result = sesionService.crearSesion(new Usuariofrontend(), new Dispositivo(), "en");

        assertThat(result.getIdioma()).isEqualTo("en");
    }

    @Test
    @DisplayName("crearSesion: asigna el usuario y dispositivo correctamente")
    void crearSesion_shouldAssignUserAndDevice() {
        Usuariofrontend usuario = new Usuariofrontend();
        Dispositivo dispositivo = new Dispositivo();
        when(sesionRepository.save(any(Sesion.class))).thenAnswer(inv -> inv.getArgument(0));

        sesionService.crearSesion(usuario, dispositivo, null);

        ArgumentCaptor<Sesion> captor = ArgumentCaptor.forClass(Sesion.class);
        verify(sesionRepository).save(captor.capture());
        assertThat(captor.getValue().getIdusuariofrontend()).isSameAs(usuario);
        assertThat(captor.getValue().getIddispositivo()).isSameAs(dispositivo);
    }

    // ─── obtenerSesionesPorUsuario ────────────────────────────────────────────

    @Test
    @DisplayName("obtenerSesionesPorUsuario: delega al repositorio con el id correcto")
    void obtenerSesionesPorUsuario_shouldDelegateToRepository() {
        List<Sesion> expected = List.of(new Sesion(), new Sesion());
        when(sesionRepository.findSesionsByIdusuariofrontend_Id(5)).thenReturn(expected);

        List<Sesion> result = sesionService.obtenerSesionesPorUsuario(5);

        assertThat(result).hasSize(2);
        verify(sesionRepository).findSesionsByIdusuariofrontend_Id(5);
    }

    @Test
    @DisplayName("obtenerSesionesPorUsuario: retorna lista vacía si no hay sesiones")
    void obtenerSesionesPorUsuario_shouldReturnEmptyList_whenNoSessions() {
        when(sesionRepository.findSesionsByIdusuariofrontend_Id(99)).thenReturn(List.of());

        List<Sesion> result = sesionService.obtenerSesionesPorUsuario(99);

        assertThat(result).isEmpty();
    }

    // ─── obtenerSesionesPorIp ─────────────────────────────────────────────────

    @Test
    @DisplayName("obtenerSesionesPorIp: delega al repositorio con la IP correcta")
    void obtenerSesionesPorIp_shouldDelegateToRepository() {
        List<Sesion> expected = List.of(new Sesion());
        when(sesionRepository.findSesionsByIddispositivo_Ip("192.168.1.1")).thenReturn(expected);

        List<Sesion> result = sesionService.obtenerSesionesPorIp("192.168.1.1");

        assertThat(result).hasSize(1);
        verify(sesionRepository).findSesionsByIddispositivo_Ip("192.168.1.1");
    }

    // ─── cerrarSesion ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("cerrarSesion: cambia estado a 0 y guarda")
    void cerrarSesion_shouldSetEstadoToZeroAndSave() {
        Sesion sesion = new Sesion();
        sesion.setEstado(1);
        when(sesionRepository.findById(1)).thenReturn(Optional.of(sesion));
        when(sesionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        sesionService.cerrarSesion(1);

        assertThat(sesion.getEstado()).isEqualTo(0);
        verify(sesionRepository).save(sesion);
    }

    @Test
    @DisplayName("cerrarSesion: no lanza excepción si la sesión no existe")
    void cerrarSesion_shouldDoNothing_whenSessionNotFound() {
        when(sesionRepository.findById(999)).thenReturn(Optional.empty());

        assertDoesNotThrow(() -> sesionService.cerrarSesion(999));
        verify(sesionRepository, never()).save(any());
    }

    // ─── cerrarTodasLasSesiones ───────────────────────────────────────────────

    @Test
    @DisplayName("cerrarTodasLasSesiones: cierra todas las sesiones activas del usuario")
    void cerrarTodasLasSesiones_shouldCloseAllUserSessions() {
        Sesion s1 = new Sesion(); s1.setEstado(1);
        Sesion s2 = new Sesion(); s2.setEstado(1);
        when(sesionRepository.findSesionsByIdusuariofrontend_Id(1)).thenReturn(List.of(s1, s2));

        sesionService.cerrarTodasLasSesiones(1);

        assertThat(s1.getEstado()).isEqualTo(0);
        assertThat(s2.getEstado()).isEqualTo(0);
        verify(sesionRepository).saveAll(List.of(s1, s2));
    }

    @Test
    @DisplayName("cerrarTodasLasSesiones: no falla si el usuario no tiene sesiones")
    void cerrarTodasLasSesiones_shouldNotFail_whenNoSessions() {
        when(sesionRepository.findSesionsByIdusuariofrontend_Id(99)).thenReturn(List.of());

        assertDoesNotThrow(() -> sesionService.cerrarTodasLasSesiones(99));
        verify(sesionRepository).saveAll(List.of());
    }
}
