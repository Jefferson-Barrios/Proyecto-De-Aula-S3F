package vista.componentes;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.font.TextLayout;
import java.awt.font.TextAttribute;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public class PanelLogin extends JPanel {

    private final CampoRedondeado campoUsuario = new CampoRedondeado(false, "Usuario");
    private final CampoRedondeado campoClave = new CampoRedondeado(true, "Contraseña");
    private final BotonDegradado btnIngresar = new BotonDegradado("Ingresar");
    private final JLabel lblOlvido = new JLabel("¿Olvidó su contraseña?", SwingConstants.CENTER);
    private BufferedImage logo;

    public PanelLogin() {
        setLayout(null);
        setOpaque(false);
        setPreferredSize(new Dimension(Estilo.px(Estilo.DISENO_ANCHO), Estilo.px(Estilo.DISENO_ALTO)));
        cargarLogo();

        campoUsuario.setBounds(Estilo.px(275), Estilo.px(507), Estilo.px(552), Estilo.px(85));
        campoClave.setBounds(Estilo.px(275), Estilo.px(642), Estilo.px(552), Estilo.px(85));

        lblOlvido.setFont(Estilo.fuente(Estilo.REGULAR, 24));
        lblOlvido.setForeground(Estilo.AZUL_LINK);
        lblOlvido.setBorder(BorderFactory.createEmptyBorder());
        lblOlvido.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        lblOlvido.setBounds(Estilo.px(275), Estilo.px(745), Estilo.px(552), Estilo.px(46));
        lblOlvido.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                lblOlvido.setText("<html><u>¿Olvidó su contraseña?</u></html>");
            }

            @Override
            public void mouseExited(MouseEvent e) {
                lblOlvido.setText("¿Olvidó su contraseña?");
            }
        });

        btnIngresar.setBounds(Estilo.px(360), Estilo.px(858), Estilo.px(380), Estilo.px(57));

        add(campoUsuario);
        add(campoClave);
        add(lblOlvido);
        add(btnIngresar);
    }

    private void cargarLogo() {
        URL url = PanelLogin.class.getResource("/recursos/logo.png");
        if (url != null) {
            try {
                logo = ImageIO.read(url);
            } catch (IOException ex) {
                logo = null;
            }
        }
    }

    public Image getLogo() {
        return logo;
    }

    public CampoRedondeado getCampoUsuario() {
        return campoUsuario;
    }

    public CampoRedondeado getCampoClave() {
        return campoClave;
    }

    public BotonDegradado getBtnIngresar() {
        return btnIngresar;
    }

    public JLabel getLblOlvido() {
        return lblOlvido;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        int w = getWidth();

        double rx = Estilo.px(400);
        double ry = Estilo.px(250);
        AffineTransform original = g2.getTransform();
        g2.translate(w / 2.0, Estilo.px(880));
        g2.scale(1, ry / rx);
        g2.setPaint(new RadialGradientPaint(new Point2D.Double(0, 0), (float) rx,
                new float[]{0f, 1f}, new Color[]{new Color(0, 0, 0, 22), new Color(0, 0, 0, 0)}));
        g2.fill(new Ellipse2D.Double(-rx, -rx, 2 * rx, 2 * rx));
        g2.setTransform(original);

        if (logo != null) {
            int lw = Estilo.px(250);
            int lh = (int) Math.round(lw * logo.getHeight() / (double) logo.getWidth());
            g2.drawImage(logo, (w - lw) / 2, Estilo.px(103), lw, lh, null);
        }

        Font titulo = Estilo.fuente(Estilo.NEGRITA, 66)
                .deriveFont(Collections.singletonMap(TextAttribute.TRACKING, 0.12f));
        centrar(g2, "E.I.P.I", titulo, Estilo.AZUL, Estilo.px(325));

        centrar(g2, "Accede a tu cuenta para continuar", Estilo.fuente(Estilo.REGULAR, 27),
                Estilo.GRIS_TEXTO, Estilo.px(389));
        g2.setColor(Estilo.AZUL_LINEA);
        g2.fill(new RoundRectangle2D.Double(Estilo.px(401), Estilo.px(426), Estilo.px(298),
                Math.max(2, Estilo.px(3)), 3, 3));

        Font fPie = Estilo.fuente(Estilo.REGULAR, 22);
        String pie = "Bienvenido a E.I.P.I";
        double tw = new TextLayout(pie, fPie, g2.getFontRenderContext()).getAdvance();
        double x0 = (w - tw) / 2.0;
        centrar(g2, pie, fPie, Estilo.GRIS_PIE, Estilo.px(1031));
        double ly = Estilo.px(1025);
        double largo = Estilo.px(52);
        double hueco = Estilo.px(28);
        g2.setColor(Estilo.GRIS_LINEA);
        g2.setStroke(new java.awt.BasicStroke((float) Math.max(1.0, 1.6 * Estilo.ESCALA)));
        g2.draw(new Line2D.Double(x0 - hueco - largo, ly, x0 - hueco, ly));
        g2.draw(new Line2D.Double(x0 + tw + hueco, ly, x0 + tw + hueco + largo, ly));

        g2.dispose();
    }

    private void centrar(Graphics2D g2, String texto, Font fuente, Color color, double baseline) {
        TextLayout tl = new TextLayout(texto, fuente, g2.getFontRenderContext());
        g2.setColor(color);
        tl.draw(g2, (float) ((getWidth() - tl.getAdvance()) / 2.0), (float) baseline);
    }
}
