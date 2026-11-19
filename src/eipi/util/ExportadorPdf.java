package eipi.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

public final class ExportadorPdf {

    private static final Charset CP1252 = Charset.forName("windows-1252");
    private static final DateTimeFormatter FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private static final double MARGEN = 36;
    private static final double TAM = 9;
    private static final double PAD = 4;
    private static final double INTERLINEA = 11.5;
    private static final double ANCHO_MAX_COLUMNA = 300;
    private static final double ANCHO_MIN_COLUMNA = 46;
    private static final double PIE = 26;

    private static final int[] REGULAR = {
        278, 278, 355, 556, 556, 889, 667, 191, 333, 333, 389, 584, 278, 333, 278, 278,
        556, 556, 556, 556, 556, 556, 556, 556, 556, 556, 278, 278, 584, 584, 584, 556,
        1015, 667, 667, 722, 722, 667, 611, 778, 722, 278, 500, 667, 556, 833, 722, 778,
        667, 778, 722, 667, 611, 722, 667, 944, 667, 667, 611, 278, 278, 278, 469, 556,
        333, 556, 556, 500, 556, 556, 278, 556, 556, 222, 222, 500, 222, 833, 556, 556,
        556, 556, 333, 500, 278, 556, 500, 722, 500, 500, 500, 334, 260, 334, 584, 761,
        556, 556, 222, 556, 333, 1000, 556, 556, 333, 1000, 667, 333, 1000, 556, 611, 556,
        556, 222, 222, 333, 333, 350, 556, 1000, 333, 1000, 500, 333, 944, 556, 500, 667,
        278, 333, 556, 556, 556, 556, 260, 556, 333, 737, 370, 556, 584, 333, 737, 333,
        400, 584, 333, 333, 333, 556, 537, 278, 333, 333, 365, 556, 834, 834, 834, 611,
        667, 667, 667, 667, 667, 667, 1000, 722, 667, 667, 667, 667, 278, 278, 278, 278,
        722, 722, 778, 778, 778, 778, 778, 584, 778, 722, 722, 722, 722, 667, 667, 611,
        556, 556, 556, 556, 556, 556, 889, 500, 556, 556, 556, 556, 278, 278, 278, 278,
        556, 556, 556, 556, 556, 556, 556, 584, 611, 556, 556, 556, 556, 500, 556, 500
    };

    private static final int[] NEGRITA = {
        278, 333, 474, 556, 556, 889, 722, 238, 333, 333, 389, 584, 278, 333, 278, 278,
        556, 556, 556, 556, 556, 556, 556, 556, 556, 556, 333, 333, 584, 584, 584, 611,
        975, 722, 722, 722, 722, 667, 611, 778, 722, 278, 556, 722, 611, 833, 722, 778,
        667, 778, 722, 667, 611, 722, 667, 944, 667, 667, 611, 333, 278, 333, 584, 556,
        333, 556, 611, 556, 611, 556, 333, 611, 611, 278, 278, 556, 278, 889, 611, 611,
        611, 611, 389, 556, 333, 611, 556, 778, 556, 556, 500, 389, 280, 389, 584, 761,
        556, 556, 278, 556, 500, 1000, 556, 556, 333, 1000, 667, 333, 1000, 556, 611, 556,
        556, 278, 278, 500, 500, 350, 556, 1000, 333, 1000, 556, 333, 944, 556, 500, 667,
        278, 333, 556, 556, 556, 556, 280, 556, 333, 737, 370, 556, 584, 333, 737, 333,
        400, 584, 333, 333, 333, 611, 556, 278, 333, 333, 365, 556, 834, 834, 834, 611,
        722, 722, 722, 722, 722, 722, 1000, 722, 667, 667, 667, 667, 278, 278, 278, 278,
        722, 722, 778, 778, 778, 778, 778, 584, 778, 722, 722, 722, 722, 667, 667, 611,
        556, 556, 556, 556, 556, 556, 889, 556, 556, 556, 556, 556, 278, 278, 278, 278,
        611, 611, 611, 611, 611, 611, 611, 584, 611, 611, 611, 611, 611, 556, 611, 556
    };

    private ExportadorPdf() {
    }

    public static void escribir(TablaExportable t, File destino) throws IOException {
        byte[] datos = generar(t);
        try (OutputStream salida = new FileOutputStream(destino)) {
            salida.write(datos);
        }
    }

    private static int codigo(char c) {
        byte[] b = String.valueOf(c).getBytes(CP1252);
        return b.length == 0 ? '?' : (b[0] & 0xFF);
    }

