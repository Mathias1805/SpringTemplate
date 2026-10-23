package Template.Api.Exception;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String usuario) {
        super("Usuario no encontrado: " + usuario);
    }
}
