package dao;

import controlador.AnalisisRiesgo;
import controlador.CierrePeriodo;
import controlador.CierrePeriodo.AlertaExistente;
import controlador.CierrePeriodo.Almacen;
import controlador.CierrePeriodo.Estudiante;
import controlador.CierrePeriodo.Evaluacion;
import controlador.CierrePeriodo.Promedio;
import controlador.CierrePeriodo.Resultado;
import eipi.util.BaseDatos;
import eipi.util.DatosException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import modelo.PeriodoAcademico;

public class CierrePeriodoDAO {

    private static final Comparator<Object[]> ORDEN_ESTUDIANTES = Comparator
            .comparingInt((Object[] f) -> Ordenes.grado((String) f[3]))
            .thenComparing(f -> Ordenes.limpio((String) f[3]))
            .thenComparing(f -> Ordenes.limpio((String) f[4]))
            .thenComparing(f -> Ordenes.limpio((String) f[5]).toLowerCase());

    public Resultado cerrar(final int idInstitucion, final PeriodoAcademico periodo) throws DatosException {
        return BaseDatos.enTransaccion(() -> CierrePeriodo.cerrar(new AlmacenMemoria(idInstitucion), periodo));
    }

    public List<Evaluacion> evaluarProvisional(final int idInstitucion, final PeriodoAcademico periodo)
            throws DatosException {
        return CierrePeriodo.evaluar(new AlmacenMemoria(idInstitucion), periodo);
    }

    private static final class AlmacenMemoria implements Almacen {

        private final int idInstitucion;

        AlmacenMemoria(int idInstitucion) {
            this.idInstitucion = idInstitucion;
        }

        @Override
        public List<Estudiante> estudiantes() throws DatosException {
            Map<Object, Map<String, Object>> estudiantes = BaseDatos.indice(BaseDatos.ESTUDIANTE, "id_usuario");
            Map<Object, Map<String, Object>> cursos = BaseDatos.indice(BaseDatos.CURSO, "id_curso");
            List<Object[]> filas = new ArrayList<>();
            for (Map<String, Object> u : BaseDatos.tabla(BaseDatos.USUARIO)) {
                if (!BaseDatos.igual(BaseDatos.entero(u, "id_institucion"), idInstitucion)
                        || !"ACTIVO".equals(BaseDatos.texto(u, "estado"))) {
                    continue;
                }
                Integer idUsuario = BaseDatos.entero(u, "id_usuario");
                Map<String, Object> e = estudiantes.get(idUsuario);
                if (e == null) {
                    continue;
                }
                Map<String, Object> c = cursos.get(BaseDatos.entero(e, "id_curso"));
                String grado = c == null ? null : BaseDatos.texto(c, "grado");
                String grupo = c == null ? null : BaseDatos.texto(c, "grupo");
                String curso = c == null ? null : Ordenes.limpio(grado) + "-" + Ordenes.limpio(grupo);
                String nombre = BaseDatos.texto(u, "nombre");
                filas.add(new Object[]{idUsuario, nombre, curso, grado, grupo, nombre});
            }
            filas.sort(ORDEN_ESTUDIANTES);
            List<Estudiante> lista = new ArrayList<>(filas.size());
            for (Object[] f : filas) {
                lista.add(new Estudiante((Integer) f[0], (String) f[1], (String) f[2]));
            }
            return lista;
        }