    static double ancho(String texto, boolean negrita) {
        int[] tabla = negrita ? NEGRITA : REGULAR;
        double suma = 0;
        for (int i = 0; i < texto.length(); i++) {
            int cod = codigo(texto.charAt(i));
            suma += cod < 32 ? 0 : tabla[cod - 32];
        }
        return suma * TAM / 1000.0;
    }

    static List<String> envolver(String texto, double anchoUtil, boolean negrita) {
        List<String> lineas = new ArrayList<>();
        for (String parrafo : texto.split("\n", -1)) {
            StringBuilder actual = new StringBuilder();
            for (String palabra : parrafo.split(" ")) {
                String prueba = actual.length() == 0 ? palabra : actual + " " + palabra;
                if (ancho(prueba, negrita) <= anchoUtil) {
                    actual = new StringBuilder(prueba);
                    continue;
                }
                if (actual.length() > 0) {
                    lineas.add(actual.toString());
                    actual = new StringBuilder();
                }

                String resto = palabra;
                while (ancho(resto, negrita) > anchoUtil && resto.length() > 1) {
                    int corte = resto.length() - 1;
                    while (corte > 1 && ancho(resto.substring(0, corte), negrita) > anchoUtil) {
                        corte--;
                    }
                    lineas.add(resto.substring(0, corte));
                    resto = resto.substring(corte);
                }
                actual = new StringBuilder(resto);
            }
            lineas.add(actual.toString());
        }
        return lineas;
    }

    static double[] repartir(double[] natural, double disponible) {
        int n = natural.length;
        double[] w = new double[n];
        double suma = 0;
        for (double v : natural) {
            suma += v;
        }
        if (suma <= disponible) {
            for (int i = 0; i < n; i++) {
                w[i] = natural[i] * disponible / suma;
            }
            return w;
        }
        boolean[] fijo = new boolean[n];
        double restante = disponible;
        int libres = n;
        boolean cambio = true;
        while (cambio && libres > 0) {
            cambio = false;
            double cuota = restante / libres;
            for (int i = 0; i < n; i++) {
                if (!fijo[i] && natural[i] <= cuota) {
                    fijo[i] = true;
                    w[i] = natural[i];
                    restante -= natural[i];
                    libres--;
                    cambio = true;
                }
            }
        }
        double sumaLibres = 0;
        for (int i = 0; i < n; i++) {
            if (!fijo[i]) {
                sumaLibres += natural[i];
            }
        }
        for (int i = 0; i < n; i++) {
            if (!fijo[i]) {
                w[i] = Math.max(ANCHO_MIN_COLUMNA, restante * natural[i] / sumaLibres);
            }
        }
        double total = 0;
        for (double v : w) {
            total += v;
        }
        if (total > disponible) {
            for (int i = 0; i < n; i++) {
                w[i] *= disponible / total;
            }
        }
        return w;
    }

    private static final class Fila {
        final List<List<String>> lineas = new ArrayList<>();
        double alto;
    }

    private static Fila medirFila(String[] celdas, double[] anchos, boolean negrita) {
        Fila f = new Fila();
        int maxLineas = 1;
        for (int c = 0; c < anchos.length; c++) {
            String texto = c < celdas.length && celdas[c] != null ? celdas[c] : "";
            List<String> ls = envolver(texto, anchos[c] - 2 * PAD, negrita);
            f.lineas.add(ls);
            maxLineas = Math.max(maxLineas, ls.size());
        }
        f.alto = maxLineas * INTERLINEA + 2 * PAD;
        return f;
    }

