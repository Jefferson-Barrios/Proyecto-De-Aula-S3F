package dao;

import eipi.util.BaseDatos;
import eipi.util.DatosException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public class HorarioDAO {

    private static int ordenDia(String dia) {
        return switch (dia == null ? "" : dia) {
            case "Lunes" -> 1;
            case "Martes" -> 2;
            case "Miércoles" -> 3;
            case "Jueves" -> 4;
            case "Viernes" -> 5;
            case "Sábado" -> 6;
            default -> 7;
        };
    }

    public List<Object[]> listar(int idInstitucion) throws DatosException {
        Map<Object, Map<String, Object>> asignaciones = BaseDatos.indice(BaseDatos.ASIGNACION_DOCENTE, "id_asignacion");
        Map<Object, Map<String, Object>> cursos = BaseDatos.indice(BaseDatos.CURSO, "id_curso");
        Map<Object, Map<String, Object>> materias = BaseDatos.indice(BaseDatos.MATERIA, "id_materia");
        Map<Object, Map<String, Object>> usuarios = BaseDatos.indice(BaseDatos.USUARIO, "id_usuario");
        List<Object[]> filas = new ArrayList<>();
        for (Map<String, Object> h : BaseDatos.tabla(BaseDatos.HORARIO)) {
            Map<String, Object> a = asignaciones.get(BaseDatos.entero(h, "id_asignacion"));
            if (a == null) {
                continue;
            }
            Map<String, Object> c = cursos.get(BaseDatos.entero(a, "id_curso"));
            if (c == null || !BaseDatos.igual(BaseDatos.entero(c, "id_institucion"), idInstitucion)) {
                continue;
            }
            Map<String, Object> m = materias.get(BaseDatos.entero(a, "id_materia"));
            Map<String, Object> d = usuarios.get(BaseDatos.entero(a, "id_docente"));
            filas.add(new Object[] {
                    BaseDatos.enteroPrimitivo(h, "id_horario"),
                    BaseDatos.texto(h, "dia"),
                    BaseDatos.texto(h, "hora_inicio"),
                    BaseDatos.texto(h, "hora_fin"),
                    BaseDatos.texto(c, "grado") + "-" + BaseDatos.texto(c, "grupo"),
                    m == null ? null : BaseDatos.texto(m, "nombre"),
                    d == null ? null : BaseDatos.texto(d, "nombre"),
                    BaseDatos.texto(h, "aula"),
                    BaseDatos.enteroPrimitivo(a, "id_asignacion"),
                    BaseDatos.texto(c, "grado"),
                    BaseDatos.texto(c, "grupo") });
        }
        filas.sort((a, b) -> {
            int x = Integer.compare(Ordenes.grado((String) a[9]), Ordenes.grado((String) b[9]));
            if (x != 0) {
                return x;
            }
            x = Ordenes.limpio((String) a[9]).compareTo(Ordenes.limpio((String) b[9]));
            if (x != 0) {
                return x;
            }
            x = Ordenes.limpio((String) a[10]).compareTo(Ordenes.limpio((String) b[10]));
            if (x != 0) {
                return x;
            }
            x = Integer.compare(ordenDia((String) a[1]), ordenDia((String) b[1]));
            return x != 0 ? x : Ordenes.limpio((String) a[2]).compareTo(Ordenes.limpio((String) b[2]));
        });
        List<Object[]> resultado = new ArrayList<>(filas.size());
        for (Object[] f : filas) {
            Object[] nueva = new Object[9];
            System.arraycopy(f, 0, nueva, 0, 9);
            resultado.add(nueva);
        }
        return resultado;
    }

    public String buscarConflicto(int idInstitucion, int idAsignacion, String dia, String inicio, String fin,
                                  String aula, int excluirId) throws DatosException {
        Map<String, Object> nueva = BaseDatos.porId(BaseDatos.ASIGNACION_DOCENTE, "id_asignacion", idAsignacion);
        if (nueva == null) {
            return null;
        }
        if (existe(idInstitucion, dia, inicio, fin, excluirId,
                a -> BaseDatos.igual(BaseDatos.entero(a, "id_docente"), BaseDatos.entero(nueva, "id_docente")), null)) {
            return "El docente ya tiene clase ese día en un horario que se cruza con este.";
        }
        if (existe(idInstitucion, dia, inicio, fin, excluirId,
                a -> BaseDatos.igual(BaseDatos.entero(a, "id_curso"), BaseDatos.entero(nueva, "id_curso")), null)) {
            return "El curso ya tiene otra clase ese día en un horario que se cruza con este.";
        }
        if (aula != null && existe(idInstitucion, dia, inicio, fin, excluirId, a -> true, aula)) {
            return "El aula \"" + aula + "\" ya está ocupada ese día en un horario que se cruza con este.";
        }
        return null;
    }

    private boolean existe(int idInstitucion, String dia, String inicio, String fin, int excluirId,
                           Predicate<Map<String, Object>> coincideAsignacion, String aula) throws DatosException {
        Map<Object, Map<String, Object>> cursos = BaseDatos.indice(BaseDatos.CURSO, "id_curso");
        for (Map<String, Object> h : BaseDatos.tabla(BaseDatos.HORARIO)) {
            if (BaseDatos.igual(BaseDatos.enteroPrimitivo(h, "id_horario"), excluirId)
                    || !BaseDatos.igual(BaseDatos.texto(h, "dia"), dia)
                    || !(Ordenes.limpio(BaseDatos.texto(h, "hora_inicio")).compareTo(Ordenes.limpio(fin)) < 0)
                    || !(Ordenes.limpio(BaseDatos.texto(h, "hora_fin")).compareTo(Ordenes.limpio(inicio)) > 0)) {
                continue;
            }
            Map<String, Object> a = BaseDatos.porId(BaseDatos.ASIGNACION_DOCENTE, "id_asignacion",
                    BaseDatos.entero(h, "id_asignacion"));
            if (a == null) {
                continue;
            }
            Map<String, Object> c = cursos.get(BaseDatos.entero(a, "id_curso"));
            if (c == null || !BaseDatos.igual(BaseDatos.entero(c, "id_institucion"), idInstitucion)) {
                continue;
            }
            if (!coincideAsignacion.test(a)) {
                continue;
            }
            if (aula != null && !BaseDatos.igual(BaseDatos.texto(h, "aula"), aula)) {
                continue;
            }
            return true;
        }
        return false;
    }

    public void insertar(int idAsignacion, String dia, String inicio, String fin, String aula) throws DatosException {
        Map<String, Object> fila = BaseDatos.nuevo();
        fila.put("id_horario", BaseDatos.nuevoId(BaseDatos.HORARIO, "id_horario"));
        fila.put("id_asignacion", idAsignacion);
        fila.put("dia", dia);
        fila.put("hora_inicio", inicio);
        fila.put("hora_fin", fin);
        fila.put("aula", aula);
        BaseDatos.insertar(BaseDatos.HORARIO, fila);
    }

    public void actualizar(int idHorario, int idAsignacion, String dia, String inicio, String fin, String aula)
            throws DatosException {
        Map<String, Object> fila = BaseDatos.porId(BaseDatos.HORARIO, "id_horario", idHorario);
        if (fila != null) {
            fila.put("id_asignacion", idAsignacion);
            fila.put("dia", dia);
            fila.put("hora_inicio", inicio);
            fila.put("hora_fin", fin);
            fila.put("aula", aula);
            BaseDatos.actualizada(BaseDatos.HORARIO);
        }
    }

    public void eliminar(int idHorario) throws DatosException {
        Map<String, Object> fila = BaseDatos.porId(BaseDatos.HORARIO, "id_horario", idHorario);
        if (fila != null) {
            BaseDatos.eliminar(BaseDatos.HORARIO, fila);
        }
    }

    public List<Object[]> listarDeEstudiante(int idEstudiante) throws DatosException {
        Map<String, Object> estudiante = BaseDatos.porId(BaseDatos.ESTUDIANTE, "id_usuario", idEstudiante);
        List<Object[]> filas = new ArrayList<>();
        if (estudiante == null) {
            return filas;
        }
        Integer idCurso = BaseDatos.entero(estudiante, "id_curso");
        Map<Object, Map<String, Object>> asignaciones = BaseDatos.indice(BaseDatos.ASIGNACION_DOCENTE, "id_asignacion");
        Map<Object, Map<String, Object>> materias = BaseDatos.indice(BaseDatos.MATERIA, "id_materia");
        Map<Object, Map<String, Object>> usuarios = BaseDatos.indice(BaseDatos.USUARIO, "id_usuario");
        for (Map<String, Object> h : BaseDatos.tabla(BaseDatos.HORARIO)) {
            Map<String, Object> a = asignaciones.get(BaseDatos.entero(h, "id_asignacion"));
            if (a == null || !"ACTIVO".equals(BaseDatos.texto(a, "estado"))
                    || !BaseDatos.igual(BaseDatos.entero(a, "id_curso"), idCurso)) {
                continue;
            }
            Map<String, Object> m = materias.get(BaseDatos.entero(a, "id_materia"));
            Map<String, Object> d = usuarios.get(BaseDatos.entero(a, "id_docente"));
            filas.add(new Object[] {
                    BaseDatos.texto(h, "dia"),
                    BaseDatos.texto(h, "hora_inicio"),
                    BaseDatos.texto(h, "hora_fin"),
                    m == null ? null : BaseDatos.texto(m, "nombre"),
                    d == null ? null : BaseDatos.texto(d, "nombre"),
                    BaseDatos.texto(h, "aula") });
        }
        filas.sort((a, b) -> Ordenes.limpio((String) a[1]).compareTo(Ordenes.limpio((String) b[1])));
        return filas;
    }
}
