package dao;

import eipi.util.BaseDatos;
import eipi.util.DatosException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import modelo.Psicosocial;

public class PsicosocialDAO {

    public int crear(Psicosocial p) throws DatosException {
        return BaseDatos.enTransaccion(() -> {
            int id = UsuarioDAO.insertarUsuario(p);
            Map<String, Object> fila = BaseDatos.nuevo();
            fila.put("id_usuario", id);
            fila.put("area", p.getArea());
            fila.put("perfil_profesional", p.getPerfilProfesional());
            fila.put("grados_monitorear", p.getGradosMonitorear());
            BaseDatos.insertar(BaseDatos.PSICOSOCIAL, fila);
            return id;
        });
    }

    public List<Object[]> listar(int idInstitucion) throws DatosException {
        List<Object[]> filas = new ArrayList<>();
        for (Map<String, Object> u : BaseDatos.tabla(BaseDatos.USUARIO)) {
            if (!BaseDatos.igual(BaseDatos.entero(u, "id_institucion"), idInstitucion)) {
                continue;
            }
            Map<String, Object> p = BaseDatos.porId(BaseDatos.PSICOSOCIAL, "id_usuario",
                    BaseDatos.entero(u, "id_usuario"));
            if (p == null) {
                continue;
            }
            filas.add(new Object[] {
                    BaseDatos.enteroPrimitivo(u, "id_usuario"),
                    BaseDatos.texto(u, "nombre"),
                    BaseDatos.texto(u, "documento"),
                    BaseDatos.texto(u, "usuario"),
                    BaseDatos.texto(p, "area"),
                    BaseDatos.texto(p, "grados_monitorear"),
                    BaseDatos.texto(u, "estado") });
        }
        filas.sort((a, b) -> Ordenes.limpio((String) a[1]).compareToIgnoreCase(Ordenes.limpio((String) b[1])));
        return filas;
    }

    public Psicosocial obtener(int idUsuario, int idInstitucion) throws DatosException {
        Map<String, Object> u = BaseDatos.porId(BaseDatos.USUARIO, "id_usuario", idUsuario);
        if (u == null || !BaseDatos.igual(BaseDatos.entero(u, "id_institucion"), idInstitucion)) {
            return null;
        }
        Map<String, Object> p = BaseDatos.porId(BaseDatos.PSICOSOCIAL, "id_usuario", idUsuario);
        if (p == null) {
            return null;
        }
        Psicosocial psi = new Psicosocial();
        psi.setIdUsuario(BaseDatos.enteroPrimitivo(u, "id_usuario"));
        psi.setNombre(BaseDatos.texto(u, "nombre"));
        psi.setDocumento(BaseDatos.texto(u, "documento"));
        psi.setTelefono(BaseDatos.texto(u, "telefono"));
        psi.setCorreo(BaseDatos.texto(u, "correo"));
        psi.setArea(BaseDatos.texto(p, "area"));
        psi.setPerfilProfesional(BaseDatos.texto(p, "perfil_profesional"));
        psi.setGradosMonitorear(BaseDatos.texto(p, "grados_monitorear"));
        return psi;
    }

    public void actualizar(Psicosocial p, int idInstitucion) throws DatosException {
        BaseDatos.enTransaccion(() -> {
            if (UsuarioDAO.actualizarDatosBasicos(p, idInstitucion) == 0) {
                throw new IllegalStateException("No se encontró el profesional en tu institución.");
            }
            Map<String, Object> fila = BaseDatos.porId(BaseDatos.PSICOSOCIAL, "id_usuario", p.getIdUsuario());
            if (fila != null) {
                fila.put("area", p.getArea());
                fila.put("perfil_profesional", p.getPerfilProfesional());
                fila.put("grados_monitorear", p.getGradosMonitorear());
                BaseDatos.marcarSucia(BaseDatos.PSICOSOCIAL);
            }
            return null;
        });
    }
}
