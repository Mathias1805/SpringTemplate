package Template.Api.User;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface UsuarioBackendRepository extends JpaRepository<UsuarioBackend, Integer> {
    Optional<UsuarioBackend> findUsuarioBackendByUsuario(String Usuario);
}
