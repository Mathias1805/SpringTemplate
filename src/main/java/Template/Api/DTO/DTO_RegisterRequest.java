package Template.Api.DTO;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DTO_RegisterRequest {

    @NotBlank(message = "El usuario es obligatorio")
    @Size(min = 3, max = 50, message = "El usuario debe tener entre 3 y 50 caracteres")
    String usuario;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
    String contrasenia;

    @Email(message = "El email no tiene un formato válido")
    @NotBlank(message = "El email es obligatorio")
    String email;

    Integer telefono;
    Integer dni;

    @NotBlank(message = "El nombre es obligatorio")
    String nombre;

    String paterno;
    String materno;
    String direccion;

    // Solo usado en registro de usuarios backend
    String rol;
}
