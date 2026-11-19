package vista.docente;

import java.awt.FlowLayout;
import vista.componentes.BotonExportar;
import eipi.util.TablaExportable;
import eipi.util.Formato;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import eipi.util.DatosException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import vista.Refrescable;
import vista.componentes.BarraMensaje;
import vista.componentes.Opcion;
import vista.componentes.Tarjeta;
import vista.componentes.Ui;

public abstract class PanelBaseDocente extends JPanel implements Refrescable {

    private static final String PRINCIPAL = "PRINCIPAL";
    private static final String EXTRA = "EXTRA";

    protected final BarraMensaje barra = new BarraMensaje();
    protected final JComboBox<Opcion> cbAsignacion = Ui.combo();
    protected final JComboBox<Opcion> cbPeriodo = Ui.combo();
    protected final JTextField tfFecha = Ui.campo();
    protected DefaultTableModel modelo;
    protected JTable tabla;

    private final CardLayout cartas = new CardLayout();
    private final JPanel cuerpo = new JPanel(cartas);
    private boolean cargando = false;

    protected abstract String titulo();

    protected abstract String subtitulo();

    protected abstract String[] columnas();

    protected abstract int[] anchos();

    protected abstract void cargarTabla() throws DatosException;

    protected boolean usaPeriodo() {
        return false;
    }

    protected boolean usaFecha() {
        return false;
    }

    protected boolean celdaEditable(int fila, int columna) {
        return false;
    }

    protected void configurarTabla(JTable t) {
    }

    protected JComponent construirBotones() {
        return null;
    }

    protected JComponent construirExtra() {
        return null;
    }

    protected final Opcion asignacion() {
        Opcion o = (Opcion) cbAsignacion.getSelectedItem();
        if (o == null) {
            throw new IllegalStateException("Selecciona una asignación.");
        }
        return o;
    }

    protected final Opcion periodo() {
        Opcion o = (Opcion) cbPeriodo.getSelectedItem();
        if (o == null) {
            throw new IllegalStateException("Selecciona un período.");
        }
        return o;
    }

    protected final LocalDate fecha() {
        try {
            return Formato.parsear(tfFecha.getText());
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("La fecha debe tener el formato dd/mm/aaaa (por ejemplo 15/03/2026).");
        }
    }

    protected String contextoExportacion() {
        StringBuilder sb = new StringBuilder();
        Object a = cbAsignacion.getSelectedItem();
        if (a != null) {
            sb.append(a);
        }
        if (usaPeriodo() && cbPeriodo.getSelectedItem() != null) {
            sb.append("  ·  ").append(cbPeriodo.getSelectedItem());
        }
        if (usaFecha()) {
            sb.append("  ·  ").append(tfFecha.getText().trim());
        }
        return sb.toString();
    }

    protected final void mostrarExtra() {
        cartas.show(cuerpo, EXTRA);
    }

    protected final void mostrarPrincipal() {
        cartas.show(cuerpo, PRINCIPAL);
    }

    protected final int filaSeleccionada() {
        return tabla.getSelectedRow();
    }

    protected final void recargar() {
        try {
            cargarTabla();
        } catch (IllegalArgumentException | IllegalStateException ex) {
            barra.error(ex.getMessage());
        } catch (DatosException ex) {
            barra.error("No se pudo cargar la información: " + ex.getMessage());
        }
    }

    protected final void iniciar() {
        setLayout(new BorderLayout());
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(28, 32, 28, 32));
        cuerpo.setOpaque(false);
        cuerpo.add(construirPrincipal(), PRINCIPAL);
        JComponent extra = construirExtra();
        if (extra != null) {
            cuerpo.add(extra, EXTRA);
        }
        add(cuerpo, BorderLayout.CENTER);

