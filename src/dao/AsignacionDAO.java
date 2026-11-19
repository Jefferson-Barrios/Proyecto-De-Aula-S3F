package dao;

import eipi.util.BaseDatos;
import eipi.util.DatosException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class AsignacionDAO {

    private static final String TABLA = BaseDatos.ASIGNACION_DOCENTE;

    private static final Comparator<Object[]> ORDEN = Comparator
            .comparingInt((Object[] f) -> Ordenes.grado((String) f[2]))
            .thenComparing(f -> Ordenes.limpio((String) f[2]))
            .thenComparing(f -> Ordenes.limpio((String) f[3]));

    public List<Object[]> listar(int idInstitucion) throws DatosException {
        Map<Object, Map<String, Object>> usuarios = BaseDatos.indice(BaseDatos.USUARIO, "id_usuario");
        Map<Object, Map<String, Object>> cursos = BaseDatos.indice(BaseDatos.CURSO, "id_curso");
        Map<Object, Map<String, Object>> materias = BaseDatos.indice(BaseDatos.MATERIA, "id_materia");
        List<Object[]> filas = new ArrayList<>();
        for (Map<String, Object> a : BaseDatos.tabla(TABLA)) {
            Map<String, Object> c = cursos.get(BaseDatos.entero(a, "id_curso"));
            if (c == null || !BaseDatos.igual(BaseDatos.entero(c, "id_institucion"), idInstitucion)) {
                continue;
            }
            Map<String, Object> m = materias.get(BaseDatos.entero(a, "id_materia"));
            Map<String, Object> d = usuarios.get(BaseDatos.entero(a, "id_docente"));
            filas.add(new Object[] {
                    BaseDatos.enteroPrimitivo(a, "id_asignacion"),
                    d == null ? null : BaseDatos.texto(d, "nombre"),
                    BaseDatos.texto(c, "grado") + "-" + BaseDatos.texto(c, "grupo"),
                    m == null ? null : BaseDatos.texto(m, "nombre"),
                    BaseDatos.texto(a, "jornada"),
                    BaseDatos.texto(a, "estado") });
        }
        filas.sort(ORDEN);
        return filas;
    }

    public List<Object[]> listarDeDocente(int idDocente) throws DatosException {
        Map<Object, Map<String, Object>> cursos = BaseDatos.indice(BaseDatos.CURSO, "id_curso");
        Map<Object, Map<String, Object>> materias = BaseDatos.indice(BaseDatos.MATERIA, "id_materia");
        Map<Object, List<Map<String, Object>>> estudiantesPorCurso = BaseDatos.agrupar(BaseDatos.ESTUDIANTE,
                "id_curso");
        Map<Object, Map<String, Object>> usuarios = BaseDatos.indice(BaseDatos.USUARIO, "id_usuario");
        List<Object[]> filas = new ArrayList<>();
        for (Map<String, Object> a : BaseDatos.tabla(TABLA)) {
            if (!BaseDatos.igual(BaseDatos.entero(a, "id_docente"), idDocente)
                    || !"ACTIVO".equals(BaseDatos.texto(a, "estado"))) {
                continue;
            }
            Map<String, Object> c = cursos.get(BaseDatos.entero(a, "id_curso"));
            Map<String, Object> m = materias.get(BaseDatos.entero(a, "id_materia"));
            int estudiantes = 0;
            for (Map<String, Object> e : estudiantesPorCurso.getOrDefault(
                    BaseDatos.entero(a, "id_curso"), new ArrayList<>())) {
                Map<String, Object> u = usuarios.get(BaseDatos.entero(e, "id_usuario"));
                if (u != null && "ACTIVO".equals(BaseDatos.texto(u, "estado"))) {
                    estudiantes++;
                }
            }
            filas.add(new Object[] {
                    BaseDatos.enteroPrimitivo(a, "id_asignacion"),
                    BaseDatos.enteroPrimitivo(a, "id_curso"),
                    c == null ? null : BaseDatos.texto(c, "grado") + "-" + BaseDatos.texto(c, "grupo"),
                    m == null ? null : BaseDatos.texto(m, "nombre"),
                    BaseDatos.texto(a, "jornada"),
                    estudiantes });
        }
        filas.sort(ORDEN);
        return filas;
    }

    public int insertar(int idDocente, int idCurso, int idMateria, String jornada) throws DatosException {
        for (Map<String, Object> a : BaseDatos.tabla(TABLA)) {
            if (BaseDatos.igual(BaseDatos.entero(a, "id_docente"), idDocente)
                    && BaseDatos.igual(BaseDatos.entero(a, "id_curso"), idCurso)
                    && BaseDatos.igual(BaseDatos.entero(a, "id_materia"), idMateria)) {
                throw new DatosException("Ese docente ya tiene asignada esa materia en ese curso.");
            }
        }
        Map<String, Object> fila = BaseDatos.nuevo();
        int id = BaseDatos.nuevoId(TABLA, "id_asignacion");
        fila.put("id_asignacion", id);
        fila.put("id_docente", idDocente);
        fila.put("id_curso", idCurso);
        fila.put("id_materia", idMateria);
        fila.put("jornada", jornada);
        fila.put("estado", "ACTIVO");
        BaseDatos.insertar(TABLA, fila);
        return id;
    }

    public void cambiarEstado(int idAsignacion, String estado) throws DatosException {
        Map<String, Object> fila = BaseDatos.porId(TABLA, "id_asignacion", idAsignacion);
        if (fila != null) {
            fila.put("estado", estado);
            BaseDatos.actualizada(TABLA);
        }
    }
}
