package Template.Api.UserFrontend;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "usuariofrontend")
public class Usuariofrontend {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "idusuariofrontend")
    private Integer id;

    @Column(name = "usuario", nullable = false, length = Integer.MAX_VALUE,unique = true)
    private String usuario;

    @Column(name = "contrasenia", nullable = false, length = Integer.MAX_VALUE)
    private String contrasenia;
    @Email
    @Column(name = "email", nullable = false, length = Integer.MAX_VALUE,unique = true)
    private String email;

    @Column(name = "telefono", nullable = false,unique = true)
    private Integer telefono;

    @Column(name = "dni", nullable = false,unique = true)
    private Integer dni;

    @Column(name = "nombre", nullable = false, length = Integer.MAX_VALUE)
    private String nombre;

    @Column(name = "paterno", nullable = false, length = Integer.MAX_VALUE)
    private String paterno;

    @Column(name = "materno", nullable = false, length = Integer.MAX_VALUE)
    private String materno;

    @Column(name = "cod_ver", nullable = true,unique = false)
    private String cod_ver;

    @Column(name = "direccion", nullable = true,unique = false)
    private String direccion;

    @Column(name = "activo", nullable = false,unique = false)
    private Integer activo;

    @JsonIgnore
    @OneToMany(mappedBy = "idusuariofrontend")
    private Set<Sesion> sesions = new LinkedHashSet<>();

}