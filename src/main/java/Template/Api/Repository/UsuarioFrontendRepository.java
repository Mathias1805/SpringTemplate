package Template.Api.Repository;

import Template.Api.UserFrontend.Usuariofrontend;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioFrontendRepository extends JpaRepository<Usuariofrontend, Integer> {
    Optional<Usuariofrontend> findByUsuarioAndAndContrasenia(String Usuario,String Contrasenia);
}
