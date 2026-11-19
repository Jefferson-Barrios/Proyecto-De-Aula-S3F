package controlador;

import java.util.List;
import java.util.Locale;

public final class AnalisisRiesgo {

    public static final String ADECUADO = "ADECUADO";
    public static final String PREVENTIVA = "PREVENTIVA";
    public static final String MEDIO = "MEDIO";
    public static final String ALTO = "ALTO";

    public static final double NOTA_MINIMA = 3.0;

    private static final Locale ES = Locale.forLanguageTag("es-CO");

    private static final String[][] MATRIZ = {
        {ADECUADO, MEDIO, ALTO, ALTO},
        {PREVENTIVA, MEDIO, ALTO, ALTO},
        {MEDIO, MEDIO, ALTO, ALTO},
        {MEDIO, ALTO, ALTO, ALTO}};

    private AnalisisRiesgo() {
    }

    public static Double calcularPromedio(List<Double> notas) {
        if (notas == null || notas.isEmpty()) {
            return null;
        }
        double suma = 0;
        for (double n : notas) {
            suma += n;
        }
        return suma / notas.size();
    }

    public static double redondear(double valor) {
        return Math.round(valor * 10) / 10.0;
    }

    public static boolean esMateriaPerdida(double definitiva) {
        return redondear(definitiva) < NOTA_MINIMA;
    }

    public static Double porcentajeAsistencia(int totalRegistros, int ausencias) {
        if (totalRegistros <= 0) {
            return null;
        }
        return redondear((totalRegistros - ausencias) * 100.0 / totalRegistros);
    }

    private static int fila(double asistencia) {
        if (asistencia >= 90) {
            return 0;
        }
        if (asistencia >= 80) {
            return 1;
        }
        return asistencia >= 70 ? 2 : 3;
    }

    private static int columna(int materiasPerdidas) {
        if (materiasPerdidas <= 0) {
            return 0;
        }
        if (materiasPerdidas <= 2) {
            return 1;
        }
        return materiasPerdidas <= 4 ? 2 : 3;
    }

    public static String clasificar(Double asistencia, int materiasPerdidas) {
        double a = asistencia == null ? 100 : redondear(asistencia);
        return MATRIZ[fila(a)][columna(materiasPerdidas)];
    }

    public static boolean generaAlerta(String nivel) {
        return PREVENTIVA.equals(nivel) || MEDIO.equals(nivel) || ALTO.equals(nivel);
    }

    public static String formatoPorcentaje(Double asistencia) {
        return asistencia == null ? "—" : String.format(ES, "%.1f %%", asistencia);
    }

    public static String motivo(String periodo, Double asistencia, List<String> materiasPerdidas, String nivel) {
        StringBuilder sb = new StringBuilder();
        sb.append("Asistencia: ").append(asistencia == null ? "sin registros" : formatoPorcentaje(asistencia));
        sb.append(". Materias perdidas: ").append(materiasPerdidas.size());
        if (!materiasPerdidas.isEmpty()) {
            sb.append(" (").append(String.join(", ", materiasPerdidas)).append(")");
        }
        sb.append(".");
        if (periodo != null && !periodo.isEmpty()) {
            sb.append(" ").append(periodo).append(".");
        }
        double a = asistencia == null ? 100 : asistencia;
        if (ALTO.equals(nivel) && a < 90 && !materiasPerdidas.isEmpty()) {
            sb.append(" La combinación de inasistencias y dificultades académicas requiere intervención prioritaria.");
        } else if (MEDIO.equals(nivel) && a < 70 && materiasPerdidas.isEmpty()) {
            sb.append(" Investigar la causa de las ausencias y hacer seguimiento.");
        }
        String texto = sb.toString();
        return texto.length() <= 255 ? texto : texto.substring(0, 252) + "...";
    }
}
