package dao;

import eipi.util.BaseDatos;
import eipi.util.DatosException;
import eipi.util.Formato;
import java.util.Map;

public class ExpedienteDAO {

    public Object[] obtener(int idEstudiante) throws DatosException {
        Map<String, Object> fila = BaseDatos.porId(BaseDatos.EXPEDIENTE, "id_estudiante", idEstudiante);
        if (fila == null) {
            return null;
        }
        return new Object[] {
                Formato.texto(BaseDatos.fecha(fila, "fecha_creacion")),
                BaseDatos.texto(fila, "estado") };
    }
}
