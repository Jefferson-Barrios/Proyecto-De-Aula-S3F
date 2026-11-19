package vista.componentes;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.plaf.basic.BasicHTML;
import javax.swing.text.View;

public class BarraMensaje extends JPanel {

    private final JLabel etiqueta = new JLabel();

    public BarraMensaje() {
        super(new BorderLayout());
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));
        etiqueta.setOpaque(true);
        etiqueta.setFont(Estilo.fuentePx(Estilo.MEDIO, 13f));
        etiqueta.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
        add(etiqueta, BorderLayout.CENTER);
        setVisible(false);
    }

    public void exito(String texto) {
        mostrar(texto, new Color(0xE7F7EF), new Color(0x1E7A4F));
    }

    public void error(String texto) {
        mostrar(texto, new Color(0xFDECEC), new Color(0xB3261E));
    }

    public void limpiar() {
        etiqueta.setText("");
        setVisible(false);
    }

    private void mostrar(String texto, Color fondo, Color letra) {
        etiqueta.setText("<html>" + texto.replace("&", "&amp;").replace("<", "&lt;") + "</html>");
        etiqueta.setBackground(fondo);
        etiqueta.setForeground(letra);
        setVisible(true);
        revalidate();

        SwingUtilities.invokeLater(this::revalidate);
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension base = super.getPreferredSize();
        Object vista = etiqueta.getClientProperty(BasicHTML.propertyKey);
        if (!(vista instanceof View)) {
            return base;
        }
        int ancho = getWidth();
        if (ancho <= 0) {
            Container padre = getParent();
            ancho = padre == null ? 0 : padre.getWidth();
        }
        Insets externo = getInsets();
        Insets interno = etiqueta.getInsets();
        int util = ancho - externo.left - externo.right - interno.left - interno.right;
        if (util <= 0) {
            return base;
        }
        View v = (View) vista;
        v.setSize(util, 0);
        int alto = (int) Math.ceil(v.getPreferredSpan(View.Y_AXIS))
                + interno.top + interno.bottom + externo.top + externo.bottom;
        return new Dimension(base.width, Math.max(alto, base.height));
    }
}
