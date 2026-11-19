package vista.estudiante;

import java.awt.FlowLayout;
import vista.componentes.BotonExportar;
import eipi.util.TablaExportable;
import controlador.AnalisisRiesgo;
import dao.CalificacionDAO;
import dao.PeriodoDAO;
import eipi.util.Formato;
import java.awt.BorderLayout;
import eipi.util.DatosException;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import modelo.PeriodoAcademico;
import vista.Refrescable;
import vista.componentes.BarraMensaje;
import vista.componentes.Opcion;
import vista.componentes.Tarjeta;
import vista.componentes.Ui;

public class PanelNotasEstudiante extends JPanel implements Refrescable {

    private final BarraMensaje barra = new BarraMensaje();
    private final JComboBox<Opcion> cbPeriodo = Ui.combo();
    private final DefaultTableModel modelo = new DefaultTableModel(
            new Object[] { "Materia", "Notas del período", "Promedio", "Definitiva" }, 0) {
        @Override
        public boolean isCellEditable(int fila, int col) {
            return false;
        }
    };
    private boolean cargando = false;

    public PanelNotasEstudiante() {
        setLayout(new BorderLayout(0, 16));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(28, 32, 28, 32));

        JPanel norte = new JPanel();
        norte.setOpaque(false);
        norte.setLayout(new BoxLayout(norte, BoxLayout.Y_AXIS));
        JLabel t = Ui.titulo("Mis calificaciones");
        JLabel s = Ui.subtitulo("La nota definitiva aparece cuando tu docente la guarda al cerrar el período");
        t.setAlignmentX(LEFT_ALIGNMENT);
        s.setAlignmentX(LEFT_ALIGNMENT);
        barra.setAlignmentX(LEFT_ALIGNMENT);
        norte.add(t);
        norte.add(s);
        norte.add(barra);
        add(norte, BorderLayout.NORTH);

        JTable tabla = new JTable(modelo);
        Ui.estilizarTabla(tabla);
        int[] an = { 240, 360, 100, 110 };
        for (int i = 0; i < an.length; i++) {
            tabla.getColumnModel().getColumn(i).setPreferredWidth(an[i]);
        }
        Tarjeta tarjeta = new Tarjeta(new BorderLayout(0, 8));
        tarjeta.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        tarjeta.add(Ui.rejilla(2, Ui.conEtiqueta("Período", cbPeriodo)), BorderLayout.NORTH);
        tarjeta.add(Ui.scroll(tabla), BorderLayout.CENTER);
        add(tarjeta, BorderLayout.CENTER);

        BotonExportar exportar = new BotonExportar(() -> {
            modelo.Usuario u = eipi.util.Sesion.getUsuarioActual();
            return TablaExportable.de("Boletín de calificaciones",
                    (u == null ? "" : u.getNombre() + "  ·  ") + cbPeriodo.getSelectedItem(), modelo);
        }, barra);
        JPanel pie = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        pie.setOpaque(false);
        pie.add(exportar);
        add(pie, BorderLayout.SOUTH);

        cbPeriodo.addActionListener(e -> {
            if (!cargando && cbPeriodo.getItemCount() > 0) {
                cargarNotas();
            }
        });
    }

    @SuppressWarnings("unchecked")
    public void mostrar(List<Object[]> filas) {
        modelo.setRowCount(0);
        for (Object[] f : filas) {
            List<Double> notas = (List<Double>) f[1];
            Double promedio = AnalisisRiesgo.calcularPromedio(notas);
            StringBuilder sb = new StringBuilder();
            for (Double n : notas) {
                if (sb.length() > 0) {
                    sb.append("  ·  ");
                }
                sb.append(Formato.nota(n));
            }
            modelo.addRow(new Object[] { f[0], notas.isEmpty() ? "Sin notas" : sb.toString(),
                    promedio == null ? "—" : Formato.nota(promedio),
                    f[2] == null ? "—" : Formato.nota((Double) f[2]) });
        }
    }

    private void cargarNotas() {
        Opcion op = (Opcion) cbPeriodo.getSelectedItem();
        if (op == null) {
            return;
        }
        try {
            mostrar(new CalificacionDAO().listarDeEstudiante(Contexto.idEstudiante(), op.getId()));
            barra.limpiar();
        } catch (IllegalStateException ex) {
            barra.error(ex.getMessage());
        } catch (DatosException ex) {
            barra.error("No se pudieron cargar las notas: " + ex.getMessage());
        }
    }

    @Override
    public void refrescar() {
        cargando = true;
        try {
            int anterior = cbPeriodo.getSelectedItem() == null ? -1 : ((Opcion) cbPeriodo.getSelectedItem()).getId();
            List<PeriodoAcademico> periodos = new PeriodoDAO().listar(Contexto.idInstitucion());
            cbPeriodo.removeAllItems();
            Opcion elegida = null;
            Opcion abierta = null;
            for (PeriodoAcademico p : periodos) {
                boolean ab = "ABIERTO".equals(p.getEstado());
                Opcion o = new Opcion(p.getIdPeriodo(), p.getNombre() + (ab ? "" : " (cerrado)"), p);
                cbPeriodo.addItem(o);
                if (o.getId() == anterior) {
                    elegida = o;
                }
                if (ab && abierta == null) {
                    abierta = o;
                }
            }
            if (elegida != null) {
                cbPeriodo.setSelectedItem(elegida);
            } else if (abierta != null) {
                cbPeriodo.setSelectedItem(abierta);
            }
        } catch (IllegalStateException ex) {
            barra.error(ex.getMessage());
            return;
        } catch (DatosException ex) {
            barra.error("No se pudo cargar la información. Revisa la carpeta datos del proyecto. ("
                    + ex.getMessage() + ")");
            return;
        } finally {
            cargando = false;
        }
        if (cbPeriodo.getItemCount() == 0) {
            modelo.setRowCount(0);
            barra.error("Tu institución aún no ha creado períodos académicos.");
            return;
        }
        cargarNotas();
    }
}
