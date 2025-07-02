package Template.Api.DTO;

import Template.Api.UserFrontend.Sesion;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DTO_AuthResponse {
    String token;
    List<Sesion> sesiones;
}
