package Template.Api.Auth;

public enum TipoUsuario {
    CLIENTE(1),
    USUARIO_FRONTEND(2);

    private final int valor;

    TipoUsuario(int valor) {
        this.valor = valor;
    }

    public int getValor() {
        return valor;
    }

    public static TipoUsuario fromValor(Integer valor) {
        if (valor == null) {
            throw new IllegalStateException("El tipo de usuario no fue especificado");
        }
        for (TipoUsuario tipo : values()) {
            if (tipo.valor == valor) return tipo;
        }
        throw new IllegalStateException("Tipo de usuario no válido: " + valor);
    }
}
