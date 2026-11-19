package dao;

import eipi.util.BaseDatos;
import eipi.util.DatosException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CalificacionDAO {

    private static final String TABLA = BaseDatos.CALIFICACION;

    public List<Object[]> listarResumen(int idAsignacion, int idPeriodo) throws DatosException {
        Map<String, Object> asignacion = BaseDatos.porId(BaseDatos.ASIGNACION_DOCENTE, "id_asignacion", idAsignacion);
        if (asignacion == null) {
            return new ArrayList<>();
        }
        Map<Object, Map<String, Object>> usuarios = BaseDatos.indice(BaseDatos.USUARIO, "id_usuario");
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
            estudiantes.add(new Object[]{id, BaseDatos.texto(u, "nombre")});
        }
        estudiantes.sort(Comparator.comparing(f -> Ordenes.limpio((String) f[1])));

        List<Map<String, Object>> notas = new ArrayList<>();
        for (Map<String, Object> c : BaseDatos.tabla(TABLA)) {
            if (BaseDatos.igual(BaseDatos.entero(c, "id_asignacion"), idAsignacion)
                    && BaseDatos.igual(BaseDatos.entero(c, "id_periodo"), idPeriodo)) {
                notas.add(c);
            }
        }
        notas.sort(Comparator
                .comparing((Map<String, Object> c) -> Ordenes.limpio(BaseDatos.texto(c, "fecha")))
                .thenComparingInt(c -> BaseDatos.enteroPrimitivo(c, "id_calificacion")));

        Map<Integer, List<Double>> porEstudiante = new LinkedHashMap<>();
        for (Map<String, Object> c : notas) {
            Double valor = BaseDatos.decimal(c, "valor");
            if (valor != null) {
                porEstudiante.computeIfAbsent(BaseDatos.entero(c, "id_estudiante"), k -> new ArrayList<>()).add(valor);
            }
        }

        List<Object[]> filas = new ArrayList<>();
        for (Object[] e : estudiantes) {
            filas.add(new Object[]{e[0], e[1],
                new ArrayList<>(porEstudiante.getOrDefault((Integer) e[0], new ArrayList<>()))});
        }
        return filas;
    }

    public void insertar(int idEstudiante, int idAsignacion, int idPeriodo, double valor,
                         String tipo, LocalDate fecha) throws DatosException {
        Map<String, Object> fila = BaseDatos.nuevo();
        fila.put("id_calificacion", BaseDatos.nuevoId(TABLA, "id_calificacion"));
        fila.put("id_estudiante", idEstudiante);
        fila.put("id_asignacion", idAsignacion);
        fila.put("id_periodo", idPeriodo);
        fila.put("valor", valor);
        fila.put("tipo", tipo);
        fila.put("fecha", fecha.toString());
        BaseDatos.insertar(TABLA, fila);
    }

    public void guardarDefinitivas(int idAsignacion, int idPeriodo, Map<Integer, Double> notas)
            throws DatosException {
        BaseDatos.enTransaccion(() -> {
            Map<String, Map<String, Object>> existentes = new LinkedHashMap<>();
            for (Map<String, Object> n : BaseDatos.tabla(BaseDatos.NOTA_PERIODO)) {
                existentes.put(clave(BaseDatos.entero(n, "id_estudiante"), idAsignacion, idPeriodo), n);
            }
            for (Map.Entry<Integer, Double> e : notas.entrySet()) {
                Map<String, Object> n = existentes.get(clave(e.getKey(), idAsignacion, idPeriodo));
                if (n == null) {
                    n = BaseDatos.nuevo();
                    n.put("id_nota_periodo", BaseDatos.nuevoId(BaseDatos.NOTA_PERIODO, "id_nota_periodo"));
                    n.put("id_estudiante", e.getKey());
                    n.put("id_asignacion", idAsignacion);
                    n.put("id_periodo", idPeriodo);
                    n.put("nota_definitiva", e.getValue());
                    BaseDatos.insertar(BaseDatos.NOTA_PERIODO, n);
                } else {
                    n.put("nota_definitiva", e.getValue());
                    BaseDatos.marcarSucia(BaseDatos.NOTA_PERIODO);
                }
            }
            return null;
        });
    }

    public List<Object[]> listarDeEstudiante(int idEstudiante, int idPeriodo) throws DatosException {
        Map<String, Object> estudiante = BaseDatos.porId(BaseDatos.ESTUDIANTE, "id_usuario", idEstudiante);
        List<Object[]> filas = new ArrayList<>();
        if (estudiante == null) {
            return filas;
        }
        Map<Object, Map<String, Object>> materias = BaseDatos.indice(BaseDatos.MATERIA, "id_materia");
        List<Map<String, Object>> asignaciones = new ArrayList<>();
        for (Map<String, Object> a : BaseDatos.tabla(BaseDatos.ASIGNACION_DOCENTE)) {
            if (BaseDatos.igual(BaseDatos.entero(a, "id_curso"), BaseDatos.entero(estudiante, "id_curso"))
                    && "ACTIVO".equals(BaseDatos.texto(a, "estado"))) {
                asignaciones.add(a);
            }
        }
        asignaciones.sort(Comparator
                .comparing((Map<String, Object> a) -> {
                    Map<String, Object> m = materias.get(BaseDatos.entero(a, "id_materia"));
                    return Ordenes.limpio(m == null ? null : BaseDatos.texto(m, "nombre"));
                })
                .thenComparingInt(a -> BaseDatos.enteroPrimitivo(a, "id_asignacion")));

        for (Map<String, Object> a : asignaciones) {
            int idAsignacion = BaseDatos.enteroPrimitivo(a, "id_asignacion");
            List<Map<String, Object>> notas = new ArrayList<>();
            for (Map<String, Object> c : BaseDatos.tabla(TABLA)) {
                if (BaseDatos.igual(BaseDatos.entero(c, "id_asignacion"), idAsignacion)
                        && BaseDatos.igual(BaseDatos.entero(c, "id_estudiante"), idEstudiante)
                        && BaseDatos.igual(BaseDatos.entero(c, "id_periodo"), idPeriodo)) {
                    notas.add(c);
                }
            }
            notas.sort(Comparator
                    .comparing((Map<String, Object> c) -> Ordenes.limpio(BaseDatos.texto(c, "fecha")))
                    .thenComparingInt(c -> BaseDatos.enteroPrimitivo(c, "id_calificacion")));
            List<Double> valores = new ArrayList<>();
            for (Map<String, Object> c : notas) {
                Double valor = BaseDatos.decimal(c, "valor");
                if (valor != null) {
                    valores.add(valor);
                }
            }
            Double definitiva = null;
            for (Map<String, Object> n : BaseDatos.tabla(BaseDatos.NOTA_PERIODO)) {
                if (BaseDatos.igual(BaseDatos.entero(n, "id_asignacion"), idAsignacion)
                        && BaseDatos.igual(BaseDatos.entero(n, "id_estudiante"), idEstudiante)
                        && BaseDatos.igual(BaseDatos.entero(n, "id_periodo"), idPeriodo)) {
                    definitiva = BaseDatos.decimal(n, "nota_definitiva");
                    break;
                }
            }
            Map<String, Object> m = materias.get(BaseDatos.entero(a, "id_materia"));
            filas.add(new Object[]{m == null ? null : BaseDatos.texto(m, "nombre"), valores, definitiva});
        }
        return filas;
    }

    public Double promedioGeneral(int idEstudiante, int idPeriodo) throws DatosException {
        double suma = 0;
        int total = 0;
        for (Map<String, Object> c : BaseDatos.tabla(TABLA)) {
            if (BaseDatos.igual(BaseDatos.entero(c, "id_estudiante"), idEstudiante)
                    && BaseDatos.igual(BaseDatos.entero(c, "id_periodo"), idPeriodo)) {
                Double valor = BaseDatos.decimal(c, "valor");
                if (valor != null) {
                    suma += valor;
                    total++;
                }
            }
        }
        return total == 0 ? null : suma / total;
    }

    private static String clave(Integer idEstudiante, int idAsignacion, int idPeriodo) {
        return idEstudiante + "|" + idAsignacion + "|" + idPeriodo;
    }
}
