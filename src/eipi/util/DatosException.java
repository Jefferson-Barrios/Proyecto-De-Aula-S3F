package eipi.util;

public class DatosException extends Exception {

    public DatosException(String mensaje) {
        super(mensaje);
    }

    public DatosException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
