package vista.admin;

import vista.componentes.BotonExportar;
import eipi.util.TablaExportable;
import dao.InstitucionDAO;
import dao.UsuarioDAO;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import eipi.util.DatosException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import modelo.Directiva;
import modelo.Institucion;
import vista.Refrescable;
import vista.componentes.BarraMensaje;
import vista.componentes.BotonSimple;
import vista.componentes.Tarjeta;
import vista.componentes.Ui;

public class PanelDirectivas extends JPanel implements Refrescable {

    private static final String LISTA = "LISTA";
    private static final String NUEVA = "NUEVA";
    private static final String CLAVE = "CLAVE";

    private final CardLayout cartas = new CardLayout();
    private final JPanel cuerpo = new JPanel(cartas);
    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private final InstitucionDAO institucionDAO = new InstitucionDAO();
    private List<Object[]> filas = new ArrayList<>();

    private final BarraMensaje barraLista = new BarraMensaje();
    private final DefaultTableModel modelo = new DefaultTableModel(
            new Object[] { "Nombre", "Documento", "Usuario", "Institución", "Estado" }, 0) {
        @Override
        public boolean isCellEditable(int fila, int col) {
            return false;
        }
    };
    private final JTable tabla = new JTable(modelo);
    private final BotonSimple btnClave = new BotonSimple("Cambiar contraseña", BotonSimple.Tipo.SECUNDARIO);
    private final BotonSimple btnEstado = new BotonSimple("Desactivar", BotonSimple.Tipo.PELIGRO);

    private final BarraMensaje barraNueva = new BarraMensaje();
    private final JTextField tfNombre = Ui.campo();
    private final JTextField tfDocumento = Ui.campo();
    private final JTextField tfTelefono = Ui.campo();
    private final JTextField tfCorreo = Ui.campo();
    private final JComboBox<Institucion> cbInstitucion = Ui.combo();
    private final JTextField tfUsuario = Ui.campo();
    private final JPasswordField pfClave = Ui.campoClave();
    private final JPasswordField pfConfirmar = Ui.campoClave();

    private final BarraMensaje barraClave = new BarraMensaje();
    private final JLabel lblClaveDe = Ui.subtitulo(" ");
    private final JPasswordField pfNueva = Ui.campoClave();
    private final JPasswordField pfNuevaConfirmar = Ui.campoClave();
    private int idClave = -1;

