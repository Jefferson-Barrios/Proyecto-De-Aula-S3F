package vista.componentes;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;
import javax.swing.border.AbstractBorder;

public class BordeRedondeado extends AbstractBorder {

    private final Color color;
    private final int radio;
    private final int grosor;
    private final int padV;
    private final int padH;

    public BordeRedondeado(Color color, int radio, int grosor, int padV, int padH) {
        this.color = color;
        this.radio = radio;
        this.grosor = grosor;
        this.padV = padV;
        this.padH = padH;
    }

    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(color);
        g2.setStroke(new BasicStroke(grosor));
        g2.draw(new RoundRectangle2D.Double(x + grosor / 2.0, y + grosor / 2.0, w - grosor, h - grosor, radio, radio));
        g2.dispose();
    }

    @Override
    public Insets getBorderInsets(Component c) {
        return new Insets(padV + grosor, padH + grosor, padV + grosor, padH + grosor);
    }

    @Override
    public Insets getBorderInsets(Component c, Insets insets) {
        insets.set(padV + grosor, padH + grosor, padV + grosor, padH + grosor);
        return insets;
    }
}
