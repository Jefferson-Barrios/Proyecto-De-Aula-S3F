package dao;

import eipi.util.BaseDatos;
import eipi.util.DatosException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import modelo.Docente;

public class DocenteDAO {

    public int crear(Docente d) throws DatosException {
        return BaseDatos.enTransaccion(() -> {
            int id = UsuarioDAO.insertarUsuario(d);
            Map<String, Object> fila = BaseDatos.nuevo();
            fila.put("id_usuario", id);
            fila.put("carga_academica", d.getCargaAcademica());
            fila.put("jornada", d.getJornada());
            BaseDatos.insertar(BaseDatos.DOCENTE, fila);
            return id;
        });
    }

    public List<Object[]> listar(int idInstitucion) throws DatosException {
        List<Object[]> filas = new ArrayList<>();
        for (Map<String, Object> u : BaseDatos.tabla(BaseDatos.USUARIO)) {
            if (!BaseDatos.igual(BaseDatos.entero(u, "id_institucion"), idInstitucion)) {
                continue;
            }
            Map<String, Object> d = BaseDatos.porId(BaseDatos.DOCENTE, "id_usuario",
                    BaseDatos.entero(u, "id_usuario"));
            if (d == null) {
                continue;
            }
            filas.add(new Object[] {
                    BaseDatos.enteroPrimitivo(u, "id_usuario"),
                    BaseDatos.texto(u, "nombre"),
                    BaseDatos.texto(u, "documento"),
                    BaseDatos.texto(u, "usuario"),
                    BaseDatos.texto(d, "jornada"),
                    BaseDatos.texto(d, "carga_academica"),
                    BaseDatos.texto(u, "estado") });
        }
        filas.sort((a, b) -> Ordenes.limpio((String) a[1]).compareToIgnoreCase(Ordenes.limpio((String) b[1])));
        return filas;
    }

    public List<Docente> listarActivos(int idInstitucion) throws DatosException {
        List<Docente> lista = new ArrayList<>();
        List<Object[]> filas = new ArrayList<>();
        for (Map<String, Object> u : BaseDatos.tabla(BaseDatos.USUARIO)) {
            if (!BaseDatos.igual(BaseDatos.entero(u, "id_institucion"), idInstitucion)
                    || !"ACTIVO".equals(BaseDatos.texto(u, "estado"))) {
                continue;
            }
            if (BaseDatos.porId(BaseDatos.DOCENTE, "id_usuario", BaseDatos.entero(u, "id_usuario")) == null) {
                continue;
            }
            filas.add(new Object[] {
                    BaseDatos.enteroPrimitivo(u, "id_usuario"),
                    BaseDatos.texto(u, "nombre") });
        }
        filas.sort((a, b) -> Ordenes.limpio((String) a[1]).compareToIgnoreCase(Ordenes.limpio((String) b[1])));
        for (Object[] f : filas) {
            Docente d = new Docente();
            d.setIdUsuario((Integer) f[0]);
            d.setNombre((String) f[1]);
            lista.add(d);
        }
        return lista;
    }

    public Docente obtener(int idUsuario, int idInstitucion) throws DatosException {
        Map<String, Object> u = BaseDatos.porId(BaseDatos.USUARIO, "id_usuario", idUsuario);
        if (u == null || !BaseDatos.igual(BaseDatos.entero(u, "id_institucion"), idInstitucion)) {
            return null;
        }
        Map<String, Object> d = BaseDatos.porId(BaseDatos.DOCENTE, "id_usuario", idUsuario);
        if (d == null) {
            return null;
        }
        Docente docente = new Docente();
        docente.setIdUsuario(BaseDatos.enteroPrimitivo(u, "id_usuario"));
        docente.setNombre(BaseDatos.texto(u, "nombre"));
        docente.setDocumento(BaseDatos.texto(u, "documento"));
        docente.setTelefono(BaseDatos.texto(u, "telefono"));
        docente.setCorreo(BaseDatos.texto(u, "correo"));
        docente.setCargaAcademica(BaseDatos.texto(d, "carga_academica"));
        docente.setJornada(BaseDatos.texto(d, "jornada"));
        return docente;
    }

    public void actualizar(Docente d, int idInstitucion) throws DatosException {
        BaseDatos.enTransaccion(() -> {
            if (UsuarioDAO.actualizarDatosBasicos(d, idInstitucion) == 0) {
                throw new IllegalStateException("No se encontró el docente en tu institución.");
            }
            Map<String, Object> fila = BaseDatos.porId(BaseDatos.DOCENTE, "id_usuario", d.getIdUsuario());
            if (fila != null) {
                fila.put("carga_academica", d.getCargaAcademica());
                fila.put("jornada", d.getJornada());
                BaseDatos.marcarSucia(BaseDatos.DOCENTE);
            }
            return null;
        });
    }
}