    public PanelDirectivas() {
        setLayout(new BorderLayout());
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(28, 32, 28, 32));
        cuerpo.setOpaque(false);
        cuerpo.add(construirLista(), LISTA);
        cuerpo.add(construirNueva(), NUEVA);
        cuerpo.add(construirClave(), CLAVE);
        add(cuerpo, BorderLayout.CENTER);
    }

    private static JPanel encabezado(JLabel titulo, JLabel sub, BarraMensaje barra) {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        titulo.setAlignmentX(LEFT_ALIGNMENT);
        sub.setAlignmentX(LEFT_ALIGNMENT);
        barra.setAlignmentX(LEFT_ALIGNMENT);
        p.add(titulo);
        p.add(sub);
        p.add(barra);
        return p;
    }

    private static JPanel botones(Component... cs) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        p.setOpaque(false);
        for (Component c : cs) {
            p.add(c);
        }
        return p;
    }

    private JPanel construirLista() {
        JPanel p = new JPanel(new BorderLayout(0, 16));
        p.setOpaque(false);

        JPanel norte = new JPanel(new BorderLayout());
        norte.setOpaque(false);
        norte.add(encabezado(Ui.titulo("Directivas"),
                Ui.subtitulo("Crea las credenciales de acceso de la directiva de cada institución"), barraLista),
                BorderLayout.CENTER);
        BotonSimple nueva = new BotonSimple("+ Nueva directiva", BotonSimple.Tipo.PRIMARIO);
        nueva.addActionListener(e -> abrirNueva());
        JPanel derecha = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        derecha.setOpaque(false);
        derecha.add(nueva);
        norte.add(derecha, BorderLayout.EAST);
        p.add(norte, BorderLayout.NORTH);

        Ui.estilizarTabla(tabla);
        int[] anchos = { 220, 120, 120, 300, 90 };
        for (int i = 0; i < anchos.length; i++) {
            tabla.getColumnModel().getColumn(i).setPreferredWidth(anchos[i]);
        }
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.getSelectionModel().addListSelectionListener(e -> actualizarBotones());
        Tarjeta tarjeta = new Tarjeta(new BorderLayout());
        tarjeta.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        tarjeta.add(Ui.scroll(tabla), BorderLayout.CENTER);
        p.add(tarjeta, BorderLayout.CENTER);

        btnClave.addActionListener(e -> abrirClave());
        btnEstado.addActionListener(e -> cambiarEstado());
        BotonExportar exportar = new BotonExportar(
                () -> TablaExportable.de("Directivas", "", modelo), barraLista);
        JPanel derecha2 = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        derecha2.setOpaque(false);
        derecha2.add(exportar);
        JPanel pie = new JPanel(new BorderLayout());
        pie.setOpaque(false);
        pie.add(botones(btnClave, btnEstado), BorderLayout.CENTER);
        pie.add(derecha2, BorderLayout.EAST);
        p.add(pie, BorderLayout.SOUTH);
        actualizarBotones();
        return p;
    }

    private Object[] seleccionada() {
        int fila = tabla.getSelectedRow();
        return (fila >= 0 && fila < filas.size()) ? filas.get(fila) : null;
    }

    private void actualizarBotones() {
        Object[] f = seleccionada();
        btnClave.setEnabled(f != null);
        btnEstado.setEnabled(f != null);
        btnEstado.setText(f == null || "ACTIVO".equals(f[5]) ? "Desactivar" : "Activar");
    }

    public void cargarLista(List<Object[]> lista) {
        filas = new ArrayList<>(lista);
        modelo.setRowCount(0);
        for (Object[] f : filas) {
            modelo.addRow(new Object[] { f[1], f[2], f[3], f[4] == null ? "" : f[4], f[5] });
        }
        actualizarBotones();
    }

    private void cambiarEstado() {
        Object[] f = seleccionada();
        if (f == null) {
            return;
        }
        String nuevo = "ACTIVO".equals(f[5]) ? "INACTIVO" : "ACTIVO";
        try {
            usuarioDAO.cambiarEstado((Integer) f[0], nuevo);
            refrescar();
            barraLista.exito("El usuario \"" + f[3] + "\" quedó " + nuevo.toLowerCase() + ".");
        } catch (DatosException ex) {
            barraLista.error("No se pudo cambiar el estado: " + ex.getMessage());
        }
    }

    @Override
    public void refrescar() {
        cartas.show(cuerpo, LISTA);
        try {
            cargarLista(usuarioDAO.listarDirectivas());
            barraLista.limpiar();
        } catch (DatosException ex) {
            barraLista.error("No se pudo cargar las directivas. Revisa la carpeta datos del proyecto. ("
                    + ex.getMessage() + ")");
        }
    }

    private JPanel construirNueva() {
        JPanel p = new JPanel(new BorderLayout(0, 16));
        p.setOpaque(false);
        p.add(encabezado(Ui.titulo("Nueva directiva"),
                Ui.subtitulo("Los campos con * son obligatorios"), barraNueva), BorderLayout.NORTH);

        cbInstitucion.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> lista, Object valor, int i,
                    boolean sel, boolean foco) {
                String texto = valor instanceof Institucion ? ((Institucion) valor).getNombre() : "";
                return super.getListCellRendererComponent(lista, texto, i, sel, foco);
            }
        });

        Tarjeta tarjeta = new Tarjeta(new BorderLayout());
        tarjeta.add(Ui.rejilla(2,
                Ui.conEtiqueta("Nombre completo *", tfNombre),
                Ui.conEtiqueta("Documento *", tfDocumento),
                Ui.conEtiqueta("Teléfono", tfTelefono),
                Ui.conEtiqueta("Correo", tfCorreo),
                Ui.conEtiqueta("Institución *", cbInstitucion),
                Ui.conEtiqueta("Usuario de acceso *", tfUsuario),
                Ui.conEtiqueta("Contraseña * (mínimo 6 caracteres)", pfClave),
                Ui.conEtiqueta("Confirmar contraseña *", pfConfirmar)),
                BorderLayout.NORTH);
        p.add(tarjeta, BorderLayout.CENTER);

        BotonSimple guardar = new BotonSimple("Crear directiva", BotonSimple.Tipo.PRIMARIO);
        BotonSimple cancelar = new BotonSimple("Cancelar", BotonSimple.Tipo.SECUNDARIO);
        guardar.addActionListener(e -> crearDirectiva());
        cancelar.addActionListener(e -> cartas.show(cuerpo, LISTA));
        p.add(botones(guardar, cancelar), BorderLayout.SOUTH);
        return p;
    }

    private void abrirNueva() {
        List<Institucion> activas = new ArrayList<>();
        try {
            for (Institucion i : institucionDAO.listar()) {
                if ("ACTIVO".equals(i.getEstado())) {
                    activas.add(i);
                }
            }
        } catch (DatosException ex) {
            barraLista.error("No se pudo cargar las instituciones: " + ex.getMessage());
            return;
        }
        if (activas.isEmpty()) {
            barraLista.error("Primero registra al menos una institución activa en la sección Instituciones.");
            return;
        }
        cbInstitucion.removeAllItems();
        for (Institucion i : activas) {
            cbInstitucion.addItem(i);
        }
        tfNombre.setText("");
        tfDocumento.setText("");
        tfTelefono.setText("");
        tfCorreo.setText("");
        tfUsuario.setText("");
        pfClave.setText("");
        pfConfirmar.setText("");
        barraNueva.limpiar();
        barraLista.limpiar();
        cartas.show(cuerpo, NUEVA);
        tfNombre.requestFocusInWindow();
    }

    private void crearDirectiva() {
        String nombre = tfNombre.getText().trim();
        String documento = tfDocumento.getText().trim();
        String correo = tfCorreo.getText().trim();
        String usuario = tfUsuario.getText().trim();
        String clave = new String(pfClave.getPassword());
        String confirmar = new String(pfConfirmar.getPassword());
        Institucion inst = (Institucion) cbInstitucion.getSelectedItem();

        if (nombre.isEmpty() || documento.isEmpty() || usuario.isEmpty() || clave.isEmpty() || inst == null) {
            barraNueva.error("Completa todos los campos obligatorios.");
            return;
        }
        if (!correo.isEmpty() && !correo.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            barraNueva.error("El correo no tiene un formato válido.");
            return;
        }
        if (usuario.length() < 4 || usuario.contains(" ")) {
            barraNueva.error("El usuario debe tener mínimo 4 caracteres y no puede tener espacios.");
            return;
        }
        if (clave.length() < 6) {
            barraNueva.error("La contraseña debe tener mínimo 6 caracteres.");
            return;
        }
        if (!clave.equals(confirmar)) {
            barraNueva.error("Las contraseñas no coinciden.");
            return;
        }

        try {
            if (usuarioDAO.existeUsuario(usuario)) {
                barraNueva.error("Ese nombre de usuario ya está en uso.");
                return;
            }
            if (usuarioDAO.existeDocumento(documento)) {
                barraNueva.error("Ya existe un usuario registrado con ese documento.");
                return;
            }
            Directiva d = new Directiva();
            d.setIdInstitucion(inst.getIdInstitucion());
            d.setNombre(nombre);
            d.setDocumento(documento);
            d.setTelefono(tfTelefono.getText().trim().isEmpty() ? null : tfTelefono.getText().trim());
            d.setCorreo(correo.isEmpty() ? null : correo);
            d.setUsuario(usuario);
            d.setContrasena(clave);
            d.setEstado("ACTIVO");
            usuarioDAO.crearDirectiva(d);
            refrescar();
            barraLista.exito("Directiva creada. Usuario de acceso: " + usuario);
        } catch (DatosException ex) {
            barraNueva.error("No se pudo crear la directiva: " + ex.getMessage());
        }
    }

    private JPanel construirClave() {
        JPanel p = new JPanel(new BorderLayout(0, 16));
        p.setOpaque(false);
        p.add(encabezado(Ui.titulo("Cambiar contraseña"), lblClaveDe, barraClave), BorderLayout.NORTH);

        Tarjeta tarjeta = new Tarjeta(new BorderLayout());
        tarjeta.add(Ui.rejilla(2,
                Ui.conEtiqueta("Nueva contraseña * (mínimo 6 caracteres)", pfNueva),
                Ui.conEtiqueta("Confirmar contraseña *", pfNuevaConfirmar)),
                BorderLayout.NORTH);
        p.add(tarjeta, BorderLayout.CENTER);

        BotonSimple guardar = new BotonSimple("Guardar contraseña", BotonSimple.Tipo.PRIMARIO);
        BotonSimple cancelar = new BotonSimple("Cancelar", BotonSimple.Tipo.SECUNDARIO);
        guardar.addActionListener(e -> guardarClave());
        cancelar.addActionListener(e -> cartas.show(cuerpo, LISTA));
        p.add(botones(guardar, cancelar), BorderLayout.SOUTH);
        return p;
    }

    private void abrirClave() {
        Object[] f = seleccionada();
        if (f == null) {
            return;
        }
        idClave = (Integer) f[0];
        lblClaveDe.setText("Usuario: " + f[3] + "  (" + f[1] + ")");
        pfNueva.setText("");
        pfNuevaConfirmar.setText("");
        barraClave.limpiar();
        barraLista.limpiar();
        cartas.show(cuerpo, CLAVE);
        pfNueva.requestFocusInWindow();
    }

    private void guardarClave() {
        String nueva = new String(pfNueva.getPassword());
        String confirmar = new String(pfNuevaConfirmar.getPassword());
        if (nueva.length() < 6) {
            barraClave.error("La contraseña debe tener mínimo 6 caracteres.");
            return;
        }
        if (!nueva.equals(confirmar)) {
            barraClave.error("Las contraseñas no coinciden.");
            return;
        }
        try {
            usuarioDAO.cambiarContrasena(idClave, nueva);
            refrescar();
            barraLista.exito("Contraseña actualizada correctamente.");
        } catch (DatosException ex) {
            barraClave.error("No se pudo actualizar la contraseña: " + ex.getMessage());
        }
    }
}
