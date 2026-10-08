package vista;

import eipi.util.Sesion;
import java.awt.BorderLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import modelo.Usuario;

public class VentanaPrincipal extends JFrame {

    public VentanaPrincipal(Usuario usuario) {
        super("EIPI - " + usuario.getRol());
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(480, 240);
        setLocationRelativeTo(null);

        JLabel lblBienvenida = new JLabel(
                "<html><center>Bienvenido, " + usuario.getNombre()
                + "<br>Rol: " + usuario.getRol() + "</center></html>",
                SwingConstants.CENTER);
        lblBienvenida.setFont(lblBienvenida.getFont().deriveFont(18f));

        JButton btnSalir = new JButton("Cerrar sesión");
        btnSalir.addActionListener(e -> {
            Sesion.cerrar();
            new LoginFrame().setVisible(true);
            dispose();
        });

        add(lblBienvenida, BorderLayout.CENTER);
        add(btnSalir, BorderLayout.SOUTH);
    }
}
