package dao;

import eipi.util.BaseDatos;
import eipi.util.DatosException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import modelo.PeriodoAcademico;

public class PeriodoDAO {

    public List<PeriodoAcademico> listar(int idInstitucion) throws DatosException {
        List<PeriodoAcademico> lista = new ArrayList<>();
        for (Map<String, Object> p : BaseDatos.tabla(BaseDatos.PERIODO_ACADEMICO)) {
            if (!BaseDatos.igual(BaseDatos.entero(p, "id_institucion"), idInstitucion)) {
                continue;
            }
            PeriodoAcademico periodo = new PeriodoAcademico();
            periodo.setIdPeriodo(BaseDatos.enteroPrimitivo(p, "id_periodo"));
            periodo.setIdInstitucion(BaseDatos.enteroPrimitivo(p, "id_institucion"));
            periodo.setNombre(BaseDatos.texto(p, "nombre"));
            periodo.setFechaInicio(BaseDatos.fecha(p, "fecha_inicio"));
            periodo.setFechaFin(BaseDatos.fecha(p, "fecha_fin"));
            periodo.setEstado(BaseDatos.texto(p, "estado"));
            lista.add(periodo);
        }
        lista.sort((a, b) -> {
            int c = a.getFechaInicio().compareTo(b.getFechaInicio());
            return c != 0 ? c : Integer.compare(a.getIdPeriodo(), b.getIdPeriodo());
        });
        return lista;
    }

    public int insertar(PeriodoAcademico p) throws DatosException {
        Map<String, Object> fila = BaseDatos.nuevo();
        int id = BaseDatos.nuevoId(BaseDatos.PERIODO_ACADEMICO, "id_periodo");
        fila.put("id_periodo", id);
        fila.put("id_institucion", p.getIdInstitucion());
        fila.put("nombre", p.getNombre());
        fila.put("fecha_inicio", p.getFechaInicio().toString());
        fila.put("fecha_fin", p.getFechaFin().toString());
        fila.put("estado", p.getEstado());
        BaseDatos.insertar(BaseDatos.PERIODO_ACADEMICO, fila);
        return id;
    }

    public void actualizar(PeriodoAcademico p) throws DatosException {
        Map<String, Object> fila = BaseDatos.porId(BaseDatos.PERIODO_ACADEMICO, "id_periodo", p.getIdPeriodo());
        if (fila != null) {
            fila.put("nombre", p.getNombre());
            fila.put("fecha_inicio", p.getFechaInicio().toString());
            fila.put("fecha_fin", p.getFechaFin().toString());
            BaseDatos.actualizada(BaseDatos.PERIODO_ACADEMICO);
        }
    }

    public void cambiarEstado(int idPeriodo, String estado) throws DatosException {
        Map<String, Object> fila = BaseDatos.porId(BaseDatos.PERIODO_ACADEMICO, "id_periodo", idPeriodo);
        if (fila != null) {
            fila.put("estado", estado);
            BaseDatos.actualizada(BaseDatos.PERIODO_ACADEMICO);
        }
    }
}
