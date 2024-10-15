package Template.Api.UserFrontend;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "sesion")
public class Sesion {
    @JsonIgnore
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "idsesion", nullable = false)
    private Integer id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "idusuariofrontend", nullable = false)
    private Usuariofrontend idusuariofrontend;

    @Column(name = "estado", nullable = false)
    private Integer estado;

    @Column(name = "idioma")
    private String idioma;
    @JsonIgnore
    @NotNull
    @ManyToOne(optional = false)
    @JoinColumn(name = "iddispositivo", nullable = false)
    private Dispositivo iddispositivo;

}