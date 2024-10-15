package Template.Api.UserFrontend;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "dispositivo")
public class Dispositivo {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "iddispositivo", nullable = false)
    private Integer id;

    @NotNull
    @Column(name = "nombre", nullable = false, length = Integer.MAX_VALUE)
    private String nombre;

    @NotNull
    @Column(name = "ip", nullable = false, length = Integer.MAX_VALUE)
    private String ip;

    @NotNull
    @Column(name = "sistemaoperativo", nullable = false, length = Integer.MAX_VALUE)
    private String sistemaoperativo;

    @NotNull
    @Column(name = "versionapp", nullable = false, length = Integer.MAX_VALUE)
    private String versionapp;

    @Column(name = "ubicacion", length = Integer.MAX_VALUE)
    private String ubicacion;
    @NotNull
    @Column(name = "estado", length = Integer.MAX_VALUE)
    private Integer estado;

    @OneToMany(mappedBy = "iddispositivo")
    private Set<Sesion> sesions = new LinkedHashSet<>();

}