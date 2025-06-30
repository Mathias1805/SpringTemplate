package Template.Api.DTO;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class LoginRequest {
    String versionapp;
    String nombreDispositivo;
    String ip;
    String sistemaOperativo;
    String ubicacion;
    String usuario;
    String contrasenia;
    String token;
    String usuariofrontend;
    String contraseniafrontend;
    Integer TipoUsuario;
}
