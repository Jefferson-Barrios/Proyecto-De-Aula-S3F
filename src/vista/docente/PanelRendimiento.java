package vista.docente;

import controlador.AnalisisRiesgo;
import dao.AsistenciaDAO;
import dao.CalificacionDAO;
import eipi.util.Formato;
import eipi.util.DatosException;
import java.util.List;
import java.util.Map;
import modelo.PeriodoAcademico;

public class PanelRendimiento extends PanelBaseDocente {

    private final CalificacionDAO calificacionDAO = new CalificacionDAO();
    private final AsistenciaDAO asistenciaDAO = new AsistenciaDAO();

    public PanelRendimiento() {
        iniciar();
    }

    @Override
    protected String titulo() {
        return "Rendimiento";
    }

    @Override
    protected String subtitulo() {
        return "Promedio y asistencia de tus estudiantes en este período. Las alertas las genera el sistema al cerrarlo";
    }

    @Override
    protected boolean usaPeriodo() {
        return true;
    }

    @Override
    protected String[] columnas() {
        return new String[] { "Estudiante", "Promedio", "Asistencia", "Situación" };
    }

    @Override
    protected int[] anchos() {
        return new int[] { 300, 110, 130, 160 };
    }

    static String situacion(Double promedio) {
        if (promedio == null) {
            return "SIN NOTAS";
        }
        return AnalisisRiesgo.esMateriaPerdida(promedio) ? "VA PERDIENDO" : "VA APROBANDO";
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void cargarTabla() throws DatosException {
        PeriodoAcademico per = (PeriodoAcademico) periodo().getDato();
        List<Object[]> resumen = calificacionDAO.listarResumen(asignacion().getId(), per.getIdPeriodo());
        Map<Integer, int[]> asistencia = asistenciaDAO.resumenInasistencia(
                asignacion().getId(), per.getFechaInicio(), per.getFechaFin());

        modelo.setRowCount(0);
        for (Object[] r : resumen) {
            Double promedio = AnalisisRiesgo.calcularPromedio((List<Double>) r[2]);
            int[] asis = asistencia.get((Integer) r[0]);
            Double pct = asis == null ? null : AnalisisRiesgo.porcentajeAsistencia(asis[0], asis[1]);
            modelo.addRow(new Object[] { r[1],
                    promedio == null ? "—" : Formato.nota(AnalisisRiesgo.redondear(promedio)),
                    AnalisisRiesgo.formatoPorcentaje(pct),
                    situacion(promedio) });
        }
    }
}
