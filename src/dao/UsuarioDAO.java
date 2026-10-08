package dao;

import eipi.util.Conexion;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import modelo.AdministradorGeneral;
import modelo.Directiva;
import modelo.Docente;
import modelo.Estudiante;
import modelo.Psicosocial;
import modelo.Usuario;

public class UsuarioDAO {

    private static final String SQL_LOGIN =
            "SELECT u.*, "
            + "d.carga_academica, d.jornada AS jornada_docente, "
            + "p.area, p.perfil_profesional, p.grados_monitorear, "
            + "e.id_curso, e.fecha_nacimiento, e.edad, e.direccion, e.barrio, "
            + "e.tipo_documento, e.grado, e.jornada AS jornada_estudiante "
            + "FROM usuario u "
            + "LEFT JOIN docente d ON d.id_usuario = u.id_usuario "
            + "LEFT JOIN psicosocial p ON p.id_usuario = u.id_usuario "
            + "LEFT JOIN estudiante e ON e.id_usuario = u.id_usuario "
            + "WHERE u.usuario = ? AND u.contrasena = ? AND u.estado = 'ACTIVO'";

    /**
     * Valida usuario y contraseña.
     * @return el usuario (del tipo según su rol) o null si las credenciales son incorrectas.
     */
    public Usuario autenticar(String usuario, String clave) throws SQLException {
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(SQL_LOGIN)) {
            ps.setString(1, usuario);
            ps.setString(2, clave);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return construir(rs);
            }
        }
    }

    private Usuario construir(ResultSet rs) throws SQLException {
        Usuario u;
        String rol = rs.getString("rol");

        switch (rol) {
            case "ADMINISTRADOR":
                u = new AdministradorGeneral();
                break;
            case "DIRECTIVA":
                u = new Directiva();
                break;
            case "DOCENTE": {
                Docente d = new Docente();
                d.setCargaAcademica(rs.getString("carga_academica"));
                d.setJornada(rs.getString("jornada_docente"));
                u = d;
                break;
            }
            case "PSICOSOCIAL": {
                Psicosocial p = new Psicosocial();
                p.setArea(rs.getString("area"));
                p.setPerfilProfesional(rs.getString("perfil_profesional"));
                p.setGradosMonitorear(rs.getString("grados_monitorear"));
                u = p;
                break;
            }
            case "ESTUDIANTE": {
                Estudiante e = new Estudiante();
                e.setIdCurso(leerInteger(rs, "id_curso"));
                Date nac = rs.getDate("fecha_nacimiento");
                if (nac != null) {
                    e.setFechaNacimiento(nac.toLocalDate());
                }
                e.setEdad(rs.getInt("edad"));
                e.setDireccion(rs.getString("direccion"));
                e.setBarrio(rs.getString("barrio"));
                e.setTipoDocumento(rs.getString("tipo_documento"));
                e.setGrado(rs.getString("grado"));
                e.setJornada(rs.getString("jornada_estudiante"));
                u = e;
                break;
            }
            default:
                throw new SQLException("Rol desconocido: " + rol);
        }

        u.setIdUsuario(rs.getInt("id_usuario"));
        u.setIdInstitucion(leerInteger(rs, "id_institucion"));
        u.setNombre(rs.getString("nombre"));
        u.setDocumento(rs.getString("documento"));
        u.setTelefono(rs.getString("telefono"));
        u.setCorreo(rs.getString("correo"));
        u.setUsuario(rs.getString("usuario"));
        u.setContrasena(rs.getString("contrasena"));
        u.setEstado(rs.getString("estado"));
        return u;
    }

    private Integer leerInteger(ResultSet rs, String columna) throws SQLException {
        int valor = rs.getInt(columna);
        return rs.wasNull() ? null : valor;
    }
}
