package vista.admin;

import java.awt.BorderLayout;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import vista.componentes.Tarjeta;
import vista.componentes.Ui;

public class PanelEnConstruccion extends JPanel {

    public PanelEnConstruccion(String nombreRol) {
        setLayout(new BorderLayout());
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(28, 32, 28, 32));

        Tarjeta t = new Tarjeta(new BorderLayout());
        JPanel textos = new JPanel();
        textos.setOpaque(false);
        textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));
        JLabel a = Ui.titulo("Módulo de " + nombreRol);
        JLabel b = Ui.subtitulo("Este módulo está en construcción. Pronto tendrás aquí tus opciones.");
        a.setAlignmentX(LEFT_ALIGNMENT);
        b.setAlignmentX(LEFT_ALIGNMENT);
        textos.add(a);
        textos.add(b);
        t.add(textos, BorderLayout.NORTH);

        JPanel envoltura = new JPanel(new BorderLayout());
        envoltura.setOpaque(false);
        envoltura.add(t, BorderLayout.NORTH);
        add(envoltura, BorderLayout.CENTER);
    }
}