        cbAsignacion.addActionListener(e -> alCambiarSeleccion());
        cbPeriodo.addActionListener(e -> alCambiarSeleccion());
        tfFecha.addActionListener(e -> alCambiarSeleccion());
        tfFecha.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                alCambiarSeleccion();
            }
        });
    }

    private void alCambiarSeleccion() {
        if (cargando || cbAsignacion.getItemCount() == 0) {
            return;
        }
        barra.limpiar();
        recargar();
    }

    private JPanel construirPrincipal() {
        JPanel p = new JPanel(new BorderLayout(0, 16));
        p.setOpaque(false);

        JPanel norte = new JPanel();
        norte.setOpaque(false);
        norte.setLayout(new BoxLayout(norte, BoxLayout.Y_AXIS));
        javax.swing.JLabel t = Ui.titulo(titulo());
        javax.swing.JLabel s = Ui.subtitulo(subtitulo());
        t.setAlignmentX(LEFT_ALIGNMENT);
        s.setAlignmentX(LEFT_ALIGNMENT);
        barra.setAlignmentX(LEFT_ALIGNMENT);
        norte.add(t);
        norte.add(s);
        norte.add(barra);
        p.add(norte, BorderLayout.NORTH);

        JComponent[] selectores;
        if (usaPeriodo() && usaFecha()) {
            selectores = new JComponent[] { Ui.conEtiqueta("Asignación", cbAsignacion),
                    Ui.conEtiqueta("Período", cbPeriodo), Ui.conEtiqueta("Fecha (dd/mm/aaaa)", tfFecha) };
        } else if (usaPeriodo()) {
            selectores = new JComponent[] { Ui.conEtiqueta("Asignación", cbAsignacion),
                    Ui.conEtiqueta("Período", cbPeriodo) };
        } else if (usaFecha()) {
            selectores = new JComponent[] { Ui.conEtiqueta("Asignación", cbAsignacion),
                    Ui.conEtiqueta("Fecha (dd/mm/aaaa)", tfFecha) };
        } else {
            selectores = new JComponent[] { Ui.conEtiqueta("Asignación", cbAsignacion) };
        }

        modelo = new DefaultTableModel(columnas(), 0) {
            @Override
            public boolean isCellEditable(int fila, int col) {
                return celdaEditable(fila, col);
            }
        };
        tabla = new JTable(modelo);
        Ui.estilizarTabla(tabla);
        int[] an = anchos();
        for (int i = 0; i < an.length && i < tabla.getColumnCount(); i++) {
            tabla.getColumnModel().getColumn(i).setPreferredWidth(an[i]);
        }
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        configurarTabla(tabla);

        Tarjeta tarjeta = new Tarjeta(new BorderLayout(0, 8));
        tarjeta.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        tarjeta.add(Ui.rejilla(selectores.length, selectores), BorderLayout.NORTH);
        tarjeta.add(Ui.scroll(tabla), BorderLayout.CENTER);
        p.add(tarjeta, BorderLayout.CENTER);

        JComponent botones = construirBotones();
        BotonExportar exportar = new BotonExportar(
                () -> TablaExportable.de(titulo(), contextoExportacion(), modelo), barra);
        JPanel derecha = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        derecha.setOpaque(false);
        derecha.add(exportar);
        JPanel pie = new JPanel(new BorderLayout());
        pie.setOpaque(false);
        if (botones != null) {
            pie.add(botones, BorderLayout.CENTER);
        }
        pie.add(derecha, BorderLayout.EAST);
        p.add(pie, BorderLayout.SOUTH);
        return p;
    }

    @Override
    public void refrescar() {
        mostrarPrincipal();
        try {
            cargando = true;
            Selectores.cargarAsignaciones(cbAsignacion);
            if (usaPeriodo()) {
                Selectores.cargarPeriodos(cbPeriodo);
            }
            if (usaFecha() && tfFecha.getText().trim().isEmpty()) {
                tfFecha.setText(Formato.texto(LocalDate.now()));
            }
        } catch (IllegalStateException ex) {
            barra.error(ex.getMessage());
            modelo.setRowCount(0);
            return;
        } catch (DatosException ex) {
            barra.error("No se pudo cargar la información. Revisa la carpeta datos del proyecto. ("
                    + ex.getMessage() + ")");
            modelo.setRowCount(0);
            return;
        } finally {
            cargando = false;
        }

        if (cbAsignacion.getItemCount() == 0) {
            modelo.setRowCount(0);
            barra.error("Aún no tienes asignaciones activas. Pide a la Directiva que te asigne materias y cursos.");
            return;
        }
        if (usaPeriodo() && cbPeriodo.getItemCount() == 0) {
            modelo.setRowCount(0);
            barra.error("La Directiva aún no ha creado períodos académicos.");
            return;
        }
        barra.limpiar();
        recargar();
    }
}
