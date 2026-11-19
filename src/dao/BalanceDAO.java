package dao;

import eipi.util.BaseDatos;
import eipi.util.DatosException;
import java.util.LinkedHashMap;
import java.util.Map;

public class BalanceDAO {

    public Map<String, Integer> contarUsuariosActivosPorRol() throws DatosException {
        Map<String, Integer> mapa = new LinkedHashMap<>();
        for (Map<String, Object> u : BaseDatos.tabla(BaseDatos.USUARIO)) {
            if ("ACTIVO".equals(BaseDatos.texto(u, "estado"))) {
                acumular(mapa, BaseDatos.texto(u, "rol"));
            }
        }
        return mapa;
    }

    public int contarInstitucionesActivas() throws DatosException {
        int total = 0;
        for (Map<String, Object> i : BaseDatos.tabla(BaseDatos.INSTITUCION)) {
            if ("ACTIVO".equals(BaseDatos.texto(i, "estado"))) {
                total++;
            }
        }
        return total;
    }

    public Map<String, Integer> contarUsuariosActivosPorRol(int idInstitucion) throws DatosException {
        Map<String, Integer> mapa = new LinkedHashMap<>();
        for (Map<String, Object> u : BaseDatos.tabla(BaseDatos.USUARIO)) {
            if ("ACTIVO".equals(BaseDatos.texto(u, "estado"))
                    && BaseDatos.igual(BaseDatos.entero(u, "id_institucion"), idInstitucion)) {
                acumular(mapa, BaseDatos.texto(u, "rol"));
            }
        }
        return mapa;
    }

    public int contarCursos(int idInstitucion) throws DatosException {
        return contarPorInstitucion(BaseDatos.CURSO, idInstitucion);
    }

    public int contarMaterias(int idInstitucion) throws DatosException {
        return contarPorInstitucion(BaseDatos.MATERIA, idInstitucion);
    }

    private int contarPorInstitucion(String tabla, int idInstitucion) throws DatosException {
        int total = 0;
        for (Map<String, Object> fila : BaseDatos.tabla(tabla)) {
            if (BaseDatos.igual(BaseDatos.entero(fila, "id_institucion"), idInstitucion)) {
                total++;
            }
        }
        return total;
    }

    private static void acumular(Map<String, Integer> mapa, String clave) {
        if (clave == null) {
            return;
        }
        mapa.merge(clave, 1, Integer::sum);
    }
}
