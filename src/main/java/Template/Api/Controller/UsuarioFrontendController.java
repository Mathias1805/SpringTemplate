package Template.Api.Controller;

import Template.Api.DTO.LoginRequest;
import Template.Api.DTO.RegisterRequest;
import Template.Api.Service.UsuarioFrontendService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@RequiredArgsConstructor
@RequestMapping("/Usuario")
public class UsuarioFrontendController {

    private final UsuarioFrontendService usuarioService;

    @PostMapping("/Login")
    ResponseEntity<?> Login(@RequestBody LoginRequest loginRequest){
        return ResponseEntity.ok(usuarioService.Login(loginRequest));
    }
    @PostMapping("/Register")
    ResponseEntity<?> Register(@RequestBody RegisterRequest registerRequest){
        return ResponseEntity.ok(usuarioService.Register(registerRequest));
    }
}
