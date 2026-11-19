package vista.admin;

import vista.componentes.BotonExportar;
import eipi.util.TablaExportable;
import dao.InstitucionDAO;
import eipi.util.Sesion;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import eipi.util.DatosException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import modelo.Institucion;
import vista.Refrescable;
import vista.componentes.BarraMensaje;
import vista.componentes.BotonSimple;
import vista.componentes.Tarjeta;
import vista.componentes.Ui;

public class PanelInstituciones extends JPanel implements Refrescable {

    private static final String LISTA = "LISTA";
    private static final String FORM = "FORM";

    private final CardLayout cartas = new CardLayout();
    private final JPanel cuerpo = new JPanel(cartas);
    private final InstitucionDAO dao = new InstitucionDAO();
    private List<Institucion> datos = new ArrayList<>();
    private Institucion enEdicion = null;

    private final BarraMensaje barraLista = new BarraMensaje();
    private final DefaultTableModel modelo = new DefaultTableModel(
            new Object[] { "Nombre", "Código oficial", "Sede principal", "Rector", "Teléfono", "Estado" }, 0) {
        @Override
        public boolean isCellEditable(int fila, int col) {
            return false;
        }
    };
    private final JTable tabla = new JTable(modelo);
    private final BotonSimple btnEditar = new BotonSimple("Editar", BotonSimple.Tipo.SECUNDARIO);
    private final BotonSimple btnEstado = new BotonSimple("Desactivar", BotonSimple.Tipo.PELIGRO);

    private final JLabel tituloForm = Ui.titulo("Nueva institución");
    private final BarraMensaje barraForm = new BarraMensaje();
    private final JTextField tfNombre = Ui.campo();
    private final JTextField tfCodigo = Ui.campo();
    private final JTextField tfSede = Ui.campo();
    private final JTextField tfBarrio = Ui.campo();
    private final JTextField tfTelInst = Ui.campo();
    private final JTextField tfCorreoInst = Ui.campo();
    private final JTextField tfRector = Ui.campo();
    private final JTextField tfCorreoResp = Ui.campo();
    private final JTextField tfTelResp = Ui.campo();

