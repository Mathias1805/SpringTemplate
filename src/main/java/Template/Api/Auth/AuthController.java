package Template.Api.Auth;

import Template.Api.DTO.DTO_AuthResponse;
import Template.Api.DTO.DTO_LoginRequest;
import Template.Api.DTO.DTO_RegisterRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/Auth")
@RequiredArgsConstructor
public class AuthController {
    @Autowired
    private final AuthService authService;

    @PostMapping( "/Login")
    public ResponseEntity<DTO_AuthResponse> login(@RequestBody DTO_LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping( "/Register")
    public ResponseEntity<DTO_AuthResponse> register(@RequestBody DTO_RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }
}
