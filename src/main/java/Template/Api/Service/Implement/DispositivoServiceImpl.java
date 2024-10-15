package Template.Api.Service.Implement;

import Template.Api.Repository.DispositivoRepository;
import Template.Api.Service.DispositivoService;
import Template.Api.UserFrontend.Dispositivo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class DispositivoServiceImpl implements DispositivoService {
    @Autowired
    private DispositivoRepository dispositivoRepository;
    @Override
    public Optional<Dispositivo> findDispositivoByIpAndNombre(String ip,String nombre) {
        return dispositivoRepository.findDispositivoByIpAndNombre(ip,nombre);
    }
}
