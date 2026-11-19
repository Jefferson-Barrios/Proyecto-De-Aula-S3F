package dao;

import eipi.util.BaseDatos;
import eipi.util.DatosException;
import eipi.util.Formato;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import modelo.AlertaDesercion;

public class AlertaDAO {

    private static final String TABLA = BaseDatos.ALERTA_DESERCION;

    private static int rangoEstado(String estado) {
        return switch (estado == null ? "" : estado) {
            case "PENDIENTE" -> 0;
            case "EN SEGUIMIENTO" -> 1;
            default -> 2;
        };
    }

    private static int rangoNivel(String nivel) {
        return switch (nivel == null ? "" : nivel) {
            case "ALTO" -> 0;
            case "MEDIO" -> 1;
            case "PREVENTIVA" -> 2;
            default -> 3;
        };
    }

    private static final Comparator<Object[]> ORDEN = Comparator
            .comparingInt((Object[] f) -> rangoEstado((String) f[9]))
            .thenComparingInt(f -> rangoNivel((String) f[3]))
            .thenComparing(Comparator.comparing((Object[] f) -> (String) f[8]).reversed())
            .thenComparing(Comparator.comparingInt((Object[] f) -> (Integer) f[0]).reversed());

    private static String cursoDe(Map<Object, Map<String, Object>> estudiantes,
            Map<Object, Map<String, Object>> cursos, Integer idEstudiante) {
        Map<String, Object> e = estudiantes.get(idEstudiante);
        if (e == null) {
            return null;
        }
        Map<String, Object> c = cursos.get(BaseDatos.entero(e, "id_curso"));
        return c == null ? null : BaseDatos.texto(c, "grado") + "-" + BaseDatos.texto(c, "grupo");
    }

    public List<Object[]> listarDeInstitucion(int idInstitucion, String estado) throws DatosException {
        Map<Object, Map<String, Object>> usuarios = BaseDatos.indice(BaseDatos.USUARIO, "id_usuario");
        Map<Object, Map<String, Object>> estudiantes = BaseDatos.indice(BaseDatos.ESTUDIANTE, "id_usuario");
        Map<Object, Map<String, Object>> cursos = BaseDatos.indice(BaseDatos.CURSO, "id_curso");
        Map<Object, Map<String, Object>> periodos = BaseDatos.indice(BaseDatos.PERIODO_ACADEMICO, "id_periodo");
        List<Object[]> filas = new ArrayList<>();
        for (Map<String, Object> al : BaseDatos.tabla(TABLA)) {
            Integer idEstudiante = BaseDatos.entero(al, "id_estudiante");
            Map<String, Object> u = usuarios.get(idEstudiante);
            if (u == null || !BaseDatos.igual(BaseDatos.entero(u, "id_institucion"), idInstitucion)) {
                continue;
            }
            String est = BaseDatos.texto(al, "estado");
            if (estado != null && !estado.equals(est)) {
                continue;
            }
            Map<String, Object> p = periodos.get(BaseDatos.entero(al, "id_periodo"));
            filas.add(new Object[] {
                    BaseDatos.enteroPrimitivo(al, "id_alerta"),
                    BaseDatos.texto(u, "nombre"),
                    cursoDe(estudiantes, cursos, idEstudiante),
                    BaseDatos.texto(al, "nivel_riesgo"),
                    BaseDatos.decimal(al, "porcentaje_asistencia"),
                    BaseDatos.enteroPrimitivo(al, "materias_perdidas"),
                    BaseDatos.texto(al, "motivo"),
                    p == null ? null : BaseDatos.texto(p, "nombre"),
                    Formato.texto(BaseDatos.fecha(al, "fecha")),
                    est,
                    idEstudiante });
        }
        filas.sort(ORDEN);
        return filas;
    }

    public List<Object[]> listarDeDocente(int idDocente) throws DatosException {
        Map<Object, Map<String, Object>> usuarios = BaseDatos.indice(BaseDatos.USUARIO, "id_usuario");
        Map<Object, Map<String, Object>> estudiantes = BaseDatos.indice(BaseDatos.ESTUDIANTE, "id_usuario");
        Map<Object, Map<String, Object>> cursos = BaseDatos.indice(BaseDatos.CURSO, "id_curso");
        List<Object[]> filas = new ArrayList<>();
        for (Map<String, Object> al : BaseDatos.tabla(TABLA)) {
            if (!BaseDatos.igual(BaseDatos.entero(al, "id_docente"), idDocente)) {
                continue;
            }
            Integer idEstudiante = BaseDatos.entero(al, "id_estudiante");
            Map<String, Object> u = usuarios.get(idEstudiante);
            filas.add(new Object[] {
                    u == null ? null : BaseDatos.texto(u, "nombre"),
                    cursoDe(estudiantes, cursos, idEstudiante),
                    BaseDatos.texto(al, "nivel_riesgo"),
                    BaseDatos.texto(al, "motivo"),
                    Formato.texto(BaseDatos.fecha(al, "fecha")),
                    BaseDatos.texto(al, "estado") });
        }
        filas.sort(Comparator.comparing((Object[] f) -> (String) f[4]).reversed());
        return filas;
    }

    public void crear(AlertaDesercion a) throws DatosException {
        Map<String, Object> fila = BaseDatos.nuevo();
        fila.put("id_alerta", BaseDatos.nuevoId(TABLA, "id_alerta"));
        fila.put("id_estudiante", a.getIdEstudiante());
        fila.put("id_docente", a.getIdDocente());
        fila.put("id_psicosocial", a.getIdPsicosocial());
        fila.put("id_periodo", null);
        fila.put("fecha", LocalDate.now().toString());
        fila.put("nivel_riesgo", a.getNivelRiesgo());
        fila.put("porcentaje_asistencia", null);
        fila.put("materias_perdidas", 0);
        fila.put("motivo", a.getMotivo());
        fila.put("estado", "PENDIENTE");
        BaseDatos.insertar(TABLA, fila);
    }

    public void tomar(int idAlerta, int idPsicosocial) throws DatosException {
        Map<String, Object> al = BaseDatos.porId(TABLA, "id_alerta", idAlerta);
        if (al != null && "PENDIENTE".equals(BaseDatos.texto(al, "estado"))) {
            al.put("id_psicosocial", idPsicosocial);
            al.put("estado", "EN SEGUIMIENTO");
            BaseDatos.actualizada(TABLA);
        }
    }

    public void cerrar(int idAlerta, int idPsicosocial) throws DatosException {
        Map<String, Object> al = BaseDatos.porId(TABLA, "id_alerta", idAlerta);
        if (al != null) {
            if (BaseDatos.entero(al, "id_psicosocial") == null) {
                al.put("id_psicosocial", idPsicosocial);
            }
            al.put("estado", "CERRADA");
            BaseDatos.actualizada(TABLA);
        }
    }
}
