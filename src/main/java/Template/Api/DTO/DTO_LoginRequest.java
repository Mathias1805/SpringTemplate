package Template.Api.DTO;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class DTO_LoginRequest {
    String versionapp;
    String nombreDispositivo;
    String sistemaOperativo;
    String ip;
    String ubicacion;
    String usuario;
    String contrasenia;
    String usuariofrontend;
    String contraseniafrontend;
    Integer tipousuario;
}
