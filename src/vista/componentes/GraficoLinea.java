package vista.componentes;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JPanel;

public class GraficoLinea extends JPanel {

    private final Map<String, Integer> datos = new LinkedHashMap<>();
    private Color colorLinea = Estilo.AZUL;

    public GraficoLinea() {
        setOpaque(false);
    }

    public GraficoLinea colorLinea(Color color) {
        colorLinea = color;
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

        int izquierda = 36;
        int derecha = 12;
        int arriba = 16;
        int abajo = 34;
        int ancho = Math.max(1, getWidth() - izquierda - derecha);
        int alto = Math.max(1, getHeight() - arriba - abajo);

        int maximo = 0;
        for (Integer valor : datos.values()) {
            maximo = Math.max(maximo, Math.max(0, valor == null ? 0 : valor));
        }
        maximo = Math.max(1, maximo);

        g2.setFont(Estilo.fuentePx(Estilo.REGULAR, 10f));
        FontMetrics fm = g2.getFontMetrics();
        for (int i = 0; i <= 4; i++) {
            int y = arriba + alto - alto * i / 4;
            int valor = maximo * i / 4;
            String texto = String.valueOf(valor);
            g2.setColor(new Color(0xE9EDF5));
            g2.setStroke(new BasicStroke(1f));
            g2.drawLine(izquierda, y, izquierda + ancho, y);
            g2.setColor(Estilo.GRIS_TEXTO);
            g2.drawString(texto, izquierda - fm.stringWidth(texto) - 7, y + fm.getAscent() / 2 - 1);
        }

        List<Map.Entry<String, Integer>> puntos = new ArrayList<>(datos.entrySet());
        if (puntos.isEmpty()) {
            g2.setColor(Estilo.GRIS_TEXTO);
            g2.drawString("Sin datos todavía", izquierda + 8, arriba + alto / 2);
            g2.dispose();
            return;
        }

        double paso = puntos.size() == 1 ? 0 : ancho / (double) (puntos.size() - 1);
        Path2D.Double linea = new Path2D.Double();
        Path2D.Double area = new Path2D.Double();
        for (int i = 0; i < puntos.size(); i++) {
            int valor = Math.max(0, puntos.get(i).getValue() == null ? 0 : puntos.get(i).getValue());
            double x = izquierda + paso * i;
            double y = arriba + alto - alto * (valor / (double) maximo);
            if (i == 0) {
                linea.moveTo(x, y);
                area.moveTo(x, arriba + alto);
                area.lineTo(x, y);
            } else {
                linea.lineTo(x, y);
                area.lineTo(x, y);
            }
        }
        area.lineTo(izquierda + paso * (puntos.size() - 1), arriba + alto);
        area.closePath();

        g2.setColor(new Color(colorLinea.getRed(), colorLinea.getGreen(), colorLinea.getBlue(), 28));
        g2.fill(area);
        g2.setColor(colorLinea);
        g2.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.draw(linea);

        for (int i = 0; i < puntos.size(); i++) {
            Map.Entry<String, Integer> punto = puntos.get(i);
            int valor = Math.max(0, punto.getValue() == null ? 0 : punto.getValue());
            double x = izquierda + paso * i;
            double y = arriba + alto - alto * (valor / (double) maximo);
            g2.setColor(Color.WHITE);
            g2.fill(new Ellipse2D.Double(x - 5, y - 5, 10, 10));
            g2.setColor(colorLinea);
            g2.fill(new Ellipse2D.Double(x - 3, y - 3, 6, 6));

            String etiqueta = etiquetaCorta(punto.getKey());
            g2.setFont(Estilo.fuentePx(Estilo.REGULAR, 9f));
            fm = g2.getFontMetrics();
            g2.setColor(Estilo.GRIS_TEXTO);
            g2.drawString(etiqueta, (int) x - fm.stringWidth(etiqueta) / 2, arriba + alto + 20);

            g2.setFont(Estilo.fuentePx(Estilo.MEDIO, 10f));
            fm = g2.getFontMetrics();
            String valorTexto = String.valueOf(valor);
            g2.setColor(Estilo.TEXTO);
            g2.drawString(valorTexto, (int) x - fm.stringWidth(valorTexto) / 2, (int) y - 9);
        }
        g2.dispose();
    }

    private static String etiquetaCorta(String etiqueta) {
        int espacio = etiqueta.indexOf(' ');
        String mes = espacio < 0 ? etiqueta : etiqueta.substring(0, espacio);
        return mes.length() <= 3 ? mes : mes.substring(0, 3);
    }
}
