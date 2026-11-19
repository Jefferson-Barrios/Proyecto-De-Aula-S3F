package vista;

import dao.UsuarioDAO;
import eipi.util.Sesion;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import eipi.util.DatosException;
import java.util.function.Consumer;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import modelo.Usuario;
import vista.componentes.Estilo;
import vista.componentes.PanelLogin;

public class PantallaLogin extends JPanel {

    private final PanelLogin panel = new PanelLogin();
    private final Consumer<Usuario> alIngresar;

    public PantallaLogin(Consumer<Usuario> alIngresar) {
        super(new GridBagLayout());
        this.alIngresar = alIngresar;
        setBackground(Estilo.FONDO);
        add(panel);

        panel.getBtnIngresar().addActionListener(e -> iniciarSesion());
        panel.getCampoClave().addActionListener(e -> iniciarSesion());
        panel.getCampoUsuario().addActionListener(e -> panel.getCampoClave().getCampo().requestFocusInWindow());
        panel.getLblOlvido().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                JOptionPane.showMessageDialog(ventana(),
                        "Para restablecer tu contraseña comunícate con la Directiva\n"
                                + "o con el Administrador de tu institución.",
                        "¿Olvidó su contraseña?", JOptionPane.INFORMATION_MESSAGE);
            }
        });
    }

    private Window ventana() {
        return SwingUtilities.getWindowAncestor(this);
    }

    private void iniciarSesion() {
        String usuario = panel.getCampoUsuario().getTexto().trim();
        String clave = panel.getCampoClave().getTexto();

        if (usuario.isEmpty() || clave.isEmpty()) {
            JOptionPane.showMessageDialog(ventana(), "Ingresa tu usuario y contraseña.",
                    "Datos incompletos", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Usuario u = new UsuarioDAO().autenticar(usuario, clave);
            if (u == null) {
                JOptionPane.showMessageDialog(ventana(), "Usuario o contraseña incorrectos.",
                        "Acceso denegado", JOptionPane.ERROR_MESSAGE);
                panel.getCampoClave().limpiar();
                panel.getCampoClave().getCampo().requestFocusInWindow();
                return;
            }
            Sesion.setUsuarioActual(u);
            alIngresar.accept(u);
        } catch (DatosException ex) {
            JOptionPane.showMessageDialog(ventana(),
                    "No se pudo conectar a la base de datos.\n"
                            + "Verifica que el servidor esté encendido en DBngin.\n\n" + ex.getMessage(),
                    "Error de conexión", JOptionPane.ERROR_MESSAGE);
        }
    }
}
