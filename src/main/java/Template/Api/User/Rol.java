package Template.Api.User;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.LinkedHashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
@Entity
@Getter
@Setter
public class Rol {
    @Id
    @Column(name = "IdRol", nullable = false)
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer IdRol;
    @Column(nullable = false,unique = true)
    private String rol;
    @JsonIgnore
    @OneToMany(mappedBy = "rol")
    private Set<UsuarioBackend> usuarios = new LinkedHashSet<>();
}
