package dao;

import eipi.util.BaseDatos;
import eipi.util.Contrasenas;
import eipi.util.DatosException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import modelo.AdministradorGeneral;
import modelo.Directiva;
import modelo.Docente;
import modelo.Estudiante;
import modelo.Psicosocial;
import modelo.Usuario;

public class UsuarioDAO {

    public Usuario autenticar(String usuario, String clave) throws DatosException {
        Map<String, Object> fila = null;
        for (Map<String, Object> u : BaseDatos.tabla(BaseDatos.USUARIO)) {
            if (usuario != null && usuario.equals(BaseDatos.texto(u, "usuario"))
                    && "ACTIVO".equals(BaseDatos.texto(u, "estado"))) {
                fila = u;
                break;
            }
        }
        if (fila == null) {
            return null;
        }
        String almacenado = BaseDatos.texto(fila, "contrasena");
        if (!Contrasenas.verificar(clave, almacenado)) {
            return null;
        }
        Usuario u = construir(fila);
        u.setContrasena(null);
        if (!Contrasenas.estaCifrada(almacenado)) {
            cambiarContrasena(u.getIdUsuario(), clave);
        }
        return u;
    }

    public boolean verificarContrasena(int idUsuario, String clave) throws DatosException {
        Map<String, Object> fila = BaseDatos.porId(BaseDatos.USUARIO, "id_usuario", idUsuario);
        return fila != null && Contrasenas.verificar(clave, BaseDatos.texto(fila, "contrasena"));
    }

    private Usuario construir(Map<String, Object> fila) throws DatosException {
        Usuario u;
        String rol = BaseDatos.texto(fila, "rol");
        switch (rol == null ? "" : rol) {
            case "ADMINISTRADOR":
                u = new AdministradorGeneral();
                break;
            case "DIRECTIVA":
                u = new Directiva();
                break;
            case "DOCENTE": {
                Docente d = new Docente();
                Map<String, Object> perfil = BaseDatos.porId(BaseDatos.DOCENTE, "id_usuario",
                        BaseDatos.entero(fila, "id_usuario"));
                if (perfil != null) {
                    d.setCargaAcademica(BaseDatos.texto(perfil, "carga_academica"));
                    d.setJornada(BaseDatos.texto(perfil, "jornada"));
                }
                u = d;
                break;
            }
            case "PSICOSOCIAL": {
                Psicosocial p = new Psicosocial();
                Map<String, Object> perfil = BaseDatos.porId(BaseDatos.PSICOSOCIAL, "id_usuario",
                        BaseDatos.entero(fila, "id_usuario"));
                if (perfil != null) {
                    p.setArea(BaseDatos.texto(perfil, "area"));
                    p.setPerfilProfesional(BaseDatos.texto(perfil, "perfil_profesional"));
                    p.setGradosMonitorear(BaseDatos.texto(perfil, "grados_monitorear"));
                }
                u = p;
                break;
            }
            case "ESTUDIANTE": {
                Estudiante e = new Estudiante();
                Map<String, Object> perfil = BaseDatos.porId(BaseDatos.ESTUDIANTE, "id_usuario",
                        BaseDatos.entero(fila, "id_usuario"));
                if (perfil != null) {
                    e.setIdCurso(BaseDatos.entero(perfil, "id_curso"));
                    e.setFechaNacimiento(BaseDatos.fecha(perfil, "fecha_nacimiento"));
                    e.setEdad(BaseDatos.enteroPrimitivo(perfil, "edad"));
                    e.setDireccion(BaseDatos.texto(perfil, "direccion"));
                    e.setBarrio(BaseDatos.texto(perfil, "barrio"));
                    e.setTipoDocumento(BaseDatos.texto(perfil, "tipo_documento"));
                    e.setGrado(BaseDatos.texto(perfil, "grado"));
                    e.setJornada(BaseDatos.texto(perfil, "jornada"));
                }
                u = e;
                break;
            }
            default:
                throw new DatosException("Rol desconocido: " + rol);
        }

        u.setIdUsuario(BaseDatos.enteroPrimitivo(fila, "id_usuario"));
        u.setIdInstitucion(BaseDatos.entero(fila, "id_institucion"));
        u.setNombre(BaseDatos.texto(fila, "nombre"));
        u.setDocumento(BaseDatos.texto(fila, "documento"));
        u.setTelefono(BaseDatos.texto(fila, "telefono"));
        u.setCorreo(BaseDatos.texto(fila, "correo"));
        u.setUsuario(BaseDatos.texto(fila, "usuario"));
        u.setContrasena(BaseDatos.texto(fila, "contrasena"));
        u.setEstado(BaseDatos.texto(fila, "estado"));
        return u;
    }

