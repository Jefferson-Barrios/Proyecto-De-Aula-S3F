package eipi.util;

import modelo.Usuario;

/** Guarda el usuario que inició sesión mientras la aplicación está abierta. */
public class Sesion {

    private static Usuario usuarioActual;

    private Sesion() {
    }

    public static Usuario getUsuarioActual() {
        return usuarioActual;
    }

    public static void setUsuarioActual(Usuario usuario) {
        usuarioActual = usuario;
    }

    public static void cerrar() {
        usuarioActual = null;
    }
}
