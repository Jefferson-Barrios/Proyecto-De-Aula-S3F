package vista.componentes;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import javax.swing.BorderFactory;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class AreaTexto extends JScrollPane {

    private final JTextArea area = new JTextArea();

    public AreaTexto(int filas) {
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setFont(Estilo.fuentePx(Estilo.REGULAR, 14f));
        area.setForeground(Estilo.TEXTO);
        area.setCaretColor(Estilo.AZUL);
        area.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        setViewportView(area);
        getViewport().setBackground(Color.WHITE);
        bordeNormal();
        setPreferredSize(new Dimension(220, 24 * filas + 24));
        area.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                setBorder(new BordeRedondeado(Estilo.AZUL, 14, 2, 0, 0));
            }

            @Override
            public void focusLost(FocusEvent e) {
                bordeNormal();
            }
        });
    }

    private void bordeNormal() {
        setBorder(new BordeRedondeado(Estilo.BORDE_CAMPO, 14, 1, 0, 0));
    }

    public String getTexto() {
        return area.getText();
    }

    public void setTexto(String texto) {
        area.setText(texto);
        area.setCaretPosition(0);
    }
}
