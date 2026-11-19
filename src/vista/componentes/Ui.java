package vista.componentes;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.text.JTextComponent;

public final class Ui {

    private Ui() {
    }

    public static JLabel titulo(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(Estilo.fuentePx(Estilo.NEGRITA, 24f));
        l.setForeground(Estilo.TEXTO);
        return l;
    }

    public static JLabel subtitulo(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(Estilo.fuentePx(Estilo.REGULAR, 13f));
        l.setForeground(Estilo.GRIS_TEXTO);
        return l;
    }

    public static JLabel etiqueta(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(Estilo.fuentePx(Estilo.MEDIO, 12f));
        l.setForeground(Estilo.TEXTO);
        return l;
    }

    private static Border bordeCampo(boolean enfocado) {
        return BorderFactory.createCompoundBorder(
                new BordeRedondeado(enfocado ? Estilo.AZUL : Estilo.BORDE_CAMPO, 14, enfocado ? 2 : 1, 0, 0),
                BorderFactory.createEmptyBorder(8, 12, 8, 12));
    }

    public static void estilizarCampo(JTextComponent f) {
        f.setFont(Estilo.fuentePx(Estilo.REGULAR, 14f));
        f.setForeground(Estilo.TEXTO);
        f.setCaretColor(Estilo.AZUL);
        f.setBackground(Color.WHITE);
        f.setBorder(bordeCampo(false));
        f.setPreferredSize(new Dimension(220, 42));
        f.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                f.setBorder(bordeCampo(true));
            }

            @Override
            public void focusLost(FocusEvent e) {
                f.setBorder(bordeCampo(false));
            }
        });
    }

    public static JTextField campo() {
        JTextField f = new JTextField();
        estilizarCampo(f);
        return f;
    }

    public static JPasswordField campoClave() {
        JPasswordField f = new JPasswordField();
        estilizarCampo(f);
        return f;
    }

    public static <T> JComboBox<T> combo() {
        JComboBox<T> c = new JComboBox<>();
        c.setFont(Estilo.fuentePx(Estilo.REGULAR, 14f));
        c.setBackground(Color.WHITE);
        c.setPreferredSize(new Dimension(220, 42));
        return c;
    }

    public static JPanel conEtiqueta(String texto, JComponent componente) {
        JPanel p = new JPanel(new BorderLayout(0, 6));
        p.setOpaque(false);
        p.add(etiqueta(texto), BorderLayout.NORTH);
        p.add(componente, BorderLayout.CENTER);
        return p;
    }

    public static JPanel rejilla(int columnas, JComponent... celdas) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.anchor = GridBagConstraints.NORTHWEST;
        g.weightx = 1;
        g.insets = new Insets(8, 8, 8, 8);
        for (int i = 0; i < celdas.length; i++) {
            g.gridx = i % columnas;
            g.gridy = i / columnas;
            p.add(celdas[i], g);
        }
        return p;
    }

    public static void estilizarTabla(JTable t) {
        t.setRowHeight(38);
        t.setFont(Estilo.fuentePx(Estilo.REGULAR, 13f));
        t.setForeground(Estilo.TEXTO);
        t.setShowVerticalLines(false);
        t.setShowHorizontalLines(true);
        t.setGridColor(new Color(0xEEF1FA));
        t.setIntercellSpacing(new Dimension(0, 1));
        t.setSelectionBackground(Estilo.SELECCION);
        t.setSelectionForeground(Estilo.TEXTO);
        t.setFillsViewportHeight(true);
        t.setBackground(Color.WHITE);
        t.setDefaultRenderer(Object.class, new RenderCelda());

        JTableHeader h = t.getTableHeader();
        h.setReorderingAllowed(false);
        h.setPreferredSize(new Dimension(100, 40));
        h.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tabla, Object valor, boolean sel,
                                                           boolean foco, int fila, int col) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(tabla, valor, false, false, fila, col);
                l.setOpaque(true);
                l.setBackground(Estilo.ENCABEZADO_TABLA);
                l.setForeground(Estilo.TEXTO);
                l.setFont(Estilo.fuentePx(Estilo.MEDIO, 12f));
                l.setHorizontalAlignment(SwingConstants.LEFT);
                l.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
                return l;
            }
        });
    }

    public static JScrollPane scroll(JTable t) {
        JScrollPane sp = new JScrollPane(t);
        sp.setColumnHeaderView(t.getTableHeader());
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.getViewport().setBackground(Color.WHITE);
        return sp;
    }

    private static class RenderCelda extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable tabla, Object valor, boolean sel,
                                                       boolean foco, int fila, int col) {
            JLabel l = (JLabel) super.getTableCellRendererComponent(tabla, valor, sel, false, fila, col);
            l.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
            l.setForeground(Estilo.TEXTO);
            l.setFont(Estilo.fuentePx(Estilo.REGULAR, 13f));

            l.setToolTipText(valor == null || String.valueOf(valor).isEmpty() ? null : String.valueOf(valor));
            String texto = valor instanceof String ? (String) valor : "";
            Color color = null;
            switch (texto) {
                case "ACTIVO":
                case "ABIERTO":
                case "PRESENTE":
                case "SIN RIESGO":
                case "ADECUADO":
                case "VA APROBANDO":
                case "REALIZADA":
                    color = Estilo.VERDE;
                    break;
                case "INACTIVO":
                case "CERRADO":
                case "AUSENTE":
                case "ALTO":
                case "VA PERDIENDO":
                case "CANCELADA":
                    color = Estilo.ROJO;
                    break;
                case "MEDIO":
                case "EXCUSA":
                case "TARDE":
                case "PENDIENTE":
                case "EN SEGUIMIENTO":
                    color = new Color(0xC47A00);
                    break;
                case "AGENDADA":
                    color = Estilo.AZUL;
                    break;
                case "PREVENTIVA":
                    color = new Color(0x2E7DD1);
                    break;
                case "CERRADA":
                    color = new Color(0x7A7A7A);
                    break;
                case "BAJO":
                    color = new Color(0x9A8200);
                    break;
                default:
                    break;
            }
            if (color != null) {
                l.setForeground(color);
                l.setFont(Estilo.fuentePx(Estilo.MEDIO, 13f));
            }
            return l;
        }
    }
}
