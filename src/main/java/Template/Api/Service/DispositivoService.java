package Template.Api.Service;


import Template.Api.UserFrontend.Dispositivo;

import java.util.Optional;

public interface DispositivoService {
    Optional<Dispositivo> findDispositivoByIpAndNombre(String ip,String nombre);
}
