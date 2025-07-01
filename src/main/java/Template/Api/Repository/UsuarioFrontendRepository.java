package Template.Api.Repository;

import Template.Api.UserFrontend.Usuariofrontend;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioFrontendRepository extends JpaRepository<Usuariofrontend, Integer> {
    Optional<Usuariofrontend> findByUsuarioAndContrasenia(String Usuario,String Contrasenia);
    Optional<Usuariofrontend> findByUsuarioAndCodver(String user,String cod_ver);

    Optional<Usuariofrontend> findByUsuario(String usuario);
}
