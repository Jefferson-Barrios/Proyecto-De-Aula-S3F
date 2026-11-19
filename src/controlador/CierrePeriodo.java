package controlador;

import eipi.util.DatosException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import modelo.PeriodoAcademico;

public final class CierrePeriodo {

    private CierrePeriodo() {
    }

    public static final class Estudiante {
        public final int id;
        public final String nombre;
        public final String curso;

        public Estudiante(int id, String nombre, String curso) {
            this.id = id;
            this.nombre = nombre;
            this.curso = curso;
        }
    }

    public static final class Promedio {
        public final int idEstudiante;
        public final int idAsignacion;
        public final int idMateria;
        public final String materia;
        public final double promedio;

        public Promedio(int idEstudiante, int idAsignacion, int idMateria, String materia, double promedio) {
            this.idEstudiante = idEstudiante;
            this.idAsignacion = idAsignacion;
            this.idMateria = idMateria;
            this.materia = materia;
            this.promedio = promedio;
        }
    }

    public static final class AlertaExistente {
        public final int idAlerta;
        public final int idEstudiante;
        public final String estado;

        public AlertaExistente(int idAlerta, int idEstudiante, String estado) {
            this.idAlerta = idAlerta;
            this.idEstudiante = idEstudiante;
            this.estado = estado;
        }
    }

    public static final class Evaluacion {
        public final int idEstudiante;
        public final String nombre;
        public final String curso;
        public final Double asistencia;
        public final List<String> materiasPerdidas;
        public final String nivel;
        public final String motivo;

        public Evaluacion(int idEstudiante, String nombre, String curso, Double asistencia,
                List<String> materiasPerdidas, String nivel, String motivo) {
            this.idEstudiante = idEstudiante;
            this.nombre = nombre;
            this.curso = curso;
            this.asistencia = asistencia;
            this.materiasPerdidas = materiasPerdidas;
            this.nivel = nivel;
            this.motivo = motivo;
        }
    }

    public static final class Resultado {
        public int evaluados;
        public int creadas;
        public int actualizadas;
        public int retiradas;
        public int enAtencion;
        public int altas;
        public int medias;
        public int preventivas;

        public String resumen() {
            StringBuilder sb = new StringBuilder("Período cerrado. Estudiantes evaluados: " + evaluados + ". ");
            int generadas = creadas + actualizadas;
            if (generadas == 0 && retiradas == 0 && enAtencion == 0) {
                return sb.append("Ninguno presenta riesgo: no se generaron alertas.").toString();
            }
            sb.append("Alertas generadas: ").append(generadas)
                    .append(" (").append(creadas).append(creadas == 1 ? " nueva" : " nuevas")
                    .append(", ").append(actualizadas).append(actualizadas == 1 ? " actualizada" : " actualizadas")
                    .append(") — ").append(altas).append(altas == 1 ? " alta, " : " altas, ")
                    .append(medias).append(medias == 1 ? " media y " : " medias y ")
                    .append(preventivas).append(preventivas == 1 ? " preventiva." : " preventivas.");
            if (retiradas > 0) {
                sb.append(" Retiradas por ya no tener riesgo: ").append(retiradas).append(".");
            }
            if (enAtencion > 0) {
                sb.append(" Ya atendidas por el psicosocial (no se modifican): ").append(enAtencion).append(".");
            }
            return sb.toString();
        }
    }

    public interface Almacen {

        List<Estudiante> estudiantes() throws DatosException;

        List<Promedio> promedios(int idPeriodo) throws DatosException;

        Map<Integer, int[]> asistencia(LocalDate desde, LocalDate hasta) throws DatosException;

        List<AlertaExistente> alertasDelPeriodo(int idPeriodo) throws DatosException;

        void guardarDefinitivas(int idPeriodo, List<Promedio> promedios) throws DatosException;

        void crearAlerta(int idPeriodo, Evaluacion e) throws DatosException;

        void actualizarAlerta(int idAlerta, Evaluacion e) throws DatosException;

        void eliminarAlerta(int idAlerta) throws DatosException;

        void marcarCerrado(int idPeriodo) throws DatosException;
    }

    public static List<Evaluacion> evaluar(Almacen a, PeriodoAcademico p) throws DatosException {
        return evaluarConDatos(p, a.estudiantes(), a.promedios(p.getIdPeriodo()),
                a.asistencia(p.getFechaInicio(), p.getFechaFin()));
    }