        @Override
        public List<Promedio> promedios(int idPeriodo) throws DatosException {
            Map<Object, Map<String, Object>> asignaciones = BaseDatos.indice(BaseDatos.ASIGNACION_DOCENTE, "id_asignacion");
            Map<Object, Map<String, Object>> materias = BaseDatos.indice(BaseDatos.MATERIA, "id_materia");
            Map<Object, Map<String, Object>> usuarios = BaseDatos.indice(BaseDatos.USUARIO, "id_usuario");

            Map<String, double[]> acumulado = new LinkedHashMap<>();
            Map<String, int[]> claves = new LinkedHashMap<>();
            Map<String, String> nombres = new LinkedHashMap<>();
            for (Map<String, Object> k : BaseDatos.tabla(BaseDatos.CALIFICACION)) {
                if (!BaseDatos.igual(BaseDatos.entero(k, "id_periodo"), idPeriodo)) {
                    continue;
                }
                Double valor = BaseDatos.decimal(k, "valor");
                if (valor == null) {
                    continue;
                }
                Map<String, Object> a = asignaciones.get(BaseDatos.entero(k, "id_asignacion"));
                if (a == null) {
                    continue;
                }
                Map<String, Object> m = materias.get(BaseDatos.entero(a, "id_materia"));
                if (m == null) {
                    continue;
                }
                Map<String, Object> u = usuarios.get(BaseDatos.entero(k, "id_estudiante"));
                if (u == null || !BaseDatos.igual(BaseDatos.entero(u, "id_institucion"), idInstitucion)
                        || !"ACTIVO".equals(BaseDatos.texto(u, "estado"))) {
                    continue;
                }
                int idEstudiante = BaseDatos.enteroPrimitivo(k, "id_estudiante");
                int idAsignacion = BaseDatos.enteroPrimitivo(k, "id_asignacion");
                int idMateria = BaseDatos.enteroPrimitivo(a, "id_materia");
                String clave = idEstudiante + "|" + idAsignacion + "|" + idMateria;
                double[] acc = acumulado.computeIfAbsent(clave, x -> new double[2]);
                acc[0] += valor;
                acc[1]++;
                claves.putIfAbsent(clave, new int[]{idEstudiante, idAsignacion, idMateria});
                nombres.putIfAbsent(clave, BaseDatos.texto(m, "nombre"));
            }

            List<Promedio> lista = new ArrayList<>(acumulado.size());
            for (Map.Entry<String, double[]> e : acumulado.entrySet()) {
                double[] acc = e.getValue();
                int[] ids = claves.get(e.getKey());
                lista.add(new Promedio(ids[0], ids[1], ids[2], nombres.get(e.getKey()), acc[0] / acc[1]));
            }
            return lista;
        }

        @Override
        public Map<Integer, int[]> asistencia(LocalDate desde, LocalDate hasta) throws DatosException {
            Map<Object, Map<String, Object>> usuarios = BaseDatos.indice(BaseDatos.USUARIO, "id_usuario");
            Map<Integer, int[]> mapa = new LinkedHashMap<>();
            for (Map<String, Object> s : BaseDatos.tabla(BaseDatos.ASISTENCIA)) {
                LocalDate fecha = BaseDatos.fecha(s, "fecha");
                if (fecha == null || fecha.isBefore(desde) || fecha.isAfter(hasta)) {
                    continue;
                }
                Integer idEstudiante = BaseDatos.enteroPrimitivo(s, "id_estudiante");
                Map<String, Object> u = usuarios.get(idEstudiante);
                if (u == null || !BaseDatos.igual(BaseDatos.entero(u, "id_institucion"), idInstitucion)) {
                    continue;
                }
                int[] acc = mapa.computeIfAbsent(idEstudiante, x -> new int[2]);
                acc[0]++;
                if ("AUSENTE".equals(BaseDatos.texto(s, "estado"))) {
                    acc[1]++;
                }
            }
            return mapa;
        }

        @Override
        public List<AlertaExistente> alertasDelPeriodo(int idPeriodo) throws DatosException {
            Map<Object, Map<String, Object>> usuarios = BaseDatos.indice(BaseDatos.USUARIO, "id_usuario");
            List<AlertaExistente> lista = new ArrayList<>();
            for (Map<String, Object> al : BaseDatos.tabla(BaseDatos.ALERTA_DESERCION)) {
                if (!BaseDatos.igual(BaseDatos.entero(al, "id_periodo"), idPeriodo)) {
                    continue;
                }
                Map<String, Object> u = usuarios.get(BaseDatos.entero(al, "id_estudiante"));
                if (u == null || !BaseDatos.igual(BaseDatos.entero(u, "id_institucion"), idInstitucion)) {
                    continue;
                }
                lista.add(new AlertaExistente(BaseDatos.enteroPrimitivo(al, "id_alerta"),
                        BaseDatos.enteroPrimitivo(al, "id_estudiante"), BaseDatos.texto(al, "estado")));
            }
            return lista;
        }

