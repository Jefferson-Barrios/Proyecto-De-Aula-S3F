package vista;

import dao.UsuarioDAO;
import eipi.util.Sesion;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import modelo.Usuario;
import vista.componentes.Estilo;
import vista.componentes.PanelLogin;

public class LoginFrame extends JFrame {

    private final PanelLogin panel = new PanelLogin();

    public LoginFrame() {
        super("E.I.P.I - Inicio de sesión");
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JPanel fondo = new JPanel(new GridBagLayout());
        fondo.setBackground(Estilo.FONDO);
        fondo.add(panel);
        setContentPane(fondo);

        Image logo = panel.getLogo();
        if (logo != null) {
            setIconImage(logo);
        }

        panel.getBtnIngresar().addActionListener(e -> iniciarSesion());
        panel.getCampoClave().addActionListener(e -> iniciarSesion());
        panel.getCampoUsuario().addActionListener(e -> panel.getCampoClave().getCampo().requestFocusInWindow());
        panel.getLblOlvido().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                JOptionPane.showMessageDialog(LoginFrame.this,
                        "Para restablecer tu contraseña comunícate con la Directiva\n"
                        + "o con el Administrador de tu institución.",
                        "¿Olvidó su contraseña?", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        getRootPane().setDefaultButton(panel.getBtnIngresar());

        pack();
        setMinimumSize(getSize());
        setLocationRelativeTo(null);
    }

    private void iniciarSesion() {
        String usuario = panel.getCampoUsuario().getTexto().trim();
        String clave = panel.getCampoClave().getTexto();

        if (usuario.isEmpty() || clave.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Ingresa tu usuario y contraseña.",
                    "Datos incompletos", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Usuario u = new UsuarioDAO().autenticar(usuario, clave);
            if (u == null) {
                JOptionPane.showMessageDialog(this, "Usuario o contraseña incorrectos.",
                        "Acceso denegado", JOptionPane.ERROR_MESSAGE);
                panel.getCampoClave().limpiar();
                panel.getCampoClave().getCampo().requestFocusInWindow();
                return;
            }
            Sesion.setUsuarioActual(u);
            new VentanaPrincipal(u).setVisible(true);
            dispose();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "No se pudo conectar a la base de datos.\n"
                    + "Verifica que el servidor esté encendido en DBngin.\n\n" + ex.getMessage(),
                    "Error de conexión", JOptionPane.ERROR_MESSAGE);
        }
    }
}
