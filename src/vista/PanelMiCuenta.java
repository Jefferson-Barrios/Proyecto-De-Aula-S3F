package vista;

import dao.UsuarioDAO;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import eipi.util.DatosException;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import modelo.Usuario;
import vista.componentes.BarraMensaje;
import vista.componentes.BotonSimple;
import vista.componentes.Estilo;
import vista.componentes.Tarjeta;
import vista.componentes.Ui;

public class PanelMiCuenta extends JPanel implements Refrescable {

    private final Usuario usuario;
    private final UsuarioDAO dao = new UsuarioDAO();
    private final BarraMensaje barra = new BarraMensaje();
    private final JPasswordField pfActual = Ui.campoClave();
    private final JPasswordField pfNueva = Ui.campoClave();
    private final JPasswordField pfConfirmar = Ui.campoClave();

    public PanelMiCuenta(Usuario usuario) {
        this.usuario = usuario;
        setLayout(new BorderLayout(0, 16));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(28, 32, 28, 32));

        JPanel norte = new JPanel();
        norte.setOpaque(false);
        norte.setLayout(new BoxLayout(norte, BoxLayout.Y_AXIS));
        JLabel t = Ui.titulo("Mi cuenta");
        JLabel s = Ui.subtitulo("Tus datos de acceso y la seguridad de tu contraseña");
        t.setAlignmentX(LEFT_ALIGNMENT);
        s.setAlignmentX(LEFT_ALIGNMENT);
        barra.setAlignmentX(LEFT_ALIGNMENT);
        norte.add(t);
        norte.add(s);
        norte.add(barra);
        add(norte, BorderLayout.NORTH);

        JPanel centro = new JPanel(new BorderLayout(0, 16));
        centro.setOpaque(false);

        Tarjeta datos = new Tarjeta(new BorderLayout(0, 4));
        JLabel tituloDatos = new JLabel("Mis datos");
        tituloDatos.setFont(Estilo.fuentePx(Estilo.NEGRITA, 15f));
        tituloDatos.setForeground(Estilo.AZUL);
        tituloDatos.setBorder(BorderFactory.createEmptyBorder(0, 8, 4, 0));
        datos.add(tituloDatos, BorderLayout.NORTH);
        datos.add(Ui.rejilla(3,
                dato("Nombre", usuario.getNombre()),
                dato("Documento", usuario.getDocumento()),
                dato("Usuario de acceso", usuario.getUsuario()),
                dato("Rol", PantallaApp.nombreRol(usuario.getRol())),
                dato("Correo", usuario.getCorreo()),
                dato("Teléfono", usuario.getTelefono())), BorderLayout.CENTER);
        centro.add(datos, BorderLayout.NORTH);

        Tarjeta clave = new Tarjeta(new BorderLayout(0, 4));
        JLabel tituloClave = new JLabel("Cambiar contraseña");
        tituloClave.setFont(Estilo.fuentePx(Estilo.NEGRITA, 15f));
        tituloClave.setForeground(Estilo.AZUL);
        tituloClave.setBorder(BorderFactory.createEmptyBorder(0, 8, 4, 0));
        clave.add(tituloClave, BorderLayout.NORTH);
        clave.add(Ui.rejilla(3,
                Ui.conEtiqueta("Contraseña actual *", pfActual),
                Ui.conEtiqueta("Nueva contraseña * (mínimo 6)", pfNueva),
                Ui.conEtiqueta("Confirmar nueva contraseña *", pfConfirmar)), BorderLayout.CENTER);
        BotonSimple guardar = new BotonSimple("Guardar contraseña", BotonSimple.Tipo.PRIMARIO);
        guardar.addActionListener(e -> guardar());
        JPanel pie = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        pie.setOpaque(false);
        pie.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
        pie.add(guardar);
        clave.add(pie, BorderLayout.SOUTH);

        JPanel envoltura = new JPanel(new BorderLayout());
        envoltura.setOpaque(false);
        envoltura.add(clave, BorderLayout.NORTH);
        centro.add(envoltura, BorderLayout.CENTER);
        add(centro, BorderLayout.CENTER);
    }

    private static JPanel dato(String etiqueta, String valor) {
        JLabel v = new JLabel(valor == null || valor.isEmpty() ? "—" : valor);
        v.setFont(Estilo.fuentePx(Estilo.REGULAR, 14f));
        v.setForeground(Estilo.TEXTO);
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setOpaque(false);
        p.add(Ui.subtitulo(etiqueta), BorderLayout.NORTH);
        p.add(v, BorderLayout.CENTER);
        return p;
    }

    private void limpiarCampos() {
        pfActual.setText("");
        pfNueva.setText("");
        pfConfirmar.setText("");
    }

    @Override
    public void refrescar() {
        limpiarCampos();
        barra.limpiar();
    }

    private void guardar() {
        String actual = new String(pfActual.getPassword());
        String nueva = new String(pfNueva.getPassword());
        String confirmar = new String(pfConfirmar.getPassword());
        if (actual.isEmpty() || nueva.isEmpty() || confirmar.isEmpty()) {
            barra.error("Completa los tres campos.");
            return;
        }
        if (nueva.length() < 6) {
            barra.error("La nueva contraseña debe tener mínimo 6 caracteres.");
            return;
        }
        if (!nueva.equals(confirmar)) {
            barra.error("La nueva contraseña y su confirmación no coinciden.");
            return;
        }
        if (nueva.equals(actual)) {
            barra.error("La nueva contraseña debe ser diferente a la actual.");
            return;
        }
        try {
            if (!dao.verificarContrasena(usuario.getIdUsuario(), actual)) {
                barra.error("La contraseña actual no es correcta.");
                return;
            }
            dao.cambiarContrasena(usuario.getIdUsuario(), nueva);
            limpiarCampos();
            barra.exito("Tu contraseña se actualizó correctamente.");
        } catch (DatosException ex) {
            barra.error("No se pudo cambiar la contraseña: " + ex.getMessage());
        }
    }
}
