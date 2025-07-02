package Template.Api.DTO;

import lombok.*;

@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DTO_RegisterRequest {
    String usuario;
    String contrasenia;
    String email;
    Integer telefono;
    Integer dni;
    String nombre;
    String paterno;
    String materno;
    String direccion;
    String rol;
}
