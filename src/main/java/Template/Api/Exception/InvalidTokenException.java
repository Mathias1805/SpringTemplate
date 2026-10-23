package Template.Api.Exception;

public class InvalidTokenException extends RuntimeException {
    public InvalidTokenException() {
        super("Token inválido o expirado");
    }
}
