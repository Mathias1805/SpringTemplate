package Template.Api.Controller;

import Template.Api.DTO.DTO_LoginRequest;
import Template.Api.DTO.DTO_RegisterRequest;
import Template.Api.DTO.DTO_UserCode;
import Template.Api.Service.UsuarioFrontendService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/Usuario")
@RestController
public class UsuarioFrontendController {
    @Autowired
    private final UsuarioFrontendService usuarioService;

    @PostMapping("/Login")
    ResponseEntity<?> Login(@RequestBody DTO_LoginRequest loginRequest){
        return ResponseEntity.ok(usuarioService.Login(loginRequest));
    }
    @PostMapping("/Register")
    ResponseEntity<?> Register(@RequestBody DTO_RegisterRequest registerRequest){
        return ResponseEntity.ok(usuarioService.Register(registerRequest));
    }
    @PostMapping("/Validate")
    ResponseEntity<?> Validate(@RequestBody DTO_UserCode UserCode){
        return ResponseEntity.ok(usuarioService.ValidateUser(UserCode));
    }
}
