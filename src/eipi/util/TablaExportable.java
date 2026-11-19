package eipi.util;

import java.text.Normalizer;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.table.TableModel;
import modelo.Usuario;

public final class TablaExportable {

    public final String titulo;
    public final String subtitulo;
    public final String[] encabezados;
    public final List<String[]> filas;
    public final String generadoPor;
    private final Set<Integer> columnasNumericas = new HashSet<>();

    public TablaExportable(String titulo, String subtitulo, String[] encabezados, List<String[]> filas) {
        this.titulo = titulo == null ? "" : titulo;
        this.subtitulo = subtitulo == null ? "" : subtitulo;
        this.encabezados = encabezados;
        this.filas = filas;
        Usuario u = Sesion.getUsuarioActual();
        this.generadoPor = u == null ? null : u.getNombre();
    }

    public TablaExportable conColumnasNumericas(int... columnas) {
        for (int c : columnas) {
            columnasNumericas.add(c);
        }
        return this;
    }

    public boolean esNumerica(int columna) {
        return columnasNumericas.contains(columna);
    }

    public static TablaExportable de(String titulo, String subtitulo, TableModel modelo) {
        String[] enc = new String[modelo.getColumnCount()];
        for (int c = 0; c < enc.length; c++) {
            enc[c] = limpiar(modelo.getColumnName(c));
        }
        List<String[]> filas = new ArrayList<>();
        for (int f = 0; f < modelo.getRowCount(); f++) {
            String[] fila = new String[enc.length];
            for (int c = 0; c < enc.length; c++) {
                fila[c] = limpiar(modelo.getValueAt(f, c));
            }
            filas.add(fila);
        }
        return new TablaExportable(titulo, subtitulo, enc, filas);
    }

    public static String unir(Object... partes) {
        StringBuilder sb = new StringBuilder();
        for (Object p : partes) {
            String t = p == null ? "" : p.toString().trim();
            if (!t.isEmpty()) {
                if (sb.length() > 0) {
                    sb.append("  ·  ");
                }
                sb.append(t);
            }
        }
        return sb.toString();
    }

    static String limpiar(Object valor) {
        if (valor == null) {
            return "";
        }
        String s = String.valueOf(valor);
        if (s.startsWith("<html>")) {
            s = s.replaceAll("<br\\s*/?>", "\n").replaceAll("<[^>]*>", "");
        }
        return s.trim();
    }

    public String nombreArchivoBase() {
        String base = Normalizer.normalize(titulo, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        base = base.replaceAll("[^A-Za-z0-9]+", "_").replaceAll("^_+|_+$", "");
        if (base.isEmpty()) {
            base = "EIPI";
        }
        return base + "_" + LocalDate.now();
    }
}