    public static byte[] generar(TablaExportable t) throws IOException {
        int n = t.encabezados.length;

        double[] natural = new double[n];
        for (int c = 0; c < n; c++) {
            natural[c] = ancho(t.encabezados[c], true) + 2 * PAD;
        }
        for (String[] fila : t.filas) {
            for (int c = 0; c < n && c < fila.length; c++) {
                if (fila[c] == null) {
                    continue;
                }
                for (String linea : fila[c].split("\n")) {
                    natural[c] = Math.max(natural[c], ancho(linea, false) + 2 * PAD);
                }
            }
        }
        double sumaNatural = 0;
        for (int c = 0; c < n; c++) {
            natural[c] = Math.min(natural[c], ANCHO_MAX_COLUMNA);
            sumaNatural += natural[c];
        }
        double vertical = 595.28;
        double largo = 841.89;
        boolean horizontal = sumaNatural > vertical - 2 * MARGEN;
        double anchoPagina = horizontal ? largo : vertical;
        double altoPagina = horizontal ? vertical : largo;
        double[] anchos = repartir(natural, anchoPagina - 2 * MARGEN);

        Fila encabezado = medirFila(t.encabezados, anchos, true);
        List<Fila> filas = new ArrayList<>();
        for (String[] f : t.filas) {
            filas.add(medirFila(f, anchos, false));
        }

        List<String> lineasSub = new ArrayList<>();
        if (!t.subtitulo.isEmpty()) {
            lineasSub.addAll(envolver(t.subtitulo, anchoPagina - 2 * MARGEN, false));
        }
        String total = t.filas.size() + (t.filas.size() == 1 ? " registro" : " registros");
        double altoTitulo = 24 + lineasSub.size() * 13 + 13 + 12;

        double minY = MARGEN + PIE;
        List<List<Integer>> paginas = new ArrayList<>();
        List<Integer> actual = new ArrayList<>();
        double y = altoPagina - MARGEN - altoTitulo - encabezado.alto;
        for (int i = 0; i < filas.size(); i++) {
            double h = filas.get(i).alto;
            if (y - h < minY && !actual.isEmpty()) {
                paginas.add(actual);
                actual = new ArrayList<>();
                y = altoPagina - MARGEN - encabezado.alto;
            }
            actual.add(i);
            y -= h;
        }
        paginas.add(actual);

        String generado = "E.I.P.I  ·  Generado el " + LocalDateTime.now().format(FECHA_HORA)
                + (t.generadoPor == null ? "" : "  ·  por " + t.generadoPor);
        List<String> contenidos = new ArrayList<>();
        for (int p = 0; p < paginas.size(); p++) {
            StringBuilder sb = new StringBuilder();
            double top = altoPagina - MARGEN;
            if (p == 0) {
                texto(sb, MARGEN, top - 16, 16, true, "0.12 0.16 0.27", t.titulo);
                double yy = top - 16 - 16;
                for (String l : lineasSub) {
                    texto(sb, MARGEN, yy, 10, false, "0.43 0.43 0.43", l);
                    yy -= 13;
                }
                texto(sb, MARGEN, yy, 9, false, "0.43 0.43 0.43", total);
                top -= altoTitulo;
            }
            double yFila = top;
            dibujarFila(sb, encabezado, anchos, yFila, true, false);
            yFila -= encabezado.alto;
            int numero = 0;
            for (int idx : paginas.get(p)) {
                Fila f = filas.get(idx);
                dibujarFila(sb, f, anchos, yFila, false, idx % 2 == 1);
                yFila -= f.alto;
                numero++;
            }

            String paginaTxt = "Página " + (p + 1) + " de " + paginas.size();
            sb.append("0.85 0.87 0.92 RG 0.5 w ").append(f(MARGEN)).append(' ').append(f(MARGEN + 14)).append(" m ")
                    .append(f(anchoPagina - MARGEN)).append(' ').append(f(MARGEN + 14)).append(" l S\n");
            texto(sb, MARGEN, MARGEN + 2, 8, false, "0.43 0.43 0.43", generado);
            texto(sb, anchoPagina - MARGEN - ancho(paginaTxt, false) * 8 / TAM, MARGEN + 2, 8, false,
                    "0.43 0.43 0.43", paginaTxt);
            contenidos.add(sb.toString());
        }

        return armar(t, contenidos, anchoPagina, altoPagina);
    }

    private static void dibujarFila(StringBuilder sb, Fila fila, double[] anchos, double yTop,
                                    boolean encabezado, boolean cebra) {
        double x = MARGEN;
        double total = 0;
        for (double a : anchos) {
            total += a;
        }
        if (encabezado) {
            sb.append("0.039 0.298 0.780 rg ").append(f(x)).append(' ').append(f(yTop - fila.alto)).append(' ')
                    .append(f(total)).append(' ').append(f(fila.alto)).append(" re f\n");
        } else {
            if (cebra) {
                sb.append("0.962 0.969 0.992 rg ").append(f(x)).append(' ').append(f(yTop - fila.alto)).append(' ')
                        .append(f(total)).append(' ').append(f(fila.alto)).append(" re f\n");
            }
            sb.append("0.88 0.90 0.95 RG 0.5 w ").append(f(x)).append(' ').append(f(yTop - fila.alto)).append(" m ")
                    .append(f(x + total)).append(' ').append(f(yTop - fila.alto)).append(" l S\n");
        }
        for (int c = 0; c < anchos.length; c++) {
            double yTexto = yTop - PAD - TAM * 0.9;
            for (String linea : fila.lineas.get(c)) {
                if (!linea.isEmpty()) {
                    texto(sb, x + PAD, yTexto, TAM, encabezado, encabezado ? "1 1 1" : "0.12 0.16 0.27", linea);
                }
                yTexto -= INTERLINEA;
            }
            x += anchos[c];
        }
    }

