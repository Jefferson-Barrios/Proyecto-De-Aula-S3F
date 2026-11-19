package dao;

import eipi.util.BaseDatos;
import eipi.util.DatosException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NotaPeriodoDAO {

    public static class Matriz {
        public final List<String> materias = new ArrayList<>();
        public final List<String> estudiantes = new ArrayList<>();

        public Double[][] notas = new Double[0][0];
    }

    public Matriz matriz(int idCurso, int idPeriodo) throws DatosException {
        Matriz m = new Matriz();
        Map<Integer, Integer> columnaDeAsignacion = new HashMap<>();
        Map<Integer, Integer> filaDeEstudiante = new HashMap<>();

        Map<Object, Map<String, Object>> materias = BaseDatos.indice(BaseDatos.MATERIA, "id_materia");
        List<Object[]> columnas = new ArrayList<>();
        for (Map<String, Object> a : BaseDatos.tabla(BaseDatos.ASIGNACION_DOCENTE)) {
            if (!BaseDatos.igual(BaseDatos.entero(a, "id_curso"), idCurso)
                    || !"ACTIVO".equals(BaseDatos.texto(a, "estado"))) {
                continue;
            }
            Map<String, Object> mat = materias.get(BaseDatos.entero(a, "id_materia"));
            columnas.add(new Object[] {
                    mat == null ? null : BaseDatos.texto(mat, "nombre"),
                    BaseDatos.enteroPrimitivo(a, "id_asignacion") });
        }
        columnas.sort((a, b) -> {
            int c = Ordenes.limpio((String) a[0]).compareToIgnoreCase(Ordenes.limpio((String) b[0]));
            return c != 0 ? c : Integer.compare((Integer) a[1], (Integer) b[1]);
        });
        for (Object[] col : columnas) {
            columnaDeAsignacion.put((Integer) col[1], m.materias.size());
            m.materias.add((String) col[0]);
        }

        Map<Object, Map<String, Object>> usuarios = BaseDatos.indice(BaseDatos.USUARIO, "id_usuario");
        List<Object[]> filas = new ArrayList<>();
        for (Map<String, Object> e : BaseDatos.tabla(BaseDatos.ESTUDIANTE)) {
            if (!BaseDatos.igual(BaseDatos.entero(e, "id_curso"), idCurso)) {
                continue;
            }
            Map<String, Object> u = usuarios.get(BaseDatos.entero(e, "id_usuario"));
            if (u == null || !"ACTIVO".equals(BaseDatos.texto(u, "estado"))) {
                continue;
            }
            filas.add(new Object[] {
                    BaseDatos.enteroPrimitivo(e, "id_usuario"),
                    BaseDatos.texto(u, "nombre") });
        }
        filas.sort((a, b) -> Ordenes.limpio((String) a[1]).compareToIgnoreCase(Ordenes.limpio((String) b[1])));
        for (Object[] f : filas) {
            filaDeEstudiante.put((Integer) f[0], m.estudiantes.size());
            m.estudiantes.add((String) f[1]);
        }

        m.notas = new Double[m.estudiantes.size()][m.materias.size()];
        for (Map<String, Object> np : BaseDatos.tabla(BaseDatos.NOTA_PERIODO)) {
            if (!BaseDatos.igual(BaseDatos.entero(np, "id_periodo"), idPeriodo)) {
                continue;
            }
            Integer fila = filaDeEstudiante.get(BaseDatos.entero(np, "id_estudiante"));
            Integer col = columnaDeAsignacion.get(BaseDatos.entero(np, "id_asignacion"));
            if (fila != null && col != null) {
                m.notas[fila][col] = BaseDatos.decimal(np, "nota_definitiva");
            }
        }
        return m;
    }
}
