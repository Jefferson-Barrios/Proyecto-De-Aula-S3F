package vista.componentes;

import java.awt.Color;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import javax.imageio.ImageIO;

public final class Estilo {

    public static final int DISENO_ANCHO = 1100;
    public static final int DISENO_ALTO = 1137;

    public static final double ESCALA = calcularEscala();

    public static final int REGULAR = 0;
    public static final int MEDIO = 1;
    public static final int NEGRITA = 2;

    public static final Color FONDO = new Color(0xF9FAFE);
    public static final Color BLANCO_CAMPO = new Color(0xFCFDFF);
    public static final Color AZUL = new Color(0x0A4CC7);
    public static final Color AZUL_LINEA = new Color(0x1A4FB5);
    public static final Color AZUL_LINK = new Color(0x0B45B8);
    public static final Color BORDE_CAMPO = new Color(0xB5C0F0);
    public static final Color GRIS_TEXTO = new Color(0x6E6E6E);
    public static final Color GRIS_ICONO = new Color(0x727272);
    public static final Color GRIS_PIE = new Color(0x717372);
    public static final Color GRIS_LINEA = new Color(0x9A9A9A);
    public static final Color DEGRADE_IZQ = new Color(0x2F3581);
    public static final Color DEGRADE_DER = new Color(0x1F57C6);

    public static final Color TEXTO = new Color(0x1F2A44);
    public static final Color BORDE = new Color(0xE3E8F5);
    public static final Color SELECCION = new Color(0xE6EDFF);
    public static final Color ENCABEZADO_TABLA = new Color(0xEEF2FF);
    public static final Color ROJO = new Color(0xC0392B);
    public static final Color VERDE = new Color(0x1E8E5A);

    private static final Font F_REGULAR = cargar("Poppins-Regular.ttf", Font.PLAIN);
    private static final Font F_MEDIO = cargar("Poppins-Medium.ttf", Font.PLAIN);
    private static final Font F_NEGRITA = cargar("Poppins-Bold.ttf", Font.BOLD);

    private Estilo() {
    }

    private static double calcularEscala() {
        try {
            int alto = Toolkit.getDefaultToolkit().getScreenSize().height;
            double e = (alto - 140) / (double) DISENO_ALTO;
            return Math.max(0.45, Math.min(0.62, e));
        } catch (Exception ex) {
            return 0.6;
        }
    }

    public static int px(double v) {
        return (int) Math.round(v * ESCALA);
    }

    public static Font fuente(int peso, double tamano) {
        Font base = peso == NEGRITA ? F_NEGRITA : (peso == MEDIO ? F_MEDIO : F_REGULAR);
        return base.deriveFont((float) (tamano * ESCALA));
    }

    public static Font fuentePx(int peso, float tamano) {
        Font base = peso == NEGRITA ? F_NEGRITA : (peso == MEDIO ? F_MEDIO : F_REGULAR);
        return base.deriveFont(tamano);
    }

    private static BufferedImage logo;

    public static synchronized BufferedImage logo() {
        if (logo == null) {
            URL url = Estilo.class.getResource("/recursos/logo.png");
            if (url != null) {
                try {
                    logo = ImageIO.read(url);
                } catch (IOException ex) {
                    logo = null;
                }
            }
        }
        return logo;
    }

    private static Font cargar(String archivo, int estiloRespaldo) {
        try (InputStream in = Estilo.class.getResourceAsStream("/recursos/fuentes/" + archivo)) {
            if (in != null) {
                Font f = Font.createFont(Font.TRUETYPE_FONT, in);
                GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(f);
                return f;
            }
        } catch (Exception ex) {

        }
        return new Font("SansSerif", estiloRespaldo, 12);
    }
}
