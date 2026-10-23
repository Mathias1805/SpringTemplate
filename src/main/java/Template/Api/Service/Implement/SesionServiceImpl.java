package Template.Api.Service.Implement;

import Template.Api.Repository.SesionRepository;
import Template.Api.Service.SesionService;
import Template.Api.UserFrontend.Dispositivo;
import Template.Api.UserFrontend.Sesion;
import Template.Api.UserFrontend.Usuariofrontend;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SesionServiceImpl implements SesionService {

    private final SesionRepository sesionRepository;

    @Override
    @Transactional
    public Sesion crearSesion(Usuariofrontend usuario, Dispositivo dispositivo, String idioma) {
        Sesion sesion = new Sesion();
        sesion.setIdusuariofrontend(usuario);
        sesion.setIddispositivo(dispositivo);
        sesion.setEstado(1);
        sesion.setIdioma(idioma != null ? idioma : "es");
        return sesionRepository.save(sesion);
    }

    @Override
    public List<Sesion> obtenerSesionesPorUsuario(Integer idUsuario) {
        return sesionRepository.findSesionsByIdusuariofrontend_Id(idUsuario);
    }

    @Override
    public List<Sesion> obtenerSesionesPorIp(String ip) {
        return sesionRepository.findSesionsByIddispositivo_Ip(ip);
    }

    @Override
    @Transactional
    public void cerrarSesion(Integer idSesion) {
        sesionRepository.findById(idSesion).ifPresent(sesion -> {
            sesion.setEstado(0);
            sesionRepository.save(sesion);
        });
    }

    @Override
    @Transactional
    public void cerrarTodasLasSesiones(Integer idUsuario) {
        List<Sesion> sesiones = sesionRepository.findSesionsByIdusuariofrontend_Id(idUsuario);
        sesiones.forEach(s -> s.setEstado(0));
        sesionRepository.saveAll(sesiones);
    }
}
