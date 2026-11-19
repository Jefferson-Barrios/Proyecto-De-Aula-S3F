package eipi.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Locale;

public final class Formato {

    private static final DateTimeFormatter ENTRADA =
            DateTimeFormatter.ofPattern("d/M/uuuu").withResolverStyle(ResolverStyle.STRICT);
    private static final DateTimeFormatter SALIDA = DateTimeFormatter.ofPattern("dd/MM/uuuu");
    private static final Locale ES = Locale.forLanguageTag("es-CO");

    private Formato() {
    }

    public static String texto(LocalDate fecha) {
        return fecha == null ? "" : fecha.format(SALIDA);
    }

    public static LocalDate parsear(String texto) throws DateTimeParseException {
        return LocalDate.parse(texto.trim(), ENTRADA);
    }

    public static String hora(String texto) {
        String t = texto == null ? "" : texto.trim();
        if (!t.matches("^([01]?\\d|2[0-3]):[0-5]\\d$")) {
            throw new IllegalArgumentException(
                    "La hora debe estar en formato de 24 horas, por ejemplo 07:00 o 14:15.");
        }
        return t.length() == 4 ? "0" + t : t;
    }

    public static String nota(double valor) {
        return String.format(ES, "%.1f", valor);
    }
}
