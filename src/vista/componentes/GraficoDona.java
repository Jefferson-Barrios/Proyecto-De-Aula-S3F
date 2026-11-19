package vista.componentes;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.JPanel;

public class GraficoDona extends JPanel {

    private static final Color[] PALETA = {
        Estilo.AZUL, Estilo.VERDE, new Color(0xE2A02B),
        Estilo.ROJO, new Color(0x7B61A8), new Color(0x54A6A0)
    };

    private final Map<String, Integer> datos = new LinkedHashMap<>();
    private final Map<String, Color> colores = new LinkedHashMap<>();

    public GraficoDona() {
        setOpaque(false);
    }

    public GraficoDona colorDe(String etiqueta, Color color) {
        colores.put(etiqueta, color);
        return this;
    }

    public void setDatos(Map<String, Integer> nuevosDatos) {
        datos.clear();
        if (nuevosDatos != null) {
            datos.putAll(nuevosDatos);
        }
        repaint();
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(390, 184);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int total = 0;
        for (Integer valor : datos.values()) {
            total += Math.max(0, valor == null ? 0 : valor);
        }

        int diametro = Math.max(72, Math.min(142, Math.min(getHeight() - 22, getWidth() * 2 / 5)));
        int x = 8;
        int y = (getHeight() - diametro) / 2;
        int centroX = x + diametro / 2;
        int centroY = y + diametro / 2;

        if (total == 0) {
            g2.setColor(new Color(0xE8EDF6));
            g2.fill(new Ellipse2D.Double(x, y, diametro, diametro));
        } else {
            double acumulado = 0;
            int indice = 0;
            for (Map.Entry<String, Integer> entrada : datos.entrySet()) {
                int valor = Math.max(0, entrada.getValue() == null ? 0 : entrada.getValue());
                if (valor == 0) {
                    indice++;
                    continue;
                }
                double angulo = valor * 360.0 / total;
                g2.setColor(color(entrada.getKey(), indice));
                g2.fill(new Arc2D.Double(x, y, diametro, diametro, 90 - acumulado, -angulo, Arc2D.PIE));
                acumulado += angulo;
                indice++;
            }
        }

        int hueco = (int) (diametro * 0.64);
        g2.setColor(Color.WHITE);
        g2.fill(new Ellipse2D.Double(centroX - hueco / 2.0, centroY - hueco / 2.0, hueco, hueco));

        g2.setColor(Estilo.TEXTO);
        g2.setFont(Estilo.fuentePx(Estilo.NEGRITA, 21f));
        String totalTexto = String.valueOf(total);
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(totalTexto, centroX - fm.stringWidth(totalTexto) / 2, centroY + 3);
        g2.setColor(Estilo.GRIS_TEXTO);
        g2.setFont(Estilo.fuentePx(Estilo.MEDIO, 9f));
        String etiquetaTotal = "TOTAL";
        fm = g2.getFontMetrics();
        g2.drawString(etiquetaTotal, centroX - fm.stringWidth(etiquetaTotal) / 2, centroY + 19);

        pintarLeyenda(g2, x + diametro + 18, total);
        g2.dispose();
    }

    private void pintarLeyenda(Graphics2D g2, int x, int total) {
        int altoFila = datos.size() > 5 ? 22 : 27;
        int altoTotal = datos.size() * altoFila;
        int y = Math.max(14, (getHeight() - altoTotal) / 2);
        int indice = 0;
        FontMetrics fm;

        for (Map.Entry<String, Integer> entrada : datos.entrySet()) {
            int valor = Math.max(0, entrada.getValue() == null ? 0 : entrada.getValue());
            int porcentaje = total == 0 ? 0 : (int) Math.round(valor * 100.0 / total);
            String cantidad = valor + "  " + porcentaje + "%";

            g2.setColor(color(entrada.getKey(), indice));
            g2.fillRoundRect(x, y + 4, 9, 9, 4, 4);
            g2.setFont(Estilo.fuentePx(Estilo.REGULAR, 11f));
            fm = g2.getFontMetrics();
            int anchoCantidad = fm.stringWidth(cantidad);
            int anchoEtiqueta = Math.max(25, getWidth() - x - anchoCantidad - 18);
            String etiqueta = ajustar(entrada.getKey(), anchoEtiqueta, fm);
            g2.setColor(Estilo.TEXTO);
            g2.drawString(etiqueta, x + 16, y + 13);
            g2.setColor(Estilo.GRIS_TEXTO);
            g2.drawString(cantidad, getWidth() - anchoCantidad - 4, y + 13);

            y += altoFila;
            indice++;
        }
    }

    private Color color(String etiqueta, int indice) {
        return colores.getOrDefault(etiqueta, PALETA[indice % PALETA.length]);
    }

    private static String ajustar(String texto, int ancho, FontMetrics fm) {
        if (fm.stringWidth(texto) <= ancho) {
            return texto;
        }
        String resultado = texto;
        while (!resultado.isEmpty() && fm.stringWidth(resultado + "...") > ancho) {
            resultado = resultado.substring(0, resultado.length() - 1);
        }
        return resultado.isEmpty() ? "" : resultado + "...";
    }
}
