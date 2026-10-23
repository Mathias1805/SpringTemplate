package Template.Api.Repository;

import Template.Api.UserFrontend.Sesion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SesionRepository extends JpaRepository<Sesion, Integer> {
    List<Sesion> findSesionsByIddispositivo_Ip(String ip);
    List<Sesion> findSesionsByIdusuariofrontend_Id(Integer idUsuario);
}