        @Override
        public void guardarDefinitivas(int idPeriodo, List<Promedio> promedios) throws DatosException {
            if (promedios.isEmpty()) {
                return;
            }
            Map<String, Map<String, Object>> existentes = new LinkedHashMap<>();
            for (Map<String, Object> n : BaseDatos.tabla(BaseDatos.NOTA_PERIODO)) {
                existentes.putIfAbsent(clave(BaseDatos.enteroPrimitivo(n, "id_estudiante"),
                        BaseDatos.enteroPrimitivo(n, "id_asignacion"), idPeriodo), n);
            }
            for (Promedio p : promedios) {
                double valor = AnalisisRiesgo.redondear(p.promedio);
                Map<String, Object> n = existentes.get(clave(p.idEstudiante, p.idAsignacion, idPeriodo));
                if (n == null) {
                    n = BaseDatos.nuevo();
                    n.put("id_nota_periodo", BaseDatos.nuevoId(BaseDatos.NOTA_PERIODO, "id_nota_periodo"));
                    n.put("id_estudiante", p.idEstudiante);
                    n.put("id_asignacion", p.idAsignacion);
                    n.put("id_periodo", idPeriodo);
                    n.put("nota_definitiva", valor);
                    BaseDatos.insertar(BaseDatos.NOTA_PERIODO, n);
                } else {
                    n.put("nota_definitiva", valor);
                    BaseDatos.marcarSucia(BaseDatos.NOTA_PERIODO);
                }
            }
        }

        @Override
        public void crearAlerta(int idPeriodo, Evaluacion e) throws DatosException {
            Map<String, Object> al = BaseDatos.nuevo();
            al.put("id_alerta", BaseDatos.nuevoId(BaseDatos.ALERTA_DESERCION, "id_alerta"));
            al.put("id_estudiante", e.idEstudiante);
            al.put("id_docente", null);
            al.put("id_psicosocial", null);
            al.put("id_periodo", idPeriodo);
            al.put("fecha", LocalDate.now().toString());
            al.put("nivel_riesgo", e.nivel);
            al.put("porcentaje_asistencia", e.asistencia);
            al.put("materias_perdidas", e.materiasPerdidas.size());
            al.put("motivo", e.motivo);
            al.put("estado", "PENDIENTE");
            BaseDatos.insertar(BaseDatos.ALERTA_DESERCION, al);
        }

        @Override
        public void actualizarAlerta(int idAlerta, Evaluacion e) throws DatosException {
            Map<String, Object> al = BaseDatos.porId(BaseDatos.ALERTA_DESERCION, "id_alerta", idAlerta);
            if (al == null || !"PENDIENTE".equals(BaseDatos.texto(al, "estado"))) {
                return;
            }
            al.put("fecha", LocalDate.now().toString());
            al.put("nivel_riesgo", e.nivel);
            al.put("porcentaje_asistencia", e.asistencia);
            al.put("materias_perdidas", e.materiasPerdidas.size());
            al.put("motivo", e.motivo);
            BaseDatos.marcarSucia(BaseDatos.ALERTA_DESERCION);
        }

        @Override
        public void eliminarAlerta(int idAlerta) throws DatosException {
            Map<String, Object> al = BaseDatos.porId(BaseDatos.ALERTA_DESERCION, "id_alerta", idAlerta);
            if (al != null && "PENDIENTE".equals(BaseDatos.texto(al, "estado"))) {
                BaseDatos.eliminar(BaseDatos.ALERTA_DESERCION, al);
            }
        }

        @Override
        public void marcarCerrado(int idPeriodo) throws DatosException {
            for (Map<String, Object> p : BaseDatos.tabla(BaseDatos.PERIODO_ACADEMICO)) {
                if (BaseDatos.igual(BaseDatos.entero(p, "id_periodo"), idPeriodo)
                        && BaseDatos.igual(BaseDatos.entero(p, "id_institucion"), idInstitucion)) {
                    p.put("estado", "CERRADO");
                    BaseDatos.marcarSucia(BaseDatos.PERIODO_ACADEMICO);
                }
            }
        }
    }

    private static String clave(int idEstudiante, int idAsignacion, int idPeriodo) {
        return idEstudiante + "|" + idAsignacion + "|" + idPeriodo;
    }
}
