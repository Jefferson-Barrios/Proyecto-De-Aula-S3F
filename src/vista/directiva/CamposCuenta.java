package vista.directiva;

import dao.UsuarioDAO;
import eipi.util.DatosException;
import javax.swing.JComponent;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import modelo.Usuario;
import vista.componentes.Ui;

public class CamposCuenta {

    public final JTextField nombre = Ui.campo();
    public final JTextField documento = Ui.campo();
    public final JTextField telefono = Ui.campo();
    public final JTextField correo = Ui.campo();
    public final JTextField usuario = Ui.campo();
    public final JPasswordField clave = Ui.campoClave();
    public final JPasswordField confirmar = Ui.campoClave();

    private JComponent[] personales;
    private JComponent[] acceso;

    public JComponent[] personales() {
        if (personales == null) {
            personales = new JComponent[]{
                Ui.conEtiqueta("Nombre completo *", nombre),
                Ui.conEtiqueta("Documento *", documento),
                Ui.conEtiqueta("Teléfono", telefono),
                Ui.conEtiqueta("Correo", correo)};
        }
        return personales;
    }

    public JComponent[] acceso() {
        if (acceso == null) {
            acceso = new JComponent[]{
                Ui.conEtiqueta("Usuario de acceso *", usuario),
                Ui.conEtiqueta("Contraseña * (mínimo 6 caracteres)", clave),
                Ui.conEtiqueta("Confirmar contraseña *", confirmar)};
        }
        return acceso;
    }

    public void mostrarAcceso(boolean visible) {
        for (JComponent c : acceso()) {
            c.setVisible(visible);
        }
    }

    public static JComponent[] unir(JComponent[]... partes) {
        int total = 0;
        for (JComponent[] p : partes) {
            total += p.length;
        }
        JComponent[] r = new JComponent[total];
        int i = 0;
        for (JComponent[] p : partes) {
            for (JComponent c : p) {
                r[i++] = c;
            }
        }
        return r;
    }

    public void limpiar() {
        nombre.setText("");
        documento.setText("");
        telefono.setText("");
        correo.setText("");
        usuario.setText("");
        clave.setText("");
        confirmar.setText("");
    }

    public void cargar(Usuario u) {
        limpiar();
        nombre.setText(u.getNombre() == null ? "" : u.getNombre());
        documento.setText(u.getDocumento() == null ? "" : u.getDocumento());
        telefono.setText(u.getTelefono() == null ? "" : u.getTelefono());
        correo.setText(u.getCorreo() == null ? "" : u.getCorreo());
    }

    public void validarPersonales() {
        if (nombre.getText().trim().isEmpty() || documento.getText().trim().isEmpty()) {
            throw new IllegalArgumentException("Completa todos los campos obligatorios.");
        }
        String mail = correo.getText().trim();
        if (!mail.isEmpty() && !mail.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            throw new IllegalArgumentException("El correo no tiene un formato válido.");
        }
        String tel = telefono.getText().trim();
        if (!tel.isEmpty() && !tel.matches("^[0-9+()\\s-]{7,20}$")) {
            throw new IllegalArgumentException("El teléfono debe tener solo números (mínimo 7 dígitos).");
        }
    }

    public void validarAcceso() {
        String u = usuario.getText().trim();
        if (u.isEmpty() || clave.getPassword().length == 0) {
            throw new IllegalArgumentException("Completa todos los campos obligatorios.");
        }
        if (u.length() < 4 || u.contains(" ")) {
            throw new IllegalArgumentException("El usuario debe tener mínimo 4 caracteres y no puede tener espacios.");
        }
        String c = new String(clave.getPassword());
        if (c.length() < 6) {
            throw new IllegalArgumentException("La contraseña debe tener mínimo 6 caracteres.");
        }
        if (!c.equals(new String(confirmar.getPassword()))) {
            throw new IllegalArgumentException("Las contraseñas no coinciden.");
        }
    }

    public void validarCampos() {
        validarPersonales();
        validarAcceso();
    }

    public void validarDuplicados(UsuarioDAO dao) throws DatosException {
        if (dao.existeUsuario(usuario.getText().trim())) {
            throw new IllegalArgumentException("Ese nombre de usuario ya está en uso.");
        }
        if (dao.existeDocumento(documento.getText().trim())) {
            throw new IllegalArgumentException("Ya existe un usuario registrado con ese documento.");
        }
    }

    public void validarDocumentoLibre(UsuarioDAO dao, int idUsuario) throws DatosException {
        if (dao.existeDocumentoDeOtro(documento.getText().trim(), idUsuario)) {
            throw new IllegalArgumentException("Ya existe otro usuario registrado con ese documento.");
        }
    }

    public void aplicar(Usuario u, int idInstitucion) {
        u.setIdInstitucion(idInstitucion);
        u.setNombre(nombre.getText().trim());
        u.setDocumento(documento.getText().trim());
        u.setTelefono(telefono.getText().trim().isEmpty() ? null : telefono.getText().trim());
        u.setCorreo(correo.getText().trim().isEmpty() ? null : correo.getText().trim());
        u.setUsuario(usuario.getText().trim());
        u.setContrasena(new String(clave.getPassword()));
        u.setEstado("ACTIVO");
    }
}