    private static String f(double v) {
        return String.format(Locale.US, "%.2f", v);
    }

    private static String cadena(String s) {
        String latin1 = new String(s.getBytes(CP1252), StandardCharsets.ISO_8859_1);
        StringBuilder sb = new StringBuilder("(");
        for (int i = 0; i < latin1.length(); i++) {
            char c = latin1.charAt(i);
            if (c == '(' || c == ')' || c == '\\') {
                sb.append('\\');
            }
            if (c == '\r' || c == '\n') {
                sb.append(' ');
            } else {
                sb.append(c);
            }
        }
        return sb.append(')').toString();
    }

    private static void texto(StringBuilder sb, double x, double y, double tam, boolean negrita,
                              String color, String s) {
        sb.append("BT ").append(color).append(" rg /").append(negrita ? "F2 " : "F1 ").append(f(tam))
                .append(" Tf ").append(f(x)).append(' ').append(f(y)).append(" Td ")
                .append(cadena(s)).append(" Tj ET\n");
    }

    private static byte[] armar(TablaExportable t, List<String> contenidos, double ancho, double alto)
            throws IOException {
        int paginas = contenidos.size();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        List<Integer> posiciones = new ArrayList<>();
        escribir(out, "%PDF-1.4\n");
        out.write(new byte[]{'%', (byte) 0xE2, (byte) 0xE3, (byte) 0xCF, (byte) 0xD3, '\n'});

        StringBuilder kids = new StringBuilder();
        for (int p = 0; p < paginas; p++) {
            kids.append(6 + 2 * p).append(" 0 R ");
        }
        objeto(out, posiciones, 1, "<< /Type /Catalog /Pages 2 0 R >>");
        objeto(out, posiciones, 2, "<< /Type /Pages /Kids [" + kids.toString().trim() + "] /Count " + paginas + " >>");
        objeto(out, posiciones, 3, "<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica /Encoding /WinAnsiEncoding >>");
        objeto(out, posiciones, 4, "<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica-Bold /Encoding /WinAnsiEncoding >>");
        String fecha = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        objeto(out, posiciones, 5, "<< /Title " + hexUtf16(t.titulo) + " /Producer " + hexUtf16("E.I.P.I")
                + " /CreationDate (D:" + fecha + ") >>");

        for (int p = 0; p < paginas; p++) {
            int idPagina = 6 + 2 * p;
            int idContenido = 7 + 2 * p;
            objeto(out, posiciones, idPagina, "<< /Type /Page /Parent 2 0 R /MediaBox [0 0 " + f(ancho) + " " + f(alto)
                    + "] /Resources << /Font << /F1 3 0 R /F2 4 0 R >> >> /Contents " + idContenido + " 0 R >>");

            ByteArrayOutputStream comprimido = new ByteArrayOutputStream();
            try (DeflaterOutputStream d = new DeflaterOutputStream(comprimido, new Deflater(Deflater.BEST_COMPRESSION))) {
                d.write(contenidos.get(p).getBytes(StandardCharsets.ISO_8859_1));
            }
            posiciones.add(out.size());
            escribir(out, idContenido + " 0 obj\n<< /Length " + comprimido.size() + " /Filter /FlateDecode >>\nstream\n");
            out.write(comprimido.toByteArray());
            escribir(out, "\nendstream\nendobj\n");
        }

        int inicioXref = out.size();
        int totalObjetos = 6 + 2 * paginas;
        StringBuilder xref = new StringBuilder("xref\n0 " + totalObjetos + "\n0000000000 65535 f \n");
        for (int pos : posiciones) {
            xref.append(String.format(Locale.US, "%010d 00000 n \n", pos));
        }
        xref.append("trailer\n<< /Size ").append(totalObjetos).append(" /Root 1 0 R /Info 5 0 R >>\nstartxref\n")
                .append(inicioXref).append("\n%%EOF\n");
        escribir(out, xref.toString());
        return out.toByteArray();
    }

    private static void objeto(ByteArrayOutputStream out, List<Integer> posiciones, int id, String cuerpo)
            throws IOException {
        posiciones.add(out.size());
        escribir(out, id + " 0 obj\n" + cuerpo + "\nendobj\n");
    }

    private static void escribir(ByteArrayOutputStream out, String s) throws IOException {
        out.write(s.getBytes(StandardCharsets.ISO_8859_1));
    }

    private static String hexUtf16(String s) {
        StringBuilder sb = new StringBuilder("<FEFF");
        for (char c : s.toCharArray()) {
            sb.append(String.format("%04X", (int) c));
        }
        return sb.append('>').toString();
    }
}
