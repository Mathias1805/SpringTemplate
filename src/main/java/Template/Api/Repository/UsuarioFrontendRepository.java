package Template.Api.Repository;

import Template.Api.UserFrontend.Usuariofrontend;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioFrontendRepository extends JpaRepository<Usuariofrontend, Integer> {
    Optional<Usuariofrontend> findByUsuarioAndContrasenia(String usuario, String contrasenia);
    Optional<Usuariofrontend> findByUsuarioAndCodver(String usuario, String codver);
    Optional<Usuariofrontend> findByUsuario(String usuario);
    Optional<Usuariofrontend> findByEmail(String email);
}
