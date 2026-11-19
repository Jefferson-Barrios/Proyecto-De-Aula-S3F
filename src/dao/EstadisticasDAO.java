package dao;

import eipi.util.BaseDatos;
import eipi.util.DatosException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class EstadisticasDAO {

    private static final Locale ES = Locale.forLanguageTag("es-CO");

    private static Map<String, Integer> conClaves(String... claves) {
        Map<String, Integer> m = new LinkedHashMap<>();
        for (String c : claves) {
            m.put(c, 0);
        }
        return m;
    }

    private static Map<Object, Map<String, Object>> usuarios() throws DatosException {
        return BaseDatos.indice(BaseDatos.USUARIO, "id_usuario");
    }

    private static Map<Integer, Integer> contarPorInstitucion(Map<Integer, Integer> conteo,
            Map<Object, Map<String, Object>> instituciones) {
        List<Map.Entry<Integer, Integer>> orden = new ArrayList<>(conteo.entrySet());
        orden.sort((a, b) -> {
            int c = Integer.compare(b.getValue(), a.getValue());
            if (c != 0) {
                return c;
            }
            Map<String, Object> ia = instituciones.get(a.getKey());
            Map<String, Object> ib = instituciones.get(b.getKey());
            return Ordenes.limpio(ia == null ? null : BaseDatos.texto(ia, "nombre"))
                    .compareToIgnoreCase(Ordenes.limpio(ib == null ? null : BaseDatos.texto(ib, "nombre")));
        });
        Map<Integer, Integer> resultado = new LinkedHashMap<>();
        for (int i = 0; i < orden.size() && i < 10; i++) {
            resultado.put(orden.get(i).getKey(), orden.get(i).getValue());
        }
        return resultado;
    }

    private static boolean delInstitucion(Map<Object, Map<String, Object>> usuarios, Integer idEstudiante,
            Integer idInstitucion) {
        Map<String, Object> u = usuarios.get(idEstudiante);
        if (u == null) {
            return false;
        }
        return idInstitucion == null
                || BaseDatos.igual(BaseDatos.entero(u, "id_institucion"), idInstitucion);
    }

    private static String cursoDe(Map<Object, Map<String, Object>> estudiantes,
            Map<Object, Map<String, Object>> cursos, Integer idEstudiante) {
        Map<String, Object> e = estudiantes.get(idEstudiante);
        if (e == null) {
            return "Sin curso";
        }
        Map<String, Object> c = cursos.get(BaseDatos.entero(e, "id_curso"));
        if (c == null) {
            return "Sin curso";
        }
        return Ordenes.limpio(BaseDatos.texto(c, "grado")) + "-" + Ordenes.limpio(BaseDatos.texto(c, "grupo"));
    }

    public Map<String, Integer> alertasPorNivel(Integer idInstitucion) throws DatosException {
        Map<String, Integer> r = conClaves("ALTO", "MEDIO", "PREVENTIVA");
        Map<Object, Map<String, Object>> usuarios = usuarios();
        for (Map<String, Object> al : BaseDatos.tabla(BaseDatos.ALERTA_DESERCION)) {
            if (!delInstitucion(usuarios, BaseDatos.entero(al, "id_estudiante"), idInstitucion)) {
                continue;
            }
            String clave = BaseDatos.texto(al, "nivel_riesgo");
            r.merge(clave == null ? "Sin curso" : clave, 1, Integer::sum);
        }
        return r;
    }

    public Map<String, Integer> alertasPorEstado(Integer idInstitucion) throws DatosException {
        Map<String, Integer> r = conClaves("PENDIENTE", "EN SEGUIMIENTO", "CERRADA");
        Map<Object, Map<String, Object>> usuarios = usuarios();
        for (Map<String, Object> al : BaseDatos.tabla(BaseDatos.ALERTA_DESERCION)) {
            if (!delInstitucion(usuarios, BaseDatos.entero(al, "id_estudiante"), idInstitucion)) {
                continue;
            }
            String clave = BaseDatos.texto(al, "estado");
            r.merge(clave == null ? "Sin curso" : clave, 1, Integer::sum);
        }
        return r;
    }

    public Map<String, Integer> citasPorEstado(Integer idInstitucion) throws DatosException {
        Map<String, Integer> r = conClaves("AGENDADA", "REALIZADA", "CANCELADA");
        Map<Object, Map<String, Object>> usuarios = usuarios();
        for (Map<String, Object> ct : BaseDatos.tabla(BaseDatos.CITA_PSICOSOCIAL)) {
            if (!delInstitucion(usuarios, BaseDatos.entero(ct, "id_estudiante"), idInstitucion)) {
                continue;
            }
            String clave = BaseDatos.texto(ct, "estado");
            r.merge(clave == null ? "Sin curso" : clave, 1, Integer::sum);
        }
        return r;
    }

    public Map<String, Integer> alertasPorCurso(Integer idInstitucion) throws DatosException {
        Map<Object, Map<String, Object>> usuarios = usuarios();
        Map<Object, Map<String, Object>> estudiantes = BaseDatos.indice(BaseDatos.ESTUDIANTE, "id_usuario");
        Map<Object, Map<String, Object>> cursos = BaseDatos.indice(BaseDatos.CURSO, "id_curso");
        Map<String, Integer> conteo = new LinkedHashMap<>();
        for (Map<String, Object> al : BaseDatos.tabla(BaseDatos.ALERTA_DESERCION)) {
            Integer idEstudiante = BaseDatos.entero(al, "id_estudiante");
            if (!delInstitucion(usuarios, idEstudiante, idInstitucion)) {
                continue;
            }
            conteo.merge(cursoDe(estudiantes, cursos, idEstudiante), 1, Integer::sum);
        }
        List<Map.Entry<String, Integer>> orden = new ArrayList<>(conteo.entrySet());
        orden.sort((a, b) -> Integer.compare(b.getValue(), a.getValue()));
        Map<String, Integer> r = new LinkedHashMap<>();
        for (int i = 0; i < orden.size() && i < 8; i++) {
            r.put(orden.get(i).getKey(), orden.get(i).getValue());
        }
        return r;
    }

    public Map<String, Integer> usuariosPorRol(Integer idInstitucion) throws DatosException {
        Map<String, Integer> r = conClaves("ESTUDIANTE", "DOCENTE", "DIRECTIVA", "PSICOSOCIAL");
        for (Map<String, Object> u : BaseDatos.tabla(BaseDatos.USUARIO)) {
            if (!"ACTIVO".equals(BaseDatos.texto(u, "estado"))) {
                continue;
            }
            String rol = BaseDatos.texto(u, "rol");
            if (rol == null || "ADMINISTRADOR".equals(rol)) {
                continue;
            }
            if (idInstitucion != null
                    && !BaseDatos.igual(BaseDatos.entero(u, "id_institucion"), idInstitucion)) {
                continue;
            }
            r.merge(rol, 1, Integer::sum);
        }
        return r;
    }

    public int contarCursos(Integer idInstitucion) throws DatosException {
        int total = 0;
        for (Map<String, Object> c : BaseDatos.tabla(BaseDatos.CURSO)) {
            if (idInstitucion == null
                    || BaseDatos.igual(BaseDatos.entero(c, "id_institucion"), idInstitucion)) {
                total++;
            }
        }
        return total;
    }

    public int estudiantesConAlertaAbierta(Integer idInstitucion) throws DatosException {
        Map<Object, Map<String, Object>> usuarios = usuarios();
        Set<Integer> ids = new LinkedHashSet<>();
        for (Map<String, Object> al : BaseDatos.tabla(BaseDatos.ALERTA_DESERCION)) {
            String estado = BaseDatos.texto(al, "estado");
            if (!"PENDIENTE".equals(estado) && !"EN SEGUIMIENTO".equals(estado)) {
                continue;
            }
            Integer idEstudiante = BaseDatos.entero(al, "id_estudiante");
            if (!delInstitucion(usuarios, idEstudiante, idInstitucion)) {
                continue;
            }
            ids.add(idEstudiante);
        }
        return ids.size();
    }

    public Map<String, Integer> alertasPorMes(Integer idInstitucion, int meses) throws DatosException {
        YearMonth hasta = YearMonth.now();
        LocalDate desde = hasta.minusMonths(meses - 1L).atDay(1);
        Map<Object, Map<String, Object>> usuarios = usuarios();
        Map<String, Integer> encontrados = new LinkedHashMap<>();
        for (Map<String, Object> al : BaseDatos.tabla(BaseDatos.ALERTA_DESERCION)) {
            LocalDate fecha = BaseDatos.fecha(al, "fecha");
            if (fecha == null || fecha.isBefore(desde)) {
                continue;
            }
            if (!delInstitucion(usuarios, BaseDatos.entero(al, "id_estudiante"), idInstitucion)) {
                continue;
            }
            String mes = fecha.getYear() + "-" + String.format("%02d", fecha.getMonthValue());
            encontrados.merge(mes, 1, Integer::sum);
        }
        return completarMeses(encontrados, hasta, meses);
    }

    static Map<String, Integer> completarMeses(Map<String, Integer> porMes, YearMonth hasta, int meses) {
        Map<String, Integer> r = new LinkedHashMap<>();
        for (int i = meses - 1; i >= 0; i--) {
            YearMonth ym = hasta.minusMonths(i);
            String mes = ym.getMonth().getDisplayName(TextStyle.SHORT, ES).replace(".", "");
            String etiqueta = Character.toUpperCase(mes.charAt(0)) + mes.substring(1) + " " + ym.getYear();
            r.put(etiqueta, porMes.getOrDefault(ym.toString(), 0));
        }
        return r;
    }

    public Map<String, Integer> estudiantesPorInstitucion() throws DatosException {
        Map<Object, Map<String, Object>> instituciones = BaseDatos.indice(BaseDatos.INSTITUCION, "id_institucion");
        Map<Integer, Integer> conteo = new LinkedHashMap<>();
        for (Map<String, Object> u : BaseDatos.tabla(BaseDatos.USUARIO)) {
            if (!"ESTUDIANTE".equals(BaseDatos.texto(u, "rol"))
                    || !"ACTIVO".equals(BaseDatos.texto(u, "estado"))) {
                continue;
            }
            Integer idInstitucion = BaseDatos.entero(u, "id_institucion");
            if (!instituciones.containsKey(idInstitucion)) {
                continue;
            }
            conteo.merge(idInstitucion, 1, Integer::sum);
        }
        Map<String, Integer> r = new LinkedHashMap<>();
        for (Map.Entry<Integer, Integer> e : contarPorInstitucion(conteo, instituciones).entrySet()) {
            r.put(BaseDatos.texto(instituciones.get(e.getKey()), "nombre"), e.getValue());
        }
        return r;
    }

    public Map<String, Integer> alertasPorInstitucion() throws DatosException {
        Map<Object, Map<String, Object>> usuarios = usuarios();
        Map<Object, Map<String, Object>> instituciones = BaseDatos.indice(BaseDatos.INSTITUCION, "id_institucion");
        Map<Integer, Integer> conteo = new LinkedHashMap<>();
        for (Map<String, Object> al : BaseDatos.tabla(BaseDatos.ALERTA_DESERCION)) {
            Map<String, Object> u = usuarios.get(BaseDatos.entero(al, "id_estudiante"));
            if (u == null) {
                continue;
            }
            Integer idInstitucion = BaseDatos.entero(u, "id_institucion");
            if (!instituciones.containsKey(idInstitucion)) {
                continue;
            }
            conteo.merge(idInstitucion, 1, Integer::sum);
        }
        Map<String, Integer> r = new LinkedHashMap<>();
        for (Map.Entry<Integer, Integer> e : contarPorInstitucion(conteo, instituciones).entrySet()) {
            r.put(BaseDatos.texto(instituciones.get(e.getKey()), "nombre"), e.getValue());
        }
        return r;
    }

    public List<Object[]> comparativo() throws DatosException {
        Map<Object, Map<String, Object>> usuarios = usuarios();
        List<Map<String, Object>> orden = new ArrayList<>(BaseDatos.tabla(BaseDatos.INSTITUCION));
        orden.sort((a, b) -> Ordenes.limpio(BaseDatos.texto(a, "nombre"))
                .compareToIgnoreCase(Ordenes.limpio(BaseDatos.texto(b, "nombre"))));

        List<Object[]> filas = new ArrayList<>();
        for (Map<String, Object> i : orden) {
            Integer idInstitucion = BaseDatos.entero(i, "id_institucion");
            int estudiantes = 0;
            int docentes = 0;
            for (Map<String, Object> u : BaseDatos.tabla(BaseDatos.USUARIO)) {
                if (!"ACTIVO".equals(BaseDatos.texto(u, "estado"))
                        || !BaseDatos.igual(BaseDatos.entero(u, "id_institucion"), idInstitucion)) {
                    continue;
                }
                String rol = BaseDatos.texto(u, "rol");
                if ("ESTUDIANTE".equals(rol)) {
                    estudiantes++;
                } else if ("DOCENTE".equals(rol)) {
                    docentes++;
                }
            }
            int alertas = 0;
            int pendientes = 0;
            int altas = 0;
            Set<Integer> abiertas = new LinkedHashSet<>();
            for (Map<String, Object> al : BaseDatos.tabla(BaseDatos.ALERTA_DESERCION)) {
                Map<String, Object> u = usuarios.get(BaseDatos.entero(al, "id_estudiante"));
                if (u == null || !BaseDatos.igual(BaseDatos.entero(u, "id_institucion"), idInstitucion)) {
                    continue;
                }
                alertas++;
                String estado = BaseDatos.texto(al, "estado");
                if ("PENDIENTE".equals(estado)) {
                    pendientes++;
                }
                if ("ALTO".equals(BaseDatos.texto(al, "nivel_riesgo"))) {
                    altas++;
                }
                if ("PENDIENTE".equals(estado) || "EN SEGUIMIENTO".equals(estado)) {
                    abiertas.add(BaseDatos.entero(al, "id_estudiante"));
                }
            }
            filas.add(new Object[]{idInstitucion, BaseDatos.texto(i, "nombre"), BaseDatos.texto(i, "estado"),
                estudiantes, docentes, alertas, pendientes, altas, abiertas.size()});
        }
        return filas;
    }
}
