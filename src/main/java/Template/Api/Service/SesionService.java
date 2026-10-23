package Template.Api.Service;

import Template.Api.UserFrontend.Dispositivo;
import Template.Api.UserFrontend.Sesion;
import Template.Api.UserFrontend.Usuariofrontend;

import java.util.List;

public interface SesionService {
    Sesion crearSesion(Usuariofrontend usuario, Dispositivo dispositivo, String idioma);
    List<Sesion> obtenerSesionesPorUsuario(Integer idUsuario);
    List<Sesion> obtenerSesionesPorIp(String ip);
    void cerrarSesion(Integer idSesion);
    void cerrarTodasLasSesiones(Integer idUsuario);
}
