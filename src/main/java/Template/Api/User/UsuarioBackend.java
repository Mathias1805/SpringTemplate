package Template.Api.User;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
@Entity
@Getter
@Setter
public class UsuarioBackend implements UserDetails {
    @Id
    @Column(name = "IdUsuarioBackend",unique = true)
    @GeneratedValue(strategy = GenerationType.AUTO)
    Integer IdUsuarioBackend;
    @Column(nullable = false,unique = true)
    String usuario;
    @Column(name = "contrasenia", nullable = false)
    String contraseia;
    @ManyToOne(optional = false)
    @JoinColumn(name = "idrol",nullable = false)
    Rol rol;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(rol.getRol()));
    }

    @Override
    public String getPassword() {
        return this.contraseia;
    }

    @Override
    public String getUsername() {
        return this.usuario;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}

