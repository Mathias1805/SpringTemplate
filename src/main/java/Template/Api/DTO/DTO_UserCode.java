package Template.Api.DTO;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DTO_UserCode {

    @NotBlank(message = "El código de verificación es obligatorio")
    private String codver;

    @NotBlank(message = "El usuario es obligatorio")
    private String user;
}
