package dao;

import eipi.util.BaseDatos;
import eipi.util.DatosException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AsistenciaDAO {

    private static final String TABLA = BaseDatos.ASISTENCIA;

    public List<Object[]> listar(int idAsignacion, LocalDate fecha) throws DatosException {
        Map<String, Object> asignacion = BaseDatos.porId(BaseDatos.ASIGNACION_DOCENTE, "id_asignacion", idAsignacion);
        List<Object[]> filas = new ArrayList<>();
        if (asignacion == null) {
            return filas;
        }
        String dia = fecha.toString();
        Map<Object, Map<String, Object>> usuarios = BaseDatos.indice(BaseDatos.USUARIO, "id_usuario");
        Map<String, Map<String, Object>> existentes = new LinkedHashMap<>();
        for (Map<String, Object> s : BaseDatos.tabla(TABLA)) {
            if (BaseDatos.igual(BaseDatos.entero(s, "id_asignacion"), idAsignacion)
                    && dia.equals(BaseDatos.texto(s, "fecha"))) {
                existentes.put(clave(BaseDatos.entero(s, "id_estudiante"), idAsignacion, dia), s);
            }
        }
        List<Object[]> estudiantes = new ArrayList<>();
        for (Map<String, Object> e : BaseDatos.tabla(BaseDatos.ESTUDIANTE)) {
            if (!BaseDatos.igual(BaseDatos.entero(e, "id_curso"), BaseDatos.entero(asignacion, "id_curso"))) {
                continue;
            }
            Integer id = BaseDatos.entero(e, "id_usuario");
            Map<String, Object> u = usuarios.get(id);
            if (u == null || !"ACTIVO".equals(BaseDatos.texto(u, "estado"))) {
                continue;
            }
            Map<String, Object> s = existentes.get(clave(id, idAsignacion, dia));
            estudiantes.add(new Object[]{id, BaseDatos.texto(u, "nombre"),
                s == null ? null : BaseDatos.texto(s, "estado"),
                s == null ? null : BaseDatos.texto(s, "observacion")});
        }
        estudiantes.sort(Comparator.comparing(f -> Ordenes.limpio((String) f[1])));
        return estudiantes;
    }

    public void guardar(int idAsignacion, LocalDate fecha, List<Object[]> filas) throws DatosException {
        BaseDatos.enTransaccion(() -> {
            String dia = fecha.toString();
            Map<String, Map<String, Object>> existentes = new LinkedHashMap<>();
            for (Map<String, Object> s : BaseDatos.tabla(TABLA)) {
                if (BaseDatos.igual(BaseDatos.entero(s, "id_asignacion"), idAsignacion)
                        && dia.equals(BaseDatos.texto(s, "fecha"))) {
                    existentes.put(clave(BaseDatos.entero(s, "id_estudiante"), idAsignacion, dia), s);
                }
            }
            for (Object[] f : filas) {
                Integer idEstudiante = (Integer) f[0];
                Map<String, Object> s = existentes.get(clave(idEstudiante, idAsignacion, dia));
                if (s == null) {
                    s = BaseDatos.nuevo();
                    s.put("id_asistencia", BaseDatos.nuevoId(TABLA, "id_asistencia"));
                    s.put("id_estudiante", idEstudiante);
                    s.put("id_asignacion", idAsignacion);
                    s.put("fecha", dia);
                    s.put("estado", f[1]);
                    s.put("observacion", f[2]);
                    BaseDatos.insertar(TABLA, s);
                } else {
                    s.put("estado", f[1]);
                    s.put("observacion", f[2]);
                    BaseDatos.marcarSucia(TABLA);
                }
            }
            return null;
        });
    }

    public Map<Integer, int[]> resumenInasistencia(int idAsignacion, LocalDate desde, LocalDate hasta)
            throws DatosException {
        Map<Integer, int[]> mapa = new LinkedHashMap<>();
        for (Map<String, Object> s : BaseDatos.tabla(TABLA)) {
            if (!BaseDatos.igual(BaseDatos.entero(s, "id_asignacion"), idAsignacion)) {
                continue;
            }
            LocalDate f = BaseDatos.fecha(s, "fecha");
            if (f == null || f.isBefore(desde) || f.isAfter(hasta)) {
                continue;
            }
            Integer id = BaseDatos.entero(s, "id_estudiante");
            int[] totales = mapa.computeIfAbsent(id, k -> new int[2]);
            totales[0]++;
            if ("AUSENTE".equals(BaseDatos.texto(s, "estado"))) {
                totales[1]++;
            }
        }
        return mapa;
    }

    private static String clave(Integer idEstudiante, int idAsignacion, String fecha) {
        return idEstudiante + "|" + idAsignacion + "|" + fecha;
    }
}
