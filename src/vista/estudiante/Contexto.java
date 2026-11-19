package vista.estudiante;

import eipi.util.Sesion;
import modelo.Usuario;

final class Contexto {

    private Contexto() {
    }

    static int idEstudiante() {
        Usuario u = Sesion.getUsuarioActual();
        if (u == null) {
            throw new IllegalStateException("No hay una sesión iniciada.");
        }
        return u.getIdUsuario();
    }

    static int idInstitucion() {
        Usuario u = Sesion.getUsuarioActual();
        if (u == null || u.getIdInstitucion() == null) {
            throw new IllegalStateException("Tu usuario no está asociado a ninguna institución.");
        }
        return u.getIdInstitucion();
    }
}
