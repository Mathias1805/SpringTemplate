package Template.Api.DTO;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DTO_LoginRequest {

    @NotBlank(message = "La versión de la app es obligatoria")
    String versionapp;

    @NotBlank(message = "El nombre del dispositivo es obligatorio")
    String nombreDispositivo;

    @NotBlank(message = "El sistema operativo es obligatorio")
    String sistemaOperativo;

    @NotBlank(message = "La IP es obligatoria")
    String ip;

    String ubicacion;

    @NotBlank(message = "El usuario es obligatorio")
    String usuario;

    @NotBlank(message = "La contraseña es obligatoria")
    String contrasenia;

    // Solo requeridos para tipousuario=2 (/Usuario/Login).
    // El controller setea tipousuario; no es necesario enviarlo en el body.
    String usuariofrontend;
    String contraseniafrontend;
    Integer tipousuario;
}
