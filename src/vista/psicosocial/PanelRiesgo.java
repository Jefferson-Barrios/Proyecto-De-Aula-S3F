package vista.psicosocial;

import controlador.AnalisisRiesgo;
import controlador.CierrePeriodo.Evaluacion;
import dao.CierrePeriodoDAO;
import dao.PeriodoDAO;
import eipi.util.TablaExportable;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import eipi.util.DatosException;
import java.util.ArrayList;
import java.util.Comparator;
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
import vista.componentes.BotonExportar;
import vista.componentes.Opcion;
import vista.componentes.Tarjeta;
import vista.componentes.Ui;

public class PanelRiesgo extends JPanel implements Refrescable {

    private static final String SOLO_RIESGO = "Solo con alerta";
    private static final String TODOS = "Todos";

    private final BarraMensaje barra = new BarraMensaje();
    private final JComboBox<Opcion> cbPeriodo = Ui.combo();
    private final JComboBox<String> cbFiltro = Ui.combo();
    private final DefaultTableModel modelo = new DefaultTableModel(
            new Object[] { "Estudiante", "Curso", "Asistencia", "Perdidas", "Materias perdidas", "Clasificación" }, 0) {
        @Override
        public boolean isCellEditable(int fila, int col) {
            return false;
        }
    };
    private final JTable tabla = new JTable(modelo);
    private boolean cargando = false;

    public PanelRiesgo() {
        setLayout(new BorderLayout(0, 16));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(28, 32, 28, 32));
        cbFiltro.addItem(SOLO_RIESGO);
        cbFiltro.addItem(TODOS);

        JPanel norte = new JPanel();
        norte.setOpaque(false);
        norte.setLayout(new BoxLayout(norte, BoxLayout.Y_AXIS));
        JLabel t = Ui.titulo("Seguimiento de riesgo");
        JLabel s = Ui.subtitulo(
                "Así quedaría cada estudiante si el período se cerrara hoy (materias perdidas = promedio menor a 3,0)");
        t.setAlignmentX(LEFT_ALIGNMENT);
        s.setAlignmentX(LEFT_ALIGNMENT);
        barra.setAlignmentX(LEFT_ALIGNMENT);
        norte.add(t);
        norte.add(s);
        norte.add(barra);
        add(norte, BorderLayout.NORTH);

        Ui.estilizarTabla(tabla);
        int[] an = { 230, 70, 100, 80, 280, 130 };
        for (int i = 0; i < an.length; i++) {
            tabla.getColumnModel().getColumn(i).setPreferredWidth(an[i]);
        }
        Tarjeta tarjeta = new Tarjeta(new BorderLayout(0, 8));
        tarjeta.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        tarjeta.add(Ui.rejilla(2, Ui.conEtiqueta("Período", cbPeriodo), Ui.conEtiqueta("Mostrar", cbFiltro)),
                BorderLayout.NORTH);
        tarjeta.add(Ui.scroll(tabla), BorderLayout.CENTER);
        add(tarjeta, BorderLayout.CENTER);

        BotonExportar exportar = new BotonExportar(
                () -> TablaExportable.de("Seguimiento de riesgo",
                        TablaExportable.unir(cbPeriodo.getSelectedItem(), cbFiltro.getSelectedItem()), modelo),
                barra);
        JPanel pie = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        pie.setOpaque(false);
        pie.add(exportar);
        add(pie, BorderLayout.SOUTH);

        cbPeriodo.addActionListener(e -> alCambiar());
        cbFiltro.addActionListener(e -> alCambiar());
    }

    private void alCambiar() {
        if (!cargando && cbPeriodo.getItemCount() > 0) {
            barra.limpiar();
            analizar();
        }
    }

    private static int gravedad(String nivel) {
        switch (nivel) {
            case AnalisisRiesgo.ALTO:
                return 0;
            case AnalisisRiesgo.MEDIO:
                return 1;
            case AnalisisRiesgo.PREVENTIVA:
                return 2;
            default:
                return 3;
        }
    }

    public void mostrar(List<Evaluacion> evaluaciones, boolean soloAlerta) {
        List<Evaluacion> lista = new ArrayList<>();
        for (Evaluacion e : evaluaciones) {
            if (!soloAlerta || AnalisisRiesgo.generaAlerta(e.nivel)) {
                lista.add(e);
            }
        }
        lista.sort(Comparator.comparingInt((Evaluacion e) -> gravedad(e.nivel)).thenComparing(e -> e.nombre));
        modelo.setRowCount(0);
        for (Evaluacion e : lista) {
            modelo.addRow(new Object[] { e.nombre, e.curso == null ? "" : e.curso,
                    AnalisisRiesgo.formatoPorcentaje(e.asistencia), e.materiasPerdidas.size(),
                    String.join(", ", e.materiasPerdidas), e.nivel });
        }
    }

    private void analizar() {
        Opcion op = (Opcion) cbPeriodo.getSelectedItem();
        if (op == null) {
            return;
        }
        try {
            mostrar(new CierrePeriodoDAO().evaluarProvisional(Contexto.idInstitucion(),
                    (PeriodoAcademico) op.getDato()),
                    SOLO_RIESGO.equals(cbFiltro.getSelectedItem()));
        } catch (IllegalStateException ex) {
            barra.error(ex.getMessage());
        } catch (DatosException ex) {
            barra.error("No se pudo analizar el riesgo: " + ex.getMessage());
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
            barra.error("La Directiva aún no ha creado períodos académicos.");
            return;
        }
        barra.limpiar();
        analizar();
    }
}
