package vista.docente;

import dao.AsistenciaDAO;
import eipi.util.Formato;
import java.awt.FlowLayout;
import eipi.util.DatosException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTable;
import vista.componentes.BotonSimple;
import vista.componentes.Estilo;

public class PanelAsistencia extends PanelBaseDocente {

    private static final String[] ESTADOS = { "PRESENTE", "AUSENTE", "EXCUSA", "TARDE" };

    private final AsistenciaDAO asistenciaDAO = new AsistenciaDAO();
    private List<Object[]> datos = new ArrayList<>();

    public PanelAsistencia() {
        iniciar();
    }

    @Override
    protected String titulo() {
        return "Asistencia";
    }

    @Override
    protected String subtitulo() {
        return "Marca la asistencia de cada estudiante en la fecha elegida";
    }

    @Override
    protected boolean usaFecha() {
        return true;
    }

    @Override
    protected String[] columnas() {
        return new String[] { "Estudiante", "Estado", "Observación" };
    }

    @Override
    protected int[] anchos() {
        return new int[] { 300, 140, 400 };
    }

    @Override
    protected boolean celdaEditable(int fila, int columna) {
        return columna == 1 || columna == 2;
    }

    @Override
    protected void configurarTabla(JTable t) {
        JComboBox<String> combo = new JComboBox<>(ESTADOS);
        combo.setFont(Estilo.fuentePx(Estilo.REGULAR, 13f));
        t.getColumnModel().getColumn(1).setCellEditor(new DefaultCellEditor(combo));
    }

    @Override
    protected void cargarTabla() throws DatosException {
        datos = asistenciaDAO.listar(asignacion().getId(), fecha());
        modelo.setRowCount(0);
        for (Object[] f : datos) {
            modelo.addRow(new Object[] { f[1], f[2] == null ? "PRESENTE" : f[2], f[3] == null ? "" : f[3] });
        }
    }

    @Override
    protected JComponent construirBotones() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        p.setOpaque(false);
        BotonSimple guardar = new BotonSimple("Guardar asistencia", BotonSimple.Tipo.PRIMARIO);
        BotonSimple todos = new BotonSimple("Marcar todos presentes", BotonSimple.Tipo.SECUNDARIO);
        guardar.addActionListener(e -> guardar());
        todos.addActionListener(e -> {
            detenerEdicion();
            for (int i = 0; i < modelo.getRowCount(); i++) {
                modelo.setValueAt("PRESENTE", i, 1);
            }
        });
        p.add(guardar);
        p.add(todos);
        return p;
    }

    private void detenerEdicion() {
        if (tabla.isEditing()) {
            tabla.getCellEditor().stopCellEditing();
        }
    }

    private void guardar() {
        detenerEdicion();
        if (datos.isEmpty()) {
            barra.error("No hay estudiantes en este curso.");
            return;
        }
        LocalDate dia;
        try {
            dia = fecha();
        } catch (IllegalArgumentException ex) {
            barra.error(ex.getMessage());
            return;
        }
        if (dia.isAfter(LocalDate.now())) {
            barra.error("No se puede registrar asistencia de una fecha futura.");
            return;
        }
        List<Object[]> filas = new ArrayList<>();
        for (int i = 0; i < datos.size(); i++) {
            String obs = String.valueOf(modelo.getValueAt(i, 2)).trim();
            if (obs.length() > 255) {
                barra.error("La observación de " + modelo.getValueAt(i, 0) + " supera los 255 caracteres.");
                return;
            }
            filas.add(new Object[] { datos.get(i)[0], String.valueOf(modelo.getValueAt(i, 1)),
                    obs.isEmpty() ? null : obs });
        }
        try {
            asistenciaDAO.guardar(asignacion().getId(), dia, filas);
            cargarTabla();
            barra.exito("Asistencia del " + Formato.texto(dia) + " guardada.");
        } catch (DatosException ex) {
            barra.error("No se pudo guardar la asistencia: " + ex.getMessage());
        }
    }
}
