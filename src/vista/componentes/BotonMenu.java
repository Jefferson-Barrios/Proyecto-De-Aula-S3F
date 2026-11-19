package vista.componentes;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;
import javax.swing.JButton;

public class BotonMenu extends JButton {

    private boolean activo = false;

    public BotonMenu(String texto) {
        super(texto);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setOpaque(false);
        setHorizontalAlignment(LEFT);
        setFont(Estilo.fuentePx(Estilo.MEDIO, 14f));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setPreferredSize(new Dimension(200, 38));
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        int w = getWidth();
        int h = getHeight();
        RoundRectangle2D forma = new RoundRectangle2D.Double(0, 0, w, h, 14, 14);
        if (activo) {
            g2.setColor(Estilo.SELECCION);
            g2.fill(forma);
            g2.setColor(Estilo.AZUL);
            g2.fill(new RoundRectangle2D.Double(0, 10, 4, h - 20, 4, 4));
        } else if (getModel().isRollover()) {
            g2.setColor(new Color(0xF2F5FD));
            g2.fill(forma);
        }
        g2.setFont(getFont());
        FontMetrics fm = g2.getFontMetrics();
        g2.setColor(activo ? Estilo.AZUL : Estilo.TEXTO);
        g2.drawString(getText(), 20, (h - fm.getHeight()) / 2 + fm.getAscent());
        g2.dispose();
    }
}
