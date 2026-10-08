package vista.componentes;

import java.awt.Color;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.io.InputStream;

/** Colores, fuentes y escala del diseño del login. */
public final class Estilo {

    /** Tamaño del diseño original (en "unidades de diseño"). */
    public static final int DISENO_ANCHO = 1100;
    public static final int DISENO_ALTO = 1137;

    /** Escala que se ajusta sola a la altura de la pantalla. */
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

    /** Convierte una medida del diseño a píxeles reales. */
    public static int px(double v) {
        return (int) Math.round(v * ESCALA);
    }

    /** Fuente Poppins (REGULAR, MEDIO o NEGRITA) con tamaño en unidades de diseño. */
    public static Font fuente(int peso, double tamano) {
        Font base = peso == NEGRITA ? F_NEGRITA : (peso == MEDIO ? F_MEDIO : F_REGULAR);
        return base.deriveFont((float) (tamano * ESCALA));
    }

    private static Font cargar(String archivo, int estiloRespaldo) {
        try (InputStream in = Estilo.class.getResourceAsStream("/recursos/fuentes/" + archivo)) {
            if (in != null) {
                Font f = Font.createFont(Font.TRUETYPE_FONT, in);
                GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(f);
                return f;
            }
        } catch (Exception ex) {
            // si falla, se usa la fuente de respaldo
        }
        return new Font("SansSerif", estiloRespaldo, 12);
    }
}
