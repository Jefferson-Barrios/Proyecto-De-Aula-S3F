package vista.directiva;

import vista.componentes.BotonExportar;
import eipi.util.TablaExportable;
import eipi.util.Sesion;
import dao.UsuarioDAO;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import eipi.util.DatosException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import modelo.Usuario;
import vista.Refrescable;
import vista.componentes.BarraMensaje;
import vista.componentes.BotonSimple;
import vista.componentes.Estilo;
import vista.componentes.Tarjeta;
import vista.componentes.Ui;

public abstract class PanelGestion extends JPanel implements Refrescable {

    protected static final String[] JORNADAS = { "Mañana", "Tarde", "Única", "Noche" };

    private static final String LISTA = "LISTA";
    private static final String FORM = "FORM";
    private static final String CLAVE = "CLAVE";

    private final CardLayout cartas = new CardLayout();
    private final JPanel cuerpo = new JPanel(cartas);
    protected final BarraMensaje barraLista = new BarraMensaje();
    protected final BarraMensaje barraForm = new BarraMensaje();
    private final JLabel tituloForm = Ui.titulo(" ");
    private final BotonSimple btnEditar = new BotonSimple("Editar", BotonSimple.Tipo.SECUNDARIO);
    private final BotonSimple btnEstado = new BotonSimple("Desactivar", BotonSimple.Tipo.PELIGRO);
    private final BotonSimple btnClave = new BotonSimple("Restablecer contraseña", BotonSimple.Tipo.SECUNDARIO);
    private final BarraMensaje barraClave = new BarraMensaje();
    private final JLabel lblClaveDe = Ui.subtitulo(" ");
    private final JPasswordField pfNueva = Ui.campoClave();
    private final JPasswordField pfConfirmar = Ui.campoClave();
    private int idClave = -1;
    private DefaultTableModel modelo;
    private JTable tabla;
    private List<Object[]> filas = new ArrayList<>();
    private Integer idEdicion = null;
    private boolean confirmandoEstado = false;

    protected abstract String titulo();

    protected abstract String subtitulo();

    protected abstract String textoNuevo();

    protected abstract String tituloFormNuevo();

    protected abstract String[] columnas();

    protected abstract int[] anchos();

    protected abstract List<Object[]> consultar() throws DatosException;

    protected abstract Component construirCampos();

    protected abstract void prepararFormulario(Object[] fila) throws DatosException;

    protected abstract void guardar(Integer idEdicion) throws DatosException;

    protected boolean tieneEstado() {
        return true;
    }

    protected boolean permiteRestablecerClave() {
        return false;
    }

    protected void restablecerClave(int idUsuario, String nueva) throws DatosException {
        new UsuarioDAO().cambiarContrasena(idUsuario, nueva);
    }

    protected List<Component> botonesExtra() {
        return new ArrayList<>();
    }

    protected void alSeleccionar(Object[] fila) {
    }

    protected final Object[] filaActual() {
        return seleccionada();
    }

    protected boolean permiteEditar() {
        return false;
    }

    protected String tituloFormEditar() {
        return "Editar";
    }

    protected String etiquetaGuardar() {
        return "Guardar";
    }

    protected String mensajeGuardado(boolean nuevo) {
        return nuevo ? "Registro creado correctamente." : "Registro actualizado correctamente.";
    }

    protected String estadoActivo() {
        return "ACTIVO";
    }

    protected String estadoInactivo() {
        return "INACTIVO";
    }

    protected String textoActivar() {
        return "Activar";
    }

    protected String textoDesactivar() {
        return "Desactivar";
    }

    protected String confirmacionEstado(boolean activar) {
        return null;
    }

    protected String mensajeEstado(boolean activo) {
        return "Registro " + (activo ? "activado" : "desactivado") + ".";
    }

    protected void cambiarEstado(int id, String nuevoEstado) throws DatosException {
        throw new UnsupportedOperationException("Esta sección no cambia estados.");
    }

