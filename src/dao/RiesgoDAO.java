package dao;

import eipi.util.BaseDatos;
import eipi.util.DatosException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class RiesgoDAO {

    private static final Comparator<Object[]> ORDEN = Comparator
            .comparingInt((Object[] f) -> Ordenes.grado((String) f[6]))
            .thenComparing(f -> Ordenes.limpio((String) f[6]))
            .thenComparing(f -> Ordenes.limpio((String) f[7]))
            .thenComparing(f -> Ordenes.limpio((String) f[8]).toLowerCase());

    public List<Object[]> resumenInstitucion(int idInstitucion, int idPeriodo, LocalDate desde, LocalDate hasta)
            throws DatosException {
        Map<Object, Map<String, Object>> estudiantes = BaseDatos.indice(BaseDatos.ESTUDIANTE, "id_usuario");
        Map<Object, Map<String, Object>> cursos = BaseDatos.indice(BaseDatos.CURSO, "id_curso");

        Map<Integer, double[]> sumas = new LinkedHashMap<>();
        for (Map<String, Object> k : BaseDatos.tabla(BaseDatos.CALIFICACION)) {
            if (!BaseDatos.igual(BaseDatos.entero(k, "id_periodo"), idPeriodo)) {
                continue;
            }
            Double valor = BaseDatos.decimal(k, "valor");
            if (valor == null) {
                continue;
            }
            double[] acumulado = sumas.computeIfAbsent(BaseDatos.enteroPrimitivo(k, "id_estudiante"),
                    x -> new double[2]);
            acumulado[0] += valor;
            acumulado[1]++;
        }

        Map<Integer, int[]> asistencia = new LinkedHashMap<>();
        for (Map<String, Object> s : BaseDatos.tabla(BaseDatos.ASISTENCIA)) {
            LocalDate fecha = BaseDatos.fecha(s, "fecha");
            if (fecha == null || fecha.isBefore(desde) || fecha.isAfter(hasta)) {
                continue;
            }
            int[] acumulado = asistencia.computeIfAbsent(BaseDatos.enteroPrimitivo(s, "id_estudiante"),
                    x -> new int[2]);
            acumulado[0]++;
            if ("AUSENTE".equals(BaseDatos.texto(s, "estado"))) {
                acumulado[1]++;
            }
        }

        List<Object[]> filas = new ArrayList<>();
        for (Map<String, Object> u : BaseDatos.tabla(BaseDatos.USUARIO)) {
            if (!BaseDatos.igual(BaseDatos.entero(u, "id_institucion"), idInstitucion)
                    || !"ACTIVO".equals(BaseDatos.texto(u, "estado"))) {
                continue;
            }
            Integer idUsuario = BaseDatos.entero(u, "id_usuario");
            if (!estudiantes.containsKey(idUsuario)) {
                continue;
            }
            Map<String, Object> c = cursos.get(BaseDatos.entero(estudiantes.get(idUsuario), "id_curso"));
            String grado = c == null ? null : BaseDatos.texto(c, "grado");
            String grupo = c == null ? null : BaseDatos.texto(c, "grupo");
            String curso = c == null ? null : Ordenes.limpio(grado) + "-" + Ordenes.limpio(grupo);
            String nombre = BaseDatos.texto(u, "nombre");

            double[] suma = sumas.get(idUsuario);
            Double promedio = suma == null || suma[1] == 0 ? null : suma[0] / suma[1];
            int[] asis = asistencia.getOrDefault(idUsuario, new int[2]);

            filas.add(new Object[] { idUsuario, nombre, curso, promedio, asis[0], asis[1], grado, grupo, nombre });
        }
        filas.sort(ORDEN);

        List<Object[]> resultado = new ArrayList<>(filas.size());
        for (Object[] f : filas) {
            Object[] nueva = new Object[6];
            System.arraycopy(f, 0, nueva, 0, nueva.length);
            resultado.add(nueva);
        }
        return resultado;
    }
}
