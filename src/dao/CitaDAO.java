package dao;

import eipi.util.BaseDatos;
import eipi.util.DatosException;
import eipi.util.Formato;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import modelo.CitaPsicosocial;

public class CitaDAO {

    public void agendar(CitaPsicosocial c) throws DatosException {
        BaseDatos.enTransaccion(() -> {
            Map<String, Object> fila = BaseDatos.nuevo();
            fila.put("id_cita", BaseDatos.nuevoId(BaseDatos.CITA_PSICOSOCIAL, "id_cita"));
            fila.put("id_estudiante", c.getIdEstudiante());
            fila.put("id_psicosocial", c.getIdPsicosocial());
            fila.put("id_alerta", c.getIdAlerta());
            fila.put("fecha", c.getFecha().toString());
            fila.put("hora", c.getHora());
            fila.put("motivo", c.getMotivo());
            fila.put("estado", "AGENDADA");
            BaseDatos.insertar(BaseDatos.CITA_PSICOSOCIAL, fila);

            if (c.getIdAlerta() != null) {
                Map<String, Object> alerta = BaseDatos.porId(BaseDatos.ALERTA_DESERCION, "id_alerta", c.getIdAlerta());
                if (alerta != null && "PENDIENTE".equals(BaseDatos.texto(alerta, "estado"))) {
                    alerta.put("id_psicosocial", c.getIdPsicosocial());
                    alerta.put("estado", "EN SEGUIMIENTO");
                    BaseDatos.marcarSucia(BaseDatos.ALERTA_DESERCION);
                }
            }
            return null;
        });
    }

    public boolean hayConflicto(int idPsicosocial, LocalDate fecha, String hora) throws DatosException {
        for (Map<String, Object> c : BaseDatos.tabla(BaseDatos.CITA_PSICOSOCIAL)) {
            if (BaseDatos.igual(BaseDatos.entero(c, "id_psicosocial"), idPsicosocial)
                    && BaseDatos.igual(BaseDatos.texto(c, "fecha"), fecha.toString())
                    && BaseDatos.igual(BaseDatos.texto(c, "hora"), hora)
                    && "AGENDADA".equals(BaseDatos.texto(c, "estado"))) {
                return true;
            }
        }
        return false;
    }

    public void cambiarEstado(int idCita, String estado) throws DatosException {
        Map<String, Object> fila = BaseDatos.porId(BaseDatos.CITA_PSICOSOCIAL, "id_cita", idCita);
        if (fila != null) {
            fila.put("estado", estado);
            BaseDatos.actualizada(BaseDatos.CITA_PSICOSOCIAL);
        }
    }

    public List<Object[]> listarDePsicosocial(int idPsicosocial) throws DatosException {
        Map<Object, Map<String, Object>> cursos = BaseDatos.indice(BaseDatos.CURSO, "id_curso");
        List<Object[]> filas = new ArrayList<>();
        for (Map<String, Object> c : BaseDatos.tabla(BaseDatos.CITA_PSICOSOCIAL)) {
            if (!BaseDatos.igual(BaseDatos.entero(c, "id_psicosocial"), idPsicosocial)) {
                continue;
            }
            Map<String, Object> u = BaseDatos.porId(BaseDatos.USUARIO, "id_usuario",
                    BaseDatos.entero(c, "id_estudiante"));
            Map<String, Object> e = BaseDatos.porId(BaseDatos.ESTUDIANTE, "id_usuario",
                    BaseDatos.entero(c, "id_estudiante"));
            Map<String, Object> curso = e == null ? null : cursos.get(BaseDatos.entero(e, "id_curso"));
            filas.add(new Object[] {
                    BaseDatos.enteroPrimitivo(c, "id_cita"),
                    Formato.texto(BaseDatos.fecha(c, "fecha")),
                    BaseDatos.texto(c, "hora"),
                    u == null ? null : BaseDatos.texto(u, "nombre"),
                    curso == null ? null : BaseDatos.texto(curso, "grado") + "-" + BaseDatos.texto(curso, "grupo"),
                    BaseDatos.texto(c, "motivo"),
                    BaseDatos.texto(c, "estado"),
                    Ordenes.limpio(BaseDatos.texto(c, "fecha")) });
        }
        filas.sort((a, b) -> {
            int c = Integer.compare("AGENDADA".equals(a[6]) ? 0 : 1, "AGENDADA".equals(b[6]) ? 0 : 1);
            if (c != 0) {
                return c;
            }
            c = ((String) a[7]).compareTo((String) b[7]);
            return c != 0 ? c : Ordenes.limpio((String) a[2]).compareTo(Ordenes.limpio((String) b[2]));
        });
        return recortar(filas);
    }

    public List<Object[]> listarDeEstudiante(int idEstudiante) throws DatosException {
        List<Object[]> filas = new ArrayList<>();
        for (Map<String, Object> c : BaseDatos.tabla(BaseDatos.CITA_PSICOSOCIAL)) {
            if (!BaseDatos.igual(BaseDatos.entero(c, "id_estudiante"), idEstudiante)) {
                continue;
            }
            Map<String, Object> p = BaseDatos.porId(BaseDatos.USUARIO, "id_usuario",
                    BaseDatos.entero(c, "id_psicosocial"));
            filas.add(new Object[] {
                    Formato.texto(BaseDatos.fecha(c, "fecha")),
                    BaseDatos.texto(c, "hora"),
                    p == null ? null : BaseDatos.texto(p, "nombre"),
                    BaseDatos.texto(c, "motivo"),
                    BaseDatos.texto(c, "estado"),
                    Ordenes.limpio(BaseDatos.texto(c, "fecha")) });
        }
        filas.sort((a, b) -> {
            int c = ((String) b[5]).compareTo((String) a[5]);
            return c != 0 ? c : Ordenes.limpio((String) b[1]).compareTo(Ordenes.limpio((String) a[1]));
        });
        return recortar(filas);
    }

    private static List<Object[]> recortar(List<Object[]> filas) {
        List<Object[]> resultado = new ArrayList<>(filas.size());
        for (Object[] f : filas) {
            Object[] nueva = new Object[f.length - 1];
            System.arraycopy(f, 0, nueva, 0, nueva.length);
            resultado.add(nueva);
        }
        return resultado;
    }
}
