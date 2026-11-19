package eipi.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public final class ExportadorXlsx {

    private static final int FILA_ENCABEZADO = 4;
    private static final String NS = "http://schemas.openxmlformats.org/spreadsheetml/2006/main";

    private ExportadorXlsx() {
    }

    public static void escribir(TablaExportable t, File destino) throws IOException {
        try (OutputStream salida = new FileOutputStream(destino);
             ZipOutputStream zip = new ZipOutputStream(salida)) {
            agregar(zip, "[Content_Types].xml", tiposDeContenido());
            agregar(zip, "_rels/.rels", relacionesRaiz());
            agregar(zip, "xl/workbook.xml", libro(t));
            agregar(zip, "xl/_rels/workbook.xml.rels", relacionesLibro());
            agregar(zip, "xl/styles.xml", estilos());
            agregar(zip, "xl/worksheets/sheet1.xml", hoja(t));
        }
    }

    private static void agregar(ZipOutputStream zip, String nombre, String contenido) throws IOException {
        zip.putNextEntry(new ZipEntry(nombre));
        zip.write(contenido.getBytes(StandardCharsets.UTF_8));
        zip.closeEntry();
    }

    private static String tiposDeContenido() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\">"
                + "<Default Extension=\"rels\" ContentType=\"application/vnd.openxmlformats-package.relationships+xml\"/>"
                + "<Default Extension=\"xml\" ContentType=\"application/xml\"/>"
                + "<Override PartName=\"/xl/workbook.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml\"/>"
                + "<Override PartName=\"/xl/worksheets/sheet1.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml\"/>"
                + "<Override PartName=\"/xl/styles.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml\"/>"
                + "</Types>";
    }

    private static String relacionesRaiz() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">"
                + "<Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument\" Target=\"xl/workbook.xml\"/>"
                + "</Relationships>";
    }

    private static String libro(TablaExportable t) {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<workbook xmlns=\"" + NS + "\" xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\">"
                + "<sheets><sheet name=\"" + xml(nombreHoja(t.titulo)) + "\" sheetId=\"1\" r:id=\"rId1\"/></sheets>"
                + "</workbook>";
    }

    private static String relacionesLibro() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">"
                + "<Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet\" Target=\"worksheets/sheet1.xml\"/>"
                + "<Relationship Id=\"rId2\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles\" Target=\"styles.xml\"/>"
                + "</Relationships>";
    }

    private static String estilos() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<styleSheet xmlns=\"" + NS + "\">"
                + "<numFmts count=\"1\"><numFmt numFmtId=\"164\" formatCode=\"0.0\"/></numFmts>"
                + "<fonts count=\"4\">"
                + "<font><sz val=\"11\"/><name val=\"Calibri\"/></font>"
                + "<font><b/><sz val=\"11\"/><color rgb=\"FFFFFFFF\"/><name val=\"Calibri\"/></font>"
                + "<font><b/><sz val=\"16\"/><color rgb=\"FF1F2A44\"/><name val=\"Calibri\"/></font>"
                + "<font><sz val=\"11\"/><color rgb=\"FF6E6E6E\"/><name val=\"Calibri\"/></font>"
                + "</fonts>"
                + "<fills count=\"3\">"
                + "<fill><patternFill patternType=\"none\"/></fill>"
                + "<fill><patternFill patternType=\"gray125\"/></fill>"
                + "<fill><patternFill patternType=\"solid\"><fgColor rgb=\"FF0A4CC7\"/><bgColor indexed=\"64\"/></patternFill></fill>"
                + "</fills>"
                + "<borders count=\"2\">"
                + "<border><left/><right/><top/><bottom/><diagonal/></border>"
                + "<border><left style=\"thin\"><color rgb=\"FFD9DEEE\"/></left><right style=\"thin\"><color rgb=\"FFD9DEEE\"/></right>"
                + "<top style=\"thin\"><color rgb=\"FFD9DEEE\"/></top><bottom style=\"thin\"><color rgb=\"FFD9DEEE\"/></bottom><diagonal/></border>"
                + "</borders>"
                + "<cellStyleXfs count=\"1\"><xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"0\"/></cellStyleXfs>"
                + "<cellXfs count=\"7\">"
                + "<xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"0\" xfId=\"0\"/>"
                + "<xf numFmtId=\"0\" fontId=\"1\" fillId=\"2\" borderId=\"1\" xfId=\"0\" applyFont=\"1\" applyFill=\"1\" applyBorder=\"1\" applyAlignment=\"1\"><alignment horizontal=\"left\" vertical=\"center\" wrapText=\"1\"/></xf>"
                + "<xf numFmtId=\"0\" fontId=\"2\" fillId=\"0\" borderId=\"0\" xfId=\"0\" applyFont=\"1\"/>"
                + "<xf numFmtId=\"0\" fontId=\"3\" fillId=\"0\" borderId=\"0\" xfId=\"0\" applyFont=\"1\"/>"
                + "<xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"1\" xfId=\"0\" applyBorder=\"1\" applyAlignment=\"1\"><alignment horizontal=\"left\" vertical=\"top\" wrapText=\"1\"/></xf>"
                + "<xf numFmtId=\"164\" fontId=\"0\" fillId=\"0\" borderId=\"1\" xfId=\"0\" applyNumberFormat=\"1\" applyBorder=\"1\" applyAlignment=\"1\"><alignment horizontal=\"left\" vertical=\"top\"/></xf>"
                + "<xf numFmtId=\"1\" fontId=\"0\" fillId=\"0\" borderId=\"1\" xfId=\"0\" applyNumberFormat=\"1\" applyBorder=\"1\" applyAlignment=\"1\"><alignment horizontal=\"left\" vertical=\"top\"/></xf>"
                + "</cellXfs>"
                + "<cellStyles count=\"1\"><cellStyle name=\"Normal\" xfId=\"0\" builtinId=\"0\"/></cellStyles>"
                + "</styleSheet>";
    }

    private static String hoja(TablaExportable t) {
        int columnas = t.encabezados.length;
        int ultimaFila = FILA_ENCABEZADO + t.filas.size();
        StringBuilder sb = new StringBuilder(4096);
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>");
        sb.append("<worksheet xmlns=\"").append(NS).append("\">");
        sb.append("<sheetPr><pageSetUpPr fitToPage=\"1\"/></sheetPr>");
        sb.append("<dimension ref=\"A1:").append(referencia(columnas - 1, ultimaFila)).append("\"/>");
        sb.append("<sheetViews><sheetView showGridLines=\"0\" workbookViewId=\"0\">")
                .append("<pane ySplit=\"").append(FILA_ENCABEZADO).append("\" topLeftCell=\"A")
                .append(FILA_ENCABEZADO + 1).append("\" activePane=\"bottomLeft\" state=\"frozen\"/>")
                .append("<selection pane=\"bottomLeft\" activeCell=\"A").append(FILA_ENCABEZADO + 1)
                .append("\" sqref=\"A").append(FILA_ENCABEZADO + 1).append("\"/>")
                .append("</sheetView></sheetViews>");
        sb.append("<sheetFormatPr defaultRowHeight=\"15\"/>");

        sb.append("<cols>");
        for (int c = 0; c < columnas; c++) {
            sb.append("<col min=\"").append(c + 1).append("\" max=\"").append(c + 1)
                    .append("\" width=\"").append(anchoColumna(t, c)).append("\" customWidth=\"1\"/>");
        }
        sb.append("</cols>");

        sb.append("<sheetData>");
        fila(sb, 1, celdaTexto(0, 1, t.titulo, 2));
        fila(sb, 2, celdaTexto(0, 2, t.subtitulo, 3));
        String pie = "Generado el " + java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                + (t.generadoPor == null ? "" : " por " + t.generadoPor) + "  ·  E.I.P.I";
        fila(sb, 3, celdaTexto(0, 3, pie, 3));

        StringBuilder enc = new StringBuilder();
        for (int c = 0; c < columnas; c++) {
            enc.append(celdaTexto(c, FILA_ENCABEZADO, t.encabezados[c], 1));
        }
        fila(sb, FILA_ENCABEZADO, enc.toString());

        int numeroFila = FILA_ENCABEZADO + 1;
        for (String[] f : t.filas) {
            StringBuilder cel = new StringBuilder();
            for (int c = 0; c < columnas; c++) {
                String v = c < f.length ? f[c] : "";
                Double nota = comoNota(v);
                if (t.esNumerica(c) && v.matches("^\\d{1,9}$")) {
                    cel.append("<c r=\"").append(referencia(c, numeroFila)).append("\" s=\"6\"><v>")
                            .append(v).append("</v></c>");
                } else if (nota != null) {
                    cel.append("<c r=\"").append(referencia(c, numeroFila)).append("\" s=\"5\"><v>")
                            .append(nota).append("</v></c>");
                } else {
                    cel.append(celdaTexto(c, numeroFila, v, 4));
                }
            }
            fila(sb, numeroFila, cel.toString());
            numeroFila++;
        }
        sb.append("</sheetData>");
        sb.append("<autoFilter ref=\"A").append(FILA_ENCABEZADO).append(":")
                .append(referencia(columnas - 1, Math.max(ultimaFila, FILA_ENCABEZADO))).append("\"/>");
        sb.append("<pageMargins left=\"0.5\" right=\"0.5\" top=\"0.6\" bottom=\"0.6\" header=\"0.3\" footer=\"0.3\"/>");
        sb.append("<pageSetup orientation=\"").append(columnas > 5 ? "landscape" : "portrait")
                .append("\" fitToWidth=\"1\" fitToHeight=\"0\"/>");
        sb.append("</worksheet>");
        return sb.toString();
    }

    private static void fila(StringBuilder sb, int numero, String celdas) {
        sb.append("<row r=\"").append(numero).append("\">").append(celdas).append("</row>");
    }

    private static String celdaTexto(int columna, int fila, String texto, int estilo) {
        return "<c r=\"" + referencia(columna, fila) + "\" s=\"" + estilo + "\" t=\"inlineStr\"><is><t xml:space=\"preserve\">"
                + xml(texto) + "</t></is></c>";
    }

    static Double comoNota(String v) {
        if (v != null && v.matches("^\\d{1,2},\\d$")) {
            return Double.valueOf(v.replace(',', '.'));
        }
        return null;
    }

    private static String anchoColumna(TablaExportable t, int c) {
        int max = t.encabezados[c].length();
        for (String[] f : t.filas) {
            if (c < f.length && f[c] != null) {
                for (String linea : f[c].split("\n")) {
                    max = Math.max(max, linea.length());
                }
            }
        }
        double ancho = Math.min(60, Math.max(10, max * 1.15 + 2));
        return String.format(java.util.Locale.US, "%.1f", ancho);
    }

    static String letraColumna(int indice) {
        StringBuilder sb = new StringBuilder();
        int n = indice;
        do {
            sb.insert(0, (char) ('A' + n % 26));
            n = n / 26 - 1;
        } while (n >= 0);
        return sb.toString();
    }

    private static String referencia(int columna, int fila) {
        return letraColumna(columna) + fila;
    }

    private static String nombreHoja(String titulo) {
        String n = Normalizer.normalize(titulo, Normalizer.Form.NFC).replaceAll("[\\[\\]:*?/\\\\]", " ").trim();
        if (n.isEmpty()) {
            n = "Datos";
        }
        return n.length() > 31 ? n.substring(0, 31).trim() : n;
    }

    static String xml(String s) {
        StringBuilder sb = new StringBuilder(s.length() + 16);
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            if (ch == '&') {
                sb.append("&amp;");
            } else if (ch == '<') {
                sb.append("&lt;");
            } else if (ch == '>') {
                sb.append("&gt;");
            } else if (ch == '"') {
                sb.append("&quot;");
            } else if (ch == '\t' || ch == '\n' || ch == '\r' || (ch >= 0x20 && ch != 0xFFFE && ch != 0xFFFF)) {
                sb.append(ch);
            }
        }
        return sb.toString();
    }
}
