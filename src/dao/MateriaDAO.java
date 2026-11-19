package dao;

import eipi.util.BaseDatos;
import eipi.util.DatosException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import modelo.Materia;

public class MateriaDAO {

    public List<Materia> listar(int idInstitucion) throws DatosException {
        List<Materia> lista = new ArrayList<>();
        for (Map<String, Object> m : BaseDatos.tabla(BaseDatos.MATERIA)) {
            if (!BaseDatos.igual(BaseDatos.entero(m, "id_institucion"), idInstitucion)) {
                continue;
            }
            Materia materia = new Materia();
            materia.setIdMateria(BaseDatos.enteroPrimitivo(m, "id_materia"));
            materia.setIdInstitucion(BaseDatos.enteroPrimitivo(m, "id_institucion"));
            materia.setNombre(BaseDatos.texto(m, "nombre"));
            materia.setDescripcion(BaseDatos.texto(m, "descripcion"));
            lista.add(materia);
        }
        lista.sort((a, b) -> Ordenes.limpio(a.getNombre()).compareToIgnoreCase(Ordenes.limpio(b.getNombre())));
        return lista;
    }

    public int insertar(Materia m) throws DatosException {
        Map<String, Object> fila = BaseDatos.nuevo();
        int id = BaseDatos.nuevoId(BaseDatos.MATERIA, "id_materia");
        fila.put("id_materia", id);
        fila.put("id_institucion", m.getIdInstitucion());
        fila.put("nombre", m.getNombre());
        fila.put("descripcion", m.getDescripcion());
        BaseDatos.insertar(BaseDatos.MATERIA, fila);
        return id;
    }

    public void actualizar(Materia m) throws DatosException {
        Map<String, Object> fila = BaseDatos.porId(BaseDatos.MATERIA, "id_materia", m.getIdMateria());
        if (fila != null) {
            fila.put("nombre", m.getNombre());
            fila.put("descripcion", m.getDescripcion());
            BaseDatos.actualizada(BaseDatos.MATERIA);
        }
    }
}
