package dao;

import eipi.util.BaseDatos;
import eipi.util.DatosException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import modelo.Institucion;

public class InstitucionDAO {

    public List<Institucion> listar() throws DatosException {
        List<Institucion> lista = new ArrayList<>();
        for (Map<String, Object> i : BaseDatos.tabla(BaseDatos.INSTITUCION)) {
            lista.add(mapear(i));
        }
        lista.sort((a, b) -> Ordenes.limpio(a.getNombre()).compareToIgnoreCase(Ordenes.limpio(b.getNombre())));
        return lista;
    }

    public int insertar(Institucion i) throws DatosException {
        validarCodigo(i.getCodigoOficial(), null);
        Map<String, Object> fila = BaseDatos.nuevo();
        int id = BaseDatos.nuevoId(BaseDatos.INSTITUCION, "id_institucion");
        fila.put("id_institucion", id);
        fila.put("id_administrador", i.getIdAdministrador());
        fila.put("nombre", i.getNombre());
        fila.put("codigo_oficial", i.getCodigoOficial());
        fila.put("sede_principal", i.getSedePrincipal());
        fila.put("barrio_sector", i.getBarrioSector());
        fila.put("telefono_institucional", i.getTelefonoInstitucional());
        fila.put("correo_institucional", i.getCorreoInstitucional());
        fila.put("nombre_rector", i.getNombreRector());
        fila.put("correo_responsable", i.getCorreoResponsable());
        fila.put("telefono_responsable", i.getTelefonoResponsable());
        fila.put("estado", i.getEstado());
        BaseDatos.insertar(BaseDatos.INSTITUCION, fila);
        return id;
    }

    public void actualizar(Institucion i) throws DatosException {
        validarCodigo(i.getCodigoOficial(), i.getIdInstitucion());
        Map<String, Object> fila = BaseDatos.porId(BaseDatos.INSTITUCION, "id_institucion", i.getIdInstitucion());
        if (fila == null) {
            return;
        }
        fila.put("nombre", i.getNombre());
        fila.put("codigo_oficial", i.getCodigoOficial());
        fila.put("sede_principal", i.getSedePrincipal());
        fila.put("barrio_sector", i.getBarrioSector());
        fila.put("telefono_institucional", i.getTelefonoInstitucional());
        fila.put("correo_institucional", i.getCorreoInstitucional());
        fila.put("nombre_rector", i.getNombreRector());
        fila.put("correo_responsable", i.getCorreoResponsable());
        fila.put("telefono_responsable", i.getTelefonoResponsable());
        BaseDatos.actualizada(BaseDatos.INSTITUCION);
    }

    public void cambiarEstado(int idInstitucion, String estado) throws DatosException {
        Map<String, Object> fila = BaseDatos.porId(BaseDatos.INSTITUCION, "id_institucion", idInstitucion);
        if (fila != null) {
            fila.put("estado", estado);
            BaseDatos.actualizada(BaseDatos.INSTITUCION);
        }
    }

    private void validarCodigo(String codigo, Integer excluirId) throws DatosException {
        if (codigo == null || codigo.isEmpty()) {
            return;
        }
        for (Map<String, Object> fila : BaseDatos.tabla(BaseDatos.INSTITUCION)) {
            if (excluirId != null && BaseDatos.igual(BaseDatos.entero(fila, "id_institucion"), excluirId)) {
                continue;
            }
            if (codigo.equals(BaseDatos.texto(fila, "codigo_oficial"))) {
                throw new DatosException("Ya existe una institución con ese código oficial.");
            }
        }
    }

    private Institucion mapear(Map<String, Object> fila) {
        Institucion i = new Institucion();
        i.setIdInstitucion(BaseDatos.enteroPrimitivo(fila, "id_institucion"));
        i.setIdAdministrador(BaseDatos.entero(fila, "id_administrador"));
        i.setNombre(BaseDatos.texto(fila, "nombre"));
        i.setCodigoOficial(BaseDatos.texto(fila, "codigo_oficial"));
        i.setSedePrincipal(BaseDatos.texto(fila, "sede_principal"));
        i.setBarrioSector(BaseDatos.texto(fila, "barrio_sector"));
        i.setTelefonoInstitucional(BaseDatos.texto(fila, "telefono_institucional"));
        i.setCorreoInstitucional(BaseDatos.texto(fila, "correo_institucional"));
        i.setNombreRector(BaseDatos.texto(fila, "nombre_rector"));
        i.setCorreoResponsable(BaseDatos.texto(fila, "correo_responsable"));
        i.setTelefonoResponsable(BaseDatos.texto(fila, "telefono_responsable"));
        i.setEstado(BaseDatos.texto(fila, "estado"));
        return i;
    }
}
