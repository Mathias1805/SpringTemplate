package Template.Api.Repository;

import Template.Api.UserFrontend.Dispositivo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DispositivoRepository extends JpaRepository<Dispositivo,Integer> {
    Optional<Dispositivo> findDispositivoByIpAndNombre(String ip, String nombre);
}
