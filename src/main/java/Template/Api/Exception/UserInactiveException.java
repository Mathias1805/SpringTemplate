package Template.Api.Exception;

public class UserInactiveException extends RuntimeException {
    public UserInactiveException(String usuario) {
        super("El usuario '" + usuario + "' no está activo. Verificá tu email para activar la cuenta.");
    }
}