    public PanelInstituciones() {
        setLayout(new BorderLayout());
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(28, 32, 28, 32));
        cuerpo.setOpaque(false);
        cuerpo.add(construirLista(), LISTA);
        cuerpo.add(construirFormulario(), FORM);
        add(cuerpo, BorderLayout.CENTER);
    }

    private JPanel construirLista() {
        JPanel p = new JPanel(new BorderLayout(0, 16));
        p.setOpaque(false);

        JPanel norte = new JPanel(new BorderLayout());
        norte.setOpaque(false);
        JPanel textos = new JPanel();
        textos.setOpaque(false);
        textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));
        JLabel t = Ui.titulo("Instituciones");
        JLabel s = Ui.subtitulo("Registra y administra las instituciones educativas");
        t.setAlignmentX(LEFT_ALIGNMENT);
        s.setAlignmentX(LEFT_ALIGNMENT);
        barraLista.setAlignmentX(LEFT_ALIGNMENT);
        textos.add(t);
        textos.add(s);
        textos.add(barraLista);
        BotonSimple nueva = new BotonSimple("+ Nueva institución", BotonSimple.Tipo.PRIMARIO);
        nueva.addActionListener(e -> abrirFormulario(null));
        JPanel derecha = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        derecha.setOpaque(false);
        derecha.add(nueva);
        norte.add(textos, BorderLayout.CENTER);
        norte.add(derecha, BorderLayout.EAST);
        p.add(norte, BorderLayout.NORTH);

        Ui.estilizarTabla(tabla);
        int[] anchos = { 300, 110, 160, 170, 120, 90 };
        for (int i = 0; i < anchos.length; i++) {
            tabla.getColumnModel().getColumn(i).setPreferredWidth(anchos[i]);
        }
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.getSelectionModel().addListSelectionListener(e -> actualizarBotones());
        tabla.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && seleccionada() != null) {
                    abrirFormulario(seleccionada());
                }
            }
        });
        Tarjeta tarjeta = new Tarjeta(new BorderLayout());
        tarjeta.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        tarjeta.add(Ui.scroll(tabla), BorderLayout.CENTER);
        p.add(tarjeta, BorderLayout.CENTER);

        btnEditar.addActionListener(e -> {
            if (seleccionada() != null) {
                abrirFormulario(seleccionada());
            }
        });
        btnEstado.addActionListener(e -> cambiarEstado());
        JPanel sur = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        sur.setOpaque(false);
        sur.add(btnEditar);
        sur.add(btnEstado);
        BotonExportar exportar = new BotonExportar(
                () -> TablaExportable.de("Instituciones", "", modelo), barraLista);
        JPanel derecha2 = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        derecha2.setOpaque(false);
        derecha2.add(exportar);
        JPanel pie = new JPanel(new BorderLayout());
        pie.setOpaque(false);
        pie.add(sur, BorderLayout.CENTER);
        pie.add(derecha2, BorderLayout.EAST);
        p.add(pie, BorderLayout.SOUTH);
        actualizarBotones();
        return p;
    }

    private Institucion seleccionada() {
        int fila = tabla.getSelectedRow();
        return (fila >= 0 && fila < datos.size()) ? datos.get(fila) : null;
    }

    private void actualizarBotones() {
        Institucion i = seleccionada();
        btnEditar.setEnabled(i != null);
        btnEstado.setEnabled(i != null);
        boolean activa = i != null && "ACTIVO".equals(i.getEstado());
        btnEstado.setText(activa || i == null ? "Desactivar" : "Activar");
    }

    public void cargarLista(List<Institucion> lista) {
        datos = new ArrayList<>(lista);
        modelo.setRowCount(0);
        for (Institucion i : datos) {
            modelo.addRow(new Object[] { i.getNombre(), i.getCodigoOficial(), valor(i.getSedePrincipal()),
                    i.getNombreRector(), valor(i.getTelefonoInstitucional()), i.getEstado() });
        }
        actualizarBotones();
    }

    private static String valor(String s) {
        return s == null ? "" : s;
    }

    private void cambiarEstado() {
        Institucion i = seleccionada();
        if (i == null) {
            return;
        }
        String nuevo = "ACTIVO".equals(i.getEstado()) ? "INACTIVO" : "ACTIVO";
        try {
            dao.cambiarEstado(i.getIdInstitucion(), nuevo);
            refrescar();
            barraLista.exito("La institución \"" + i.getNombre() + "\" quedó " + nuevo.toLowerCase() + ".");
        } catch (DatosException ex) {
            barraLista.error("No se pudo cambiar el estado: " + ex.getMessage());
        }
    }

    @Override
    public void refrescar() {
        cartas.show(cuerpo, LISTA);
        try {
            cargarLista(dao.listar());
            barraLista.limpiar();
        } catch (DatosException ex) {
            barraLista.error("No se pudo cargar las instituciones. Revisa la carpeta datos del proyecto. ("
                    + ex.getMessage() + ")");
        }
    }

    private JPanel construirFormulario() {
        JPanel p = new JPanel(new BorderLayout(0, 16));
        p.setOpaque(false);

        JPanel textos = new JPanel();
        textos.setOpaque(false);
        textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));
        tituloForm.setAlignmentX(LEFT_ALIGNMENT);
        JLabel s = Ui.subtitulo("Los campos con * son obligatorios");
        s.setAlignmentX(LEFT_ALIGNMENT);
        barraForm.setAlignmentX(LEFT_ALIGNMENT);
        textos.add(tituloForm);
        textos.add(s);
        textos.add(barraForm);
        p.add(textos, BorderLayout.NORTH);

        Tarjeta tarjeta = new Tarjeta(new BorderLayout());
        tarjeta.add(Ui.rejilla(2,
                Ui.conEtiqueta("Nombre de la institución *", tfNombre),
                Ui.conEtiqueta("Código oficial (DANE) *", tfCodigo),
                Ui.conEtiqueta("Sede principal", tfSede),
                Ui.conEtiqueta("Barrio / sector", tfBarrio),
                Ui.conEtiqueta("Teléfono institucional", tfTelInst),
                Ui.conEtiqueta("Correo institucional", tfCorreoInst),
                Ui.conEtiqueta("Nombre del rector *", tfRector),
                Ui.conEtiqueta("Teléfono del responsable", tfTelResp),
                Ui.conEtiqueta("Correo del responsable", tfCorreoResp)),
                BorderLayout.NORTH);
        p.add(tarjeta, BorderLayout.CENTER);

        BotonSimple guardar = new BotonSimple("Guardar", BotonSimple.Tipo.PRIMARIO);
        BotonSimple cancelar = new BotonSimple("Cancelar", BotonSimple.Tipo.SECUNDARIO);
        guardar.addActionListener(e -> guardar());
        cancelar.addActionListener(e -> cartas.show(cuerpo, LISTA));
        JPanel sur = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        sur.setOpaque(false);
        sur.add(guardar);
        sur.add(cancelar);
        p.add(sur, BorderLayout.SOUTH);
        return p;
    }

    private void abrirFormulario(Institucion i) {
        enEdicion = i;
        barraForm.limpiar();
        tituloForm.setText(i == null ? "Nueva institución" : "Editar institución");
        tfNombre.setText(i == null ? "" : i.getNombre());
        tfCodigo.setText(i == null ? "" : i.getCodigoOficial());
        tfSede.setText(i == null ? "" : valor(i.getSedePrincipal()));
        tfBarrio.setText(i == null ? "" : valor(i.getBarrioSector()));
        tfTelInst.setText(i == null ? "" : valor(i.getTelefonoInstitucional()));
        tfCorreoInst.setText(i == null ? "" : valor(i.getCorreoInstitucional()));
        tfRector.setText(i == null ? "" : valor(i.getNombreRector()));
        tfTelResp.setText(i == null ? "" : valor(i.getTelefonoResponsable()));
        tfCorreoResp.setText(i == null ? "" : valor(i.getCorreoResponsable()));
        cartas.show(cuerpo, FORM);
        tfNombre.requestFocusInWindow();
    }

    private static String nulo(String s) {
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private static boolean correoValido(String s) {
        return s.isEmpty() || s.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    }

    private static boolean telefonoValido(String s) {
        return s.isEmpty() || s.matches("^[0-9+()\\s-]{7,20}$");
    }

    private void guardar() {
        String nombre = tfNombre.getText().trim();
        String codigo = tfCodigo.getText().trim();
        String rector = tfRector.getText().trim();
        if (nombre.isEmpty() || codigo.isEmpty() || rector.isEmpty()) {
            barraForm.error("Completa el nombre, el código oficial y el nombre del rector.");
            return;
        }
        if (!correoValido(tfCorreoInst.getText().trim()) || !correoValido(tfCorreoResp.getText().trim())) {
            barraForm.error("Revisa los correos: el formato no es válido.");
            return;
        }
        if (!telefonoValido(tfTelInst.getText().trim()) || !telefonoValido(tfTelResp.getText().trim())) {
            barraForm.error("Revisa los teléfonos: usa solo números (mínimo 7 dígitos).");
            return;
        }

        Institucion i = enEdicion != null ? enEdicion : new Institucion();
        i.setNombre(nombre);
        i.setCodigoOficial(codigo);
        i.setSedePrincipal(nulo(tfSede.getText()));
        i.setBarrioSector(nulo(tfBarrio.getText()));
        i.setTelefonoInstitucional(nulo(tfTelInst.getText()));
        i.setCorreoInstitucional(nulo(tfCorreoInst.getText()));
        i.setNombreRector(rector);
        i.setTelefonoResponsable(nulo(tfTelResp.getText()));
        i.setCorreoResponsable(nulo(tfCorreoResp.getText()));

        try {
            boolean nueva = enEdicion == null;
            if (nueva) {
                i.setEstado("ACTIVO");
                if (Sesion.getUsuarioActual() != null) {
                    i.setIdAdministrador(Sesion.getUsuarioActual().getIdUsuario());
                }
                dao.insertar(i);
            } else {
                dao.actualizar(i);
            }
            refrescar();
            barraLista
                    .exito(nueva ? "Institución registrada correctamente." : "Institución actualizada correctamente.");
        } catch (DatosException ex) {
            barraForm.error(ex.getMessage());
        }
    }
}
