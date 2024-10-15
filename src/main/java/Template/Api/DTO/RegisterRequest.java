package Template.Api.DTO;

import lombok.*;

@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {
    String usuario;
    String contrasenia;
    String rol;
}
