package vista.componentes;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LayoutManager;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;
import javax.swing.BorderFactory;
import javax.swing.JPanel;

public class Tarjeta extends JPanel {

    public Tarjeta(LayoutManager layout) {
        super(layout);
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        RoundRectangle2D forma = new RoundRectangle2D.Double(0.5, 0.5, getWidth() - 1.5, getHeight() - 1.5, 20, 20);
        g2.setColor(Color.WHITE);
        g2.fill(forma);
        g2.setColor(Estilo.BORDE);
        g2.draw(forma);
        g2.dispose();
        super.paintComponent(g);
    }
}
