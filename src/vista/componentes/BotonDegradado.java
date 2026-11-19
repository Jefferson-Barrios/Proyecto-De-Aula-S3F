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

public class BotonDegradado extends JButton {

    public BotonDegradado(String texto) {
        super(texto);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setOpaque(false);
        setForeground(Color.WHITE);
        setFont(Estilo.fuente(Estilo.NEGRITA, 28));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setPreferredSize(new Dimension(Estilo.px(380), Estilo.px(57)));
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
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        int w = getWidth();
        int h = getHeight();

        Color izq = Estilo.DEGRADE_IZQ;
        Color der = Estilo.DEGRADE_DER;
        if (!isEnabled()) {
            izq = mezclar(izq, Color.LIGHT_GRAY, 0.55);
            der = mezclar(der, Color.LIGHT_GRAY, 0.55);
        } else if (getModel().isPressed()) {
            izq = mezclar(izq, Color.BLACK, 0.18);
            der = mezclar(der, Color.BLACK, 0.18);
        } else if (getModel().isRollover()) {
            izq = mezclar(izq, Color.WHITE, 0.12);
            der = mezclar(der, Color.WHITE, 0.12);
        }

        RoundRectangle2D forma = new RoundRectangle2D.Double(0.5, 0.5, w - 1.5, h - 1.5, h * 0.62, h * 0.62);
        g2.setPaint(new GradientPaint(0, 0, izq, w, 0, der));
        g2.fill(forma);
        g2.setColor(new Color(255, 255, 255, 70));
        g2.draw(forma);

        g2.setFont(getFont());
        FontMetrics fm = g2.getFontMetrics();
        String t = getText();
        int tw = fm.stringWidth(t);
        int aw = Estilo.px(48);
        int ah = Estilo.px(22);
        int hueco = Estilo.px(22);
        int x = (w - (tw + hueco + aw)) / 2;
        int ty = (h - fm.getHeight()) / 2 + fm.getAscent();
        g2.setColor(Color.WHITE);
        g2.drawString(t, x, ty);
        Iconos.flecha(g2, x + tw + hueco, (h - ah) / 2.0, aw, ah, Color.WHITE);
        g2.dispose();
    }
}