    public boolean existeUsuario(String usuario) throws DatosException {
        return existe("usuario", usuario, -1);
    }

    public boolean existeDocumento(String documento) throws DatosException {
        return existe("documento", documento, -1);
    }

    public boolean existeDocumentoDeOtro(String documento, int idUsuario) throws DatosException {
        return existe("documento", documento, idUsuario);
    }

    private boolean existe(String columna, String valor, int excluir) throws DatosException {
        for (Map<String, Object> u : BaseDatos.tabla(BaseDatos.USUARIO)) {
            if (valor != null && valor.equals(BaseDatos.texto(u, columna))
                    && (excluir < 0 || BaseDatos.enteroPrimitivo(u, "id_usuario") != excluir)) {
                return true;
            }
        }
        return false;
    }

    public int crearDirectiva(Directiva d) throws DatosException {
        return BaseDatos.enTransaccion(() -> {
            int id = insertarUsuario(d);
            Map<String, Object> fila = BaseDatos.nuevo();
            fila.put("id_usuario", id);
            BaseDatos.insertar(BaseDatos.DIRECTIVA, fila);
            return id;
        });
    }

    public List<Object[]> listarDirectivas() throws DatosException {
        Map<Object, Map<String, Object>> instituciones = BaseDatos.indice(BaseDatos.INSTITUCION, "id_institucion");
        List<Object[]> filas = new ArrayList<>();
        for (Map<String, Object> u : BaseDatos.tabla(BaseDatos.USUARIO)) {
            if (!"DIRECTIVA".equals(BaseDatos.texto(u, "rol"))) {
                continue;
            }
            Map<String, Object> i = instituciones.get(BaseDatos.entero(u, "id_institucion"));
            filas.add(new Object[] {
                    BaseDatos.enteroPrimitivo(u, "id_usuario"),
                    BaseDatos.texto(u, "nombre"),
                    BaseDatos.texto(u, "documento"),
                    BaseDatos.texto(u, "usuario"),
                    i == null ? null : BaseDatos.texto(i, "nombre"),
                    BaseDatos.texto(u, "estado") });
        }
        filas.sort((a, b) -> {
            int c = Ordenes.limpio((String) a[4]).compareToIgnoreCase(Ordenes.limpio((String) b[4]));
            return c != 0 ? c : Ordenes.limpio((String) a[1]).compareToIgnoreCase(Ordenes.limpio((String) b[1]));
        });
        return filas;
    }

    public void cambiarEstado(int idUsuario, String estado) throws DatosException {
        Map<String, Object> fila = BaseDatos.porId(BaseDatos.USUARIO, "id_usuario", idUsuario);
        if (fila != null) {
            fila.put("estado", estado);
            BaseDatos.actualizada(BaseDatos.USUARIO);
        }
    }

    public void cambiarContrasena(int idUsuario, String nueva) throws DatosException {
        Map<String, Object> fila = BaseDatos.porId(BaseDatos.USUARIO, "id_usuario", idUsuario);
        if (fila != null) {
            fila.put("contrasena", Contrasenas.cifrar(nueva));
            BaseDatos.actualizada(BaseDatos.USUARIO);
        }
    }

    public static int insertarUsuario(Usuario u) throws DatosException {
        Map<String, Object> fila = BaseDatos.nuevo();
        int id = BaseDatos.nuevoId(BaseDatos.USUARIO, "id_usuario");
        fila.put("id_usuario", id);
        fila.put("id_institucion", u.getIdInstitucion());
        fila.put("rol", u.getRol());
        fila.put("nombre", u.getNombre());
        fila.put("documento", u.getDocumento());
        fila.put("telefono", u.getTelefono());
        fila.put("correo", u.getCorreo());
        fila.put("usuario", u.getUsuario());
        fila.put("contrasena", Contrasenas.cifrar(u.getContrasena()));
        fila.put("estado", u.getEstado());
        BaseDatos.insertar(BaseDatos.USUARIO, fila);
        return id;
    }

    public static int actualizarDatosBasicos(Usuario u, int idInstitucion) throws DatosException {
        Map<String, Object> fila = BaseDatos.porId(BaseDatos.USUARIO, "id_usuario", u.getIdUsuario());
        if (fila == null || !BaseDatos.igual(BaseDatos.entero(fila, "id_institucion"), idInstitucion)) {
            return 0;
        }
        fila.put("nombre", u.getNombre());
        fila.put("documento", u.getDocumento());
        fila.put("telefono", u.getTelefono());
        fila.put("correo", u.getCorreo());
        BaseDatos.actualizada(BaseDatos.USUARIO);
        return 1;
    }
}
