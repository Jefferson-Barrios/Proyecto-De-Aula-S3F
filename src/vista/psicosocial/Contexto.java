package vista.psicosocial;

import eipi.util.Formato;
import eipi.util.Sesion;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import modelo.Usuario;

final class Contexto {

    private Contexto() {
    }

    static int idPsicosocial() {
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

    static LocalDate fechaCita(String texto) {
        LocalDate fecha;
        try {
            fecha = Formato.parsear(texto);
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("La fecha debe tener el formato dd/mm/aaaa (por ejemplo 20/10/2026).");
        }
        if (fecha.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("La fecha de la cita no puede ser anterior a hoy.");
        }
        return fecha;
    }

    static String horaCita(String texto) {
        String t = texto.trim();
        if (!t.matches("^([01]?\\d|2[0-3]):[0-5]\\d$")) {
            throw new IllegalArgumentException("La hora debe estar en formato de 24 horas, por ejemplo 09:30 o 14:15.");
        }
        return t.length() == 4 ? "0" + t : t;
    }

    static String motivoCita(String texto) {
        String m = texto.trim();
        if (m.length() < 5) {
            throw new IllegalArgumentException("Escribe el motivo de la cita (mínimo 5 caracteres).");
        }
        if (m.length() > 255) {
            throw new IllegalArgumentException("El motivo supera los 255 caracteres.");
        }
        return m;
    }
}
