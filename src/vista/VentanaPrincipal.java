package vista;

import eipi.util.Sesion;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import javax.swing.JFrame;
import modelo.Usuario;
import vista.componentes.Estilo;

public class VentanaPrincipal extends JFrame {

    public VentanaPrincipal() {
        super("E.I.P.I");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        Image logo = Estilo.logo();
        if (logo != null) {
            setIconImage(logo);
        }
        mostrarLogin();
    }

    public final void mostrarLogin() {
        Sesion.cerrar();
        setExtendedState(JFrame.NORMAL);
        setMinimumSize(new Dimension(0, 0));
        setTitle("E.I.P.I - Inicio de sesión");
        setContentPane(new PantallaLogin(this::mostrarAplicacion));
        pack();
        setMinimumSize(getSize());
        setLocationRelativeTo(null);
        revalidate();
        repaint();
    }

    private void mostrarAplicacion(Usuario usuario) {
        setTitle("E.I.P.I - " + usuario.getNombre());
        setMinimumSize(new Dimension(1000, 620));
        setContentPane(new PantallaApp(usuario, this::mostrarLogin));
        Dimension pantalla = Toolkit.getDefaultToolkit().getScreenSize();
        setSize(Math.min(1280, pantalla.width), Math.min(760, pantalla.height - 40));
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        revalidate();
        repaint();
    }
}
