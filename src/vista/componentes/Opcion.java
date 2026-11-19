package vista.componentes;

public class Opcion {

    private final int id;
    private final String texto;
    private final Object dato;

    public Opcion(int id, String texto, Object dato) {
        this.id = id;
        this.texto = texto;
        this.dato = dato;
    }

    public int getId() {
        return id;
    }

    public Object getDato() {
        return dato;
    }

    @Override
    public String toString() {
        return texto;
    }
}