    static List<Evaluacion> evaluarConDatos(PeriodoAcademico p, List<Estudiante> estudiantes,
            List<Promedio> promedios, Map<Integer, int[]> asistencia) {

        Map<Integer, Map<Integer, String>> perdidas = new HashMap<>();
        for (Promedio pr : promedios) {
            if (AnalisisRiesgo.esMateriaPerdida(pr.promedio)) {
                perdidas.computeIfAbsent(pr.idEstudiante, k -> new LinkedHashMap<>()).put(pr.idMateria, pr.materia);
            }
        }
        List<Evaluacion> r = new ArrayList<>();
        for (Estudiante e : estudiantes) {
            List<String> materias = new ArrayList<>(
                    perdidas.getOrDefault(e.id, new LinkedHashMap<>()).values());
            int[] asis = asistencia.get(e.id);
            Double pct = asis == null ? null : AnalisisRiesgo.porcentajeAsistencia(asis[0], asis[1]);
            String nivel = AnalisisRiesgo.clasificar(pct, materias.size());
            String motivo = AnalisisRiesgo.motivo(p.getNombre(), pct, materias, nivel);
            r.add(new Evaluacion(e.id, e.nombre, e.curso, pct, materias, nivel, motivo));
        }
        return r;
    }

    static final class Plan {
        final List<Evaluacion> crear = new ArrayList<>();
        final Map<Integer, Evaluacion> actualizar = new LinkedHashMap<>();
        final List<Integer> eliminar = new ArrayList<>();
        int enAtencion = 0;
    }

    static Plan planificar(List<Evaluacion> evaluaciones, List<AlertaExistente> existentes) {
        Map<Integer, AlertaExistente> porEstudiante = new HashMap<>();
        for (AlertaExistente x : existentes) {
            porEstudiante.putIfAbsent(x.idEstudiante, x);
        }
        Plan plan = new Plan();
        for (Evaluacion e : evaluaciones) {
            AlertaExistente x = porEstudiante.get(e.idEstudiante);
            boolean pendiente = x != null && "PENDIENTE".equals(x.estado);
            if (!AnalisisRiesgo.generaAlerta(e.nivel)) {
                if (pendiente) {
                    plan.eliminar.add(x.idAlerta);
                }
            } else if (x == null) {
                plan.crear.add(e);
            } else if (pendiente) {
                plan.actualizar.put(x.idAlerta, e);
            } else {
                plan.enAtencion++;
            }
        }
        return plan;
    }

    public static Resultado cerrar(Almacen a, PeriodoAcademico p) throws DatosException {
        List<Promedio> promedios = a.promedios(p.getIdPeriodo());
        a.guardarDefinitivas(p.getIdPeriodo(), promedios);

        List<Evaluacion> evaluaciones = evaluarConDatos(p, a.estudiantes(), promedios,
                a.asistencia(p.getFechaInicio(), p.getFechaFin()));
        Plan plan = planificar(evaluaciones, a.alertasDelPeriodo(p.getIdPeriodo()));

        Resultado r = new Resultado();
        r.evaluados = evaluaciones.size();
        for (Evaluacion e : plan.crear) {
            a.crearAlerta(p.getIdPeriodo(), e);
            r.creadas++;
            contar(r, e.nivel);
        }
        for (Map.Entry<Integer, Evaluacion> x : plan.actualizar.entrySet()) {
            a.actualizarAlerta(x.getKey(), x.getValue());
            r.actualizadas++;
            contar(r, x.getValue().nivel);
        }
        for (int idAlerta : plan.eliminar) {
            a.eliminarAlerta(idAlerta);
            r.retiradas++;
        }
        r.enAtencion = plan.enAtencion;
        a.marcarCerrado(p.getIdPeriodo());
        return r;
    }

    private static void contar(Resultado r, String nivel) {
        if (AnalisisRiesgo.ALTO.equals(nivel)) {
            r.altas++;
        } else if (AnalisisRiesgo.MEDIO.equals(nivel)) {
            r.medias++;
        } else if (AnalisisRiesgo.PREVENTIVA.equals(nivel)) {
            r.preventivas++;
        }
    }
}
