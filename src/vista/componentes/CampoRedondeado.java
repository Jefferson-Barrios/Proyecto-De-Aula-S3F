package vista.componentes;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.text.JTextComponent;

public class CampoRedondeado extends JPanel {

    private final boolean esClave;
    private final JTextField campo;
    private boolean enfocado = false;
    private boolean claveVisible = false;
    private char echoOriginal = '\u2022';

    public CampoRedondeado(boolean esClave, String placeholder) {
        this.esClave = esClave;
        setOpaque(false);
        setLayout(null);

        campo = esClave ? new CampoClave(placeholder) : new CampoTexto(placeholder);
        if (esClave) {
            echoOriginal = ((JPasswordField) campo).getEchoChar();
        }
        campo.setOpaque(false);
        campo.setBorder(null);
        campo.setFont(Estilo.fuente(Estilo.REGULAR, 27));
        campo.setForeground(new Color(0x2B2B2B));
        campo.setCaretColor(Estilo.AZUL);
        campo.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                enfocado = true;
                repaint();
            }

            @Override
            public void focusLost(FocusEvent e) {
                enfocado = false;
                repaint();
            }
        });
        add(campo);

        setPreferredSize(new Dimension(Estilo.px(552), Estilo.px(85)));

        MouseAdapter raton = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (CampoRedondeado.this.esClave && sobreOjo(e.getPoint())) {
                    alternarVisibilidad();
                } else {
                    campo.requestFocusInWindow();
                }
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                boolean mano = CampoRedondeado.this.esClave && sobreOjo(e.getPoint());
                setCursor(Cursor.getPredefinedCursor(mano ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
            }
        };
        addMouseListener(raton);
        addMouseMotionListener(raton);
    }

    @Override
    public void doLayout() {
        int izq = Estilo.px(117);
        int der = esClave ? Estilo.px(95) : Estilo.px(30);
        campo.setBounds(izq, Estilo.px(10), Math.max(10, getWidth() - izq - der), getHeight() - Estilo.px(20));
    }

    private Rectangle rectOjo() {
        int w = Estilo.px(36);
        int h = Estilo.px(24);
        return new Rectangle(getWidth() - Estilo.px(33) - w, (getHeight() - h) / 2, w, h);
    }

    private boolean sobreOjo(Point p) {
        Rectangle r = rectOjo();
        int m = Estilo.px(12);
        r.grow(m, m);
        return r.contains(p);
    }

    private void alternarVisibilidad() {
        claveVisible = !claveVisible;
        ((JPasswordField) campo).setEchoChar(claveVisible ? (char) 0 : echoOriginal);
        repaint();
        campo.requestFocusInWindow();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        int w = getWidth();
        int h = getHeight();
        float grosor = (float) Math.max(1.2, 2 * Estilo.ESCALA);

        RoundRectangle2D forma = new RoundRectangle2D.Double(grosor / 2, grosor / 2,
                w - grosor, h - grosor, Estilo.px(56), Estilo.px(56));
        g2.setColor(Estilo.BLANCO_CAMPO);
        g2.fill(forma);
        g2.setColor(enfocado ? Estilo.AZUL : Estilo.BORDE_CAMPO);
        g2.setStroke(new BasicStroke(grosor));
        g2.draw(forma);

        double s = Estilo.px(34);
        double ix = Estilo.px(40);
        double iy = (h - s) / 2.0;
        Graphics2D gi = (Graphics2D) g2.create();
        if (esClave) {
            Iconos.candado(gi, ix, iy, s, Estilo.AZUL, Estilo.BLANCO_CAMPO);
        } else {
            Iconos.usuario(gi, ix, iy, s, Estilo.AZUL);
        }
        gi.dispose();

        int sx = Estilo.px(95);
        int medio = h / 2;
        g2.setColor(Estilo.AZUL);
        g2.setStroke(new BasicStroke(grosor));
        g2.drawLine(sx, medio - Estilo.px(17), sx, medio + Estilo.px(17));

        if (esClave) {
            Rectangle r = rectOjo();
            Graphics2D go = (Graphics2D) g2.create();
            Iconos.ojo(go, r.x, r.y, r.width, r.height, Estilo.GRIS_ICONO, Estilo.BLANCO_CAMPO, claveVisible);
            go.dispose();
        }
        g2.dispose();
    }

    public String getTexto() {
        return esClave ? new String(((JPasswordField) campo).getPassword()) : campo.getText();
    }

    public void limpiar() {
        campo.setText("");
    }

    public JTextField getCampo() {
        return campo;
    }

    public void addActionListener(ActionListener l) {
        campo.addActionListener(l);
    }

    private static void pintarPlaceholder(JTextComponent c, Graphics g, String texto) {
        if (c.getDocument().getLength() > 0) {
            return;
        }
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setColor(Estilo.GRIS_TEXTO);
        g2.setFont(c.getFont());
        FontMetrics fm = g2.getFontMetrics();
        int y = (c.getHeight() - fm.getHeight()) / 2 + fm.getAscent();
        g2.drawString(texto, c.getInsets().left, y);
        g2.dispose();
    }

    private static class CampoTexto extends JTextField {
        private final String placeholder;

        CampoTexto(String placeholder) {
            this.placeholder = placeholder;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            pintarPlaceholder(this, g, placeholder);
        }
    }

    private static class CampoClave extends JPasswordField {
        private final String placeholder;

        CampoClave(String placeholder) {
            this.placeholder = placeholder;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            pintarPlaceholder(this, g, placeholder);
        }
    }
}
