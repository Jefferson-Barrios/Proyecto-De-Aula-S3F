package vista.componentes;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;
import javax.swing.JButton;

public class BotonSimple extends JButton {

    public enum Tipo { PRIMARIO, SECUNDARIO, PELIGRO }

    private final Tipo tipo;

    public BotonSimple(String texto, Tipo tipo) {
        super(texto);
        this.tipo = tipo;
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setOpaque(false);
        setFont(Estilo.fuentePx(Estilo.MEDIO, 13f));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    @Override
    public Dimension getPreferredSize() {
        FontMetrics fm = getFontMetrics(getFont());
        return new Dimension(fm.stringWidth(getText()) + 44, 40);
    }

    private static Color mezclar(Color a, Color b, double t) {
        return new Color(
                (int) (a.getRed() + (b.getRed() - a.getRed()) * t),
                (int) (a.getGreen() + (b.getGreen() - a.getGreen()) * t),
                (int) (a.getBlue() + (b.getBlue() - a.getBlue()) * t));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        int w = getWidth();
        int h = getHeight();
        RoundRectangle2D forma = new RoundRectangle2D.Double(0.5, 0.5, w - 1.5, h - 1.5, h, h);

        Color texto;
        double mezcla = 0;
        if (getModel().isPressed()) {
            mezcla = 0.14;
        } else if (getModel().isRollover()) {
            mezcla = 0.07;
        }

        if (tipo == Tipo.PRIMARIO) {
            Color izq = mezclar(Estilo.DEGRADE_IZQ, Color.BLACK, mezcla);
            Color der = mezclar(Estilo.DEGRADE_DER, Color.BLACK, mezcla);
            g2.setPaint(new GradientPaint(0, 0, izq, w, 0, der));
            g2.fill(forma);
            texto = Color.WHITE;
        } else if (tipo == Tipo.PELIGRO) {
            g2.setColor(mezclar(new Color(0xFDECEC), Color.BLACK, mezcla));
            g2.fill(forma);
            g2.setColor(new Color(0xF0B4AE));
            g2.draw(forma);
            texto = Estilo.ROJO;
        } else {
            g2.setColor(mezclar(Color.WHITE, Color.BLACK, mezcla));
            g2.fill(forma);
            g2.setColor(Estilo.BORDE_CAMPO);
            g2.draw(forma);
            texto = Estilo.AZUL;
        }
        if (!isEnabled()) {
            g2.setColor(new Color(255, 255, 255, 150));
            g2.fill(forma);
            texto = new Color(texto.getRed(), texto.getGreen(), texto.getBlue(), 110);
        }

        g2.setFont(getFont());
        FontMetrics fm = g2.getFontMetrics();
        int x = (w - fm.stringWidth(getText())) / 2;
        int y = (h - fm.getHeight()) / 2 + fm.getAscent();
        g2.setColor(texto);
        g2.drawString(getText(), x, y);
        g2.dispose();
    }
}
