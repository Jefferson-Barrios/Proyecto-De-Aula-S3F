package vista.componentes;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;
import java.awt.event.MouseEvent;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.JPanel;

public class GraficoBarras extends JPanel {

    private static final int ALTO_FILA = 29;

    private Map<String, Integer> datos = new LinkedHashMap<>();
    private final Map<String, Color> colores = new LinkedHashMap<>();
    private Color colorBase = Estilo.AZUL;

    public GraficoBarras() {
        setOpaque(false);
        setToolTipText("");
    }

    public GraficoBarras colorDe(String etiqueta, Color color) {
        colores.put(etiqueta, color);
        return this;
    }

    public GraficoBarras colorBase(Color color) {
        this.colorBase = color;
        return this;
    }

    public void setDatos(Map<String, Integer> datos) {
        this.datos = new LinkedHashMap<>(datos);
        revalidate();
        repaint();
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(390, Math.max(1, datos.size()) * ALTO_FILA + 12);
    }

    @Override
    public String getToolTipText(MouseEvent e) {
        int fila = (e.getY() - 4) / ALTO_FILA;
        if (fila < 0 || fila >= datos.size()) {
            return null;
        }
        Map.Entry<String, Integer> entrada = datos.entrySet().stream().skip(fila).findFirst().orElse(null);
        return entrada == null ? null : entrada.getKey() + ": " + entrada.getValue();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setFont(Estilo.fuentePx(Estilo.REGULAR, 11f));
        FontMetrics fm = g2.getFontMetrics();

        int maximo = 1;
        for (Map.Entry<String, Integer> e : datos.entrySet()) {
            maximo = Math.max(maximo, Math.max(0, e.getValue() == null ? 0 : e.getValue()));
        }
        int anchoEtiqueta = Math.min(142, Math.max(78, getWidth() / 3));
        int xBarra = anchoEtiqueta + 10;
        int anchoMax = Math.max(22, getWidth() - xBarra - 48);

        int y = 4;
        for (Map.Entry<String, Integer> e : datos.entrySet()) {
            int base = y + (ALTO_FILA - fm.getHeight()) / 2 + fm.getAscent();
            g2.setColor(Estilo.TEXTO);
            g2.drawString(ajustar(e.getKey(), anchoEtiqueta - 8, fm), 0, base);

            int altoBarra = 11;
            int yBarra = y + (ALTO_FILA - altoBarra) / 2;
            g2.setColor(new Color(0xEEF2F8));
            g2.fill(new RoundRectangle2D.Double(xBarra, yBarra, anchoMax, altoBarra, 10, 10));
            int valor = Math.max(0, e.getValue() == null ? 0 : e.getValue());
            int largo = valor == 0 ? 0 : Math.max(7, (int) Math.round(anchoMax * valor / (double) maximo));
            if (largo > 0) {
                Color color = colores.getOrDefault(e.getKey(), colorBase);
                g2.setPaint(new GradientPaint(xBarra, yBarra, color.brighter(), xBarra + largo, yBarra, color));
                g2.fill(new RoundRectangle2D.Double(xBarra, yBarra, largo, altoBarra, 10, 10));
            }
            g2.setColor(Estilo.TEXTO);
            g2.drawString(String.valueOf(valor), xBarra + anchoMax + 9, base);
            y += ALTO_FILA;
        }
        if (datos.isEmpty()) {
            g2.setColor(Estilo.GRIS_TEXTO);
            g2.drawString("Sin datos todavía", 0, 22);
        }
        g2.dispose();
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
