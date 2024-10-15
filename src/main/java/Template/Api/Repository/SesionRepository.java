package Template.Api.Repository;

import Template.Api.UserFrontend.Sesion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SesionRepository extends JpaRepository<Sesion,Integer> {
    List<Sesion> findSesionsByIddispositivo_Ip(String ip);
}
