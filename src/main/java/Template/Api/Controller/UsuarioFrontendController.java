package Template.Api.Controller;

import Template.Api.DTO.LoginRequest;
import Template.Api.Service.UsuarioFrontendService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
@RequiredArgsConstructor
@RestController("/Usuario")
public class UsuarioFrontendController {

    @Autowired
    private final UsuarioFrontendService usuarioService;

    @PostMapping
    ResponseEntity<?> Login(LoginRequest loginRequest){
        return ResponseEntity.ok(usuarioService.Login(loginRequest));
    }
}