    protected static int idInstitucion() {
        Usuario u = Sesion.getUsuarioActual();
        if (u == null || u.getIdInstitucion() == null) {
            throw new IllegalStateException("Tu usuario no está asociado a ninguna institución.");
        }
        return u.getIdInstitucion();
    }

    protected static String nulo(String s) {
        String t = s == null ? "" : s.trim();
        return t.isEmpty() ? null : t;
    }

    protected static JLabel seccion(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(Estilo.fuentePx(Estilo.NEGRITA, 15f));
        l.setForeground(Estilo.AZUL);
        l.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
        l.setAlignmentX(LEFT_ALIGNMENT);
        return l;
    }

    protected static JPanel apilar(JComponent... partes) {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        for (JComponent c : partes) {
            c.setAlignmentX(LEFT_ALIGNMENT);
            p.add(c);
        }
        return p;
    }

    protected static JPanel formulario(int columnas, Object... partes) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.anchor = GridBagConstraints.NORTHWEST;
        g.weightx = 1;
        g.insets = new Insets(8, 8, 8, 8);
        int col = 0;
        int fila = 0;
        for (Object parte : partes) {
            if (parte instanceof String || parte instanceof JLabel) {
                if (col != 0) {
                    fila++;
                    col = 0;
                }
                g.gridx = 0;
                g.gridy = fila;
                g.gridwidth = columnas;
                JLabel t = parte instanceof JLabel ? (JLabel) parte : seccion((String) parte);
                p.add(t, g);
                fila++;
                g.gridwidth = 1;
            } else {
                JComponent[] celdas = parte instanceof JComponent[]
                        ? (JComponent[]) parte
                        : new JComponent[] { (JComponent) parte };
                for (JComponent c : celdas) {
                    g.gridx = col;
                    g.gridy = fila;
                    g.gridwidth = 1;
                    p.add(c, g);
                    col++;
                    if (col == columnas) {
                        col = 0;
                        fila++;
                    }
                }
            }
        }
        return p;
    }

    protected final void iniciar() {
        setLayout(new BorderLayout());
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(28, 32, 28, 32));
        cuerpo.setOpaque(false);
        cuerpo.add(construirLista(), LISTA);
        cuerpo.add(construirFormulario(), FORM);
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

    private JPanel construirLista() {
        JPanel p = new JPanel(new BorderLayout(0, 16));
        p.setOpaque(false);

        JPanel norte = new JPanel(new BorderLayout());
        norte.setOpaque(false);
        norte.add(encabezado(Ui.titulo(titulo()), Ui.subtitulo(subtitulo()), barraLista), BorderLayout.CENTER);
        BotonSimple nuevo = new BotonSimple("+ " + textoNuevo(), BotonSimple.Tipo.PRIMARIO);
        nuevo.addActionListener(e -> abrirFormulario(null));
        JPanel derecha = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        derecha.setOpaque(false);
        derecha.add(nuevo);
        norte.add(derecha, BorderLayout.EAST);
        p.add(norte, BorderLayout.NORTH);

        modelo = new DefaultTableModel(columnas(), 0) {
            @Override
            public boolean isCellEditable(int fila, int col) {
                return false;
            }
        };
        tabla = new JTable(modelo);
        Ui.estilizarTabla(tabla);
        int[] an = anchos();
        for (int i = 0; i < an.length && i < tabla.getColumnCount(); i++) {
            tabla.getColumnModel().getColumn(i).setPreferredWidth(an[i]);
        }
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.getSelectionModel().addListSelectionListener(e -> actualizarBotones());
        tabla.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && permiteEditar() && seleccionada() != null) {
                    abrirFormulario(seleccionada());
                }
            }
        });
        Tarjeta tarjeta = new Tarjeta(new BorderLayout());
        tarjeta.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        tarjeta.add(Ui.scroll(tabla), BorderLayout.CENTER);
        p.add(tarjeta, BorderLayout.CENTER);

        JPanel sur = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        sur.setOpaque(false);
        if (permiteEditar()) {
            btnEditar.addActionListener(e -> {
                if (seleccionada() != null) {
                    abrirFormulario(seleccionada());
                }
            });
            sur.add(btnEditar);
        }
        if (tieneEstado()) {
            btnEstado.addActionListener(e -> alternarEstado());
            sur.add(btnEstado);
        }
        if (permiteRestablecerClave()) {
            btnClave.addActionListener(e -> abrirClave());
            sur.add(btnClave);
        }
        for (Component extra : botonesExtra()) {
            sur.add(extra);
        }
        BotonExportar exportar = new BotonExportar(
                () -> TablaExportable.de(titulo(), "", modelo), barraLista);
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

    private JPanel construirFormulario() {
        JPanel p = new JPanel(new BorderLayout(0, 16));
        p.setOpaque(false);
        p.add(encabezado(tituloForm, Ui.subtitulo("Los campos con * son obligatorios"), barraForm),
                BorderLayout.NORTH);

        Tarjeta tarjeta = new Tarjeta(new BorderLayout());
        tarjeta.add(construirCampos(), BorderLayout.NORTH);
        JPanel envoltura = new JPanel(new BorderLayout());
        envoltura.setOpaque(false);
        envoltura.add(tarjeta, BorderLayout.NORTH);
        JScrollPane sp = new JScrollPane(envoltura);
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.getVerticalScrollBar().setUnitIncrement(16);
        p.add(sp, BorderLayout.CENTER);

        BotonSimple guardar = new BotonSimple(etiquetaGuardar(), BotonSimple.Tipo.PRIMARIO);
        BotonSimple cancelar = new BotonSimple("Cancelar", BotonSimple.Tipo.SECUNDARIO);
        guardar.addActionListener(e -> clicGuardar());
        cancelar.addActionListener(e -> cartas.show(cuerpo, LISTA));
        JPanel sur = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        sur.setOpaque(false);
        sur.add(guardar);
        sur.add(cancelar);
        p.add(sur, BorderLayout.SOUTH);
        return p;
    }

    private JPanel construirClave() {
        JPanel p = new JPanel(new BorderLayout(0, 16));
        p.setOpaque(false);
        p.add(encabezado(Ui.titulo("Restablecer contraseña"), lblClaveDe, barraClave), BorderLayout.NORTH);

        Tarjeta tarjeta = new Tarjeta(new BorderLayout());
        tarjeta.add(Ui.rejilla(2,
                Ui.conEtiqueta("Nueva contraseña * (mínimo 6 caracteres)", pfNueva),
                Ui.conEtiqueta("Confirmar contraseña *", pfConfirmar)), BorderLayout.NORTH);
        p.add(tarjeta, BorderLayout.CENTER);

        BotonSimple guardar = new BotonSimple("Guardar contraseña", BotonSimple.Tipo.PRIMARIO);
        BotonSimple cancelar = new BotonSimple("Cancelar", BotonSimple.Tipo.SECUNDARIO);
        guardar.addActionListener(e -> guardarClave());
        cancelar.addActionListener(e -> cartas.show(cuerpo, LISTA));
        JPanel sur = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        sur.setOpaque(false);
        sur.add(guardar);
        sur.add(cancelar);
        p.add(sur, BorderLayout.SOUTH);
        return p;
    }

    private void abrirClave() {
        Object[] f = seleccionada();
        if (f == null) {
            return;
        }
        idClave = (Integer) f[0];
        lblClaveDe.setText("Usuario: " + f[1]);
        pfNueva.setText("");
        pfConfirmar.setText("");
        barraClave.limpiar();
        barraLista.limpiar();
        cartas.show(cuerpo, CLAVE);
        pfNueva.requestFocusInWindow();
    }

    private void guardarClave() {
        String nueva = new String(pfNueva.getPassword());
        if (nueva.length() < 6) {
            barraClave.error("La contraseña debe tener mínimo 6 caracteres.");
            return;
        }
        if (!nueva.equals(new String(pfConfirmar.getPassword()))) {
            barraClave.error("Las contraseñas no coinciden.");
            return;
        }
        try {
            restablecerClave(idClave, nueva);
            refrescar();
            barraLista.exito("Contraseña restablecida. La persona ya puede ingresar con la nueva.");
        } catch (DatosException ex) {
            barraClave.error("No se pudo restablecer la contraseña: " + ex.getMessage());
        }
    }

    private Object[] seleccionada() {
        int fila = tabla.getSelectedRow();
        return (fila >= 0 && fila < filas.size()) ? filas.get(fila) : null;
    }

    private void actualizarBotones() {
        confirmandoEstado = false;
        Object[] f = seleccionada();
        btnEditar.setEnabled(f != null);
        btnEstado.setEnabled(f != null);
        btnClave.setEnabled(f != null);
        boolean activo = f == null || estadoActivo().equals(f[f.length - 1]);
        btnEstado.setText(activo ? textoDesactivar() : textoActivar());
        alSeleccionar(f);
    }

    public void cargar(List<Object[]> lista) {
        filas = new ArrayList<>(lista);
        modelo.setRowCount(0);
        for (Object[] f : filas) {
            Object[] visibles = new Object[f.length - 1];
            for (int i = 1; i < f.length; i++) {
                visibles[i - 1] = f[i] == null ? "" : f[i];
            }
            modelo.addRow(visibles);
        }
        actualizarBotones();
    }

    @Override
    public void refrescar() {
        cartas.show(cuerpo, LISTA);
        try {
            cargar(consultar());
            barraLista.limpiar();
        } catch (IllegalStateException ex) {
            barraLista.error(ex.getMessage());
        } catch (DatosException ex) {
            barraLista.error("No se pudo cargar la información. Revisa la carpeta datos del proyecto. ("
                    + ex.getMessage() + ")");
        }
    }

    private void abrirFormulario(Object[] fila) {
        try {
            prepararFormulario(fila);
        } catch (IllegalStateException ex) {
            barraLista.error(ex.getMessage());
            return;
        } catch (DatosException ex) {
            barraLista.error("No se pudo abrir el formulario: " + ex.getMessage());
            return;
        }
        idEdicion = fila == null ? null : (Integer) fila[0];
        tituloForm.setText(fila == null ? tituloFormNuevo() : tituloFormEditar());
        barraForm.limpiar();
        barraLista.limpiar();
        cartas.show(cuerpo, FORM);
        revalidate();
    }

    private void clicGuardar() {
        try {
            boolean nuevo = idEdicion == null;
            guardar(idEdicion);
            refrescar();
            barraLista.exito(mensajeGuardado(nuevo));
        } catch (IllegalArgumentException | IllegalStateException ex) {
            barraForm.error(ex.getMessage());
        } catch (DatosException ex) {
            barraForm.error(ex.getMessage());
        }
    }

    private void alternarEstado() {
        Object[] f = seleccionada();
        if (f == null) {
            return;
        }
        String nuevo = estadoActivo().equals(f[f.length - 1]) ? estadoInactivo() : estadoActivo();
        String pregunta = confirmacionEstado(estadoActivo().equals(nuevo));
        if (pregunta != null && !confirmandoEstado) {
            confirmandoEstado = true;
            btnEstado.setText(pregunta);
            return;
        }
        confirmandoEstado = false;
        try {
            cambiarEstado((Integer) f[0], nuevo);
            refrescar();
            barraLista.exito(mensajeEstado(estadoActivo().equals(nuevo)));
        } catch (IllegalStateException ex) {
            barraLista.error(ex.getMessage());
        } catch (DatosException ex) {
            barraLista.error("No se pudo cambiar el estado: " + ex.getMessage());
        }
    }
}
