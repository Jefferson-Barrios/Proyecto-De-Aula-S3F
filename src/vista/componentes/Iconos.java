package vista.componentes;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Arc2D;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;

/** Íconos dibujados con Java2D (no necesitan archivos de imagen). */
public final class Iconos {

    private Iconos() {
    }

    public static void usuario(Graphics2D g, double x, double y, double s, Color c) {
        g.setColor(c);
        g.fill(new Ellipse2D.Double(x + 0.27 * s, y, 0.46 * s, 0.46 * s));
        Area cuerpo = new Area(new RoundRectangle2D.Double(x + 0.06 * s, y + 0.56 * s, 0.88 * s, 0.9 * s, 0.9 * s, 0.9 * s));
        cuerpo.intersect(new Area(new Rectangle2D.Double(x, y + 0.5 * s, s, 0.5 * s)));
        g.fill(cuerpo);
    }

    public static void candado(Graphics2D g, double x, double y, double s, Color c, Color agujero) {
        g.setColor(c);
        g.setStroke(new BasicStroke((float) (0.13 * s), BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));
        g.draw(new Arc2D.Double(x + 0.24 * s, y + 0.06 * s, 0.52 * s, 0.64 * s, 0, 180, Arc2D.OPEN));
        g.fill(new RoundRectangle2D.Double(x + 0.1 * s, y + 0.42 * s, 0.8 * s, 0.58 * s, 0.16 * s, 0.16 * s));
        g.setColor(agujero);
        g.fill(new Ellipse2D.Double(x + 0.43 * s, y + 0.57 * s, 0.14 * s, 0.14 * s));
        g.fill(new Rectangle2D.Double(x + 0.465 * s, y + 0.64 * s, 0.07 * s, 0.16 * s));
    }

    public static void ojo(Graphics2D g, double x, double y, double w, double h,
                           Color c, Color fondo, boolean tachado) {
        double cx = x + w / 2, cy = y + h / 2;
        Path2D forma = new Path2D.Double();
        forma.moveTo(x, cy);
        forma.quadTo(cx, y - h * 0.5, x + w, cy);
        forma.quadTo(cx, y + h * 1.5, x, cy);
        forma.closePath();
        g.setColor(c);
        g.fill(forma);
        double d1 = h * 0.62;
        g.setColor(fondo);
        g.fill(new Ellipse2D.Double(cx - d1 / 2, cy - d1 / 2, d1, d1));
        double d2 = h * 0.36;
        g.setColor(c);
        g.fill(new Ellipse2D.Double(cx - d2 / 2, cy - d2 / 2, d2, d2));
        if (tachado) {
            Line2D raya = new Line2D.Double(x + w * 0.14, y + h * 1.05, x + w * 0.86, y - h * 0.05);
            g.setColor(fondo);
            g.setStroke(new BasicStroke((float) (h * 0.36), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.draw(raya);
            g.setColor(c);
            g.setStroke(new BasicStroke((float) (h * 0.16), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.draw(raya);
        }
    }

    public static void flecha(Graphics2D g, double x, double y, double w, double h, Color c) {
        double cy = y + h / 2;
        g.setColor(c);
        g.fill(new RoundRectangle2D.Double(x, cy - h * 0.09, w * 0.74, h * 0.18, h * 0.18, h * 0.18));
        Path2D punta = new Path2D.Double();
        punta.moveTo(x + w * 0.6, y);
        punta.lineTo(x + w, cy);
        punta.lineTo(x + w * 0.6, y + h);
        punta.closePath();
        g.fill(punta);
    }
}
