package dao;

import eipi.util.BaseDatos;
import eipi.util.DatosException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import modelo.Acudiente;
import modelo.Estudiante;

public class EstudianteDAO {

    private static final Comparator<Object[]> ORDEN = Comparator
            .comparingInt((Object[] f) -> Ordenes.grado((String) f[3]))
            .thenComparing(f -> Ordenes.limpio((String) f[3]))
            .thenComparing(f -> Ordenes.limpio((String) f[4]))
            .thenComparing(f -> Ordenes.limpio((String) f[5]).toLowerCase());

    public int crear(Estudiante e, Acudiente a) throws DatosException {
        return BaseDatos.enTransaccion(() -> {
            int id = UsuarioDAO.insertarUsuario(e);

            Map<String, Object> estudiante = BaseDatos.nuevo();
            estudiante.put("id_usuario", id);
            estudiante.put("id_curso", e.getIdCurso());
            estudiante.put("fecha_nacimiento",
                    e.getFechaNacimiento() == null ? null : e.getFechaNacimiento().toString());
            estudiante.put("edad", e.getEdad());
            estudiante.put("direccion", e.getDireccion());
            estudiante.put("barrio", e.getBarrio());
            estudiante.put("tipo_documento", e.getTipoDocumento());
            estudiante.put("grado", e.getGrado());
            estudiante.put("jornada", e.getJornada());
            BaseDatos.insertar(BaseDatos.ESTUDIANTE, estudiante);

            Map<String, Object> acudiente = BaseDatos.nuevo();
            acudiente.put("id_acudiente", BaseDatos.nuevoId(BaseDatos.ACUDIENTE, "id_acudiente"));
            acudiente.put("id_estudiante", id);
            acudiente.put("nombre", a.getNombre());
            acudiente.put("correo", a.getCorreo());
            acudiente.put("telefono", a.getTelefono());
            BaseDatos.insertar(BaseDatos.ACUDIENTE, acudiente);

            Map<String, Object> expediente = BaseDatos.nuevo();
            expediente.put("id_expediente", BaseDatos.nuevoId(BaseDatos.EXPEDIENTE, "id_expediente"));
            expediente.put("id_estudiante", id);
            expediente.put("fecha_creacion", LocalDate.now().toString());
            expediente.put("estado", "ACTIVO");
            BaseDatos.insertar(BaseDatos.EXPEDIENTE, expediente);

            return id;
        });
    }

    public List<Object[]> listar(int idInstitucion) throws DatosException {
        Map<Object, Map<String, Object>> cursos = BaseDatos.indice(BaseDatos.CURSO, "id_curso");
        Map<Object, Map<String, Object>> acudientes = BaseDatos.indice(BaseDatos.ACUDIENTE, "id_estudiante");
        List<Object[]> filas = new ArrayList<>();
        for (Map<String, Object> u : BaseDatos.tabla(BaseDatos.USUARIO)) {
            if (!BaseDatos.igual(BaseDatos.entero(u, "id_institucion"), idInstitucion)) {
                continue;
            }
            Integer idUsuario = BaseDatos.entero(u, "id_usuario");
            Map<String, Object> e = BaseDatos.porId(BaseDatos.ESTUDIANTE, "id_usuario", idUsuario);
            if (e == null) {
                continue;
            }
            Map<String, Object> c = cursos.get(BaseDatos.entero(e, "id_curso"));
            Map<String, Object> ac = acudientes.get(idUsuario);
            String grado = c == null ? null : BaseDatos.texto(c, "grado");
            String grupo = c == null ? null : BaseDatos.texto(c, "grupo");
            String curso = c == null ? null : Ordenes.limpio(grado) + "-" + Ordenes.limpio(grupo);
            filas.add(new Object[] {
                    idUsuario,
                    BaseDatos.texto(u, "nombre"),
                    BaseDatos.texto(u, "documento"),
                    curso,
                    ac == null ? null : BaseDatos.texto(ac, "nombre"),
                    ac == null ? null : BaseDatos.texto(ac, "telefono"),
                    BaseDatos.texto(u, "estado"),
                    grado,
                    grupo,
                    BaseDatos.texto(u, "nombre") });
        }
        filas.sort(ORDEN);
        return recortar(filas, 3);
    }

    public List<Object[]> listarDeDocente(int idDocente) throws DatosException {
        Map<Object, Map<String, Object>> cursos = BaseDatos.indice(BaseDatos.CURSO, "id_curso");
        Map<Object, Map<String, Object>> usuarios = BaseDatos.indice(BaseDatos.USUARIO, "id_usuario");
        Set<Integer> cursosDelDocente = new LinkedHashSet<>();
        for (Map<String, Object> a : BaseDatos.tabla(BaseDatos.ASIGNACION_DOCENTE)) {
            if (BaseDatos.igual(BaseDatos.entero(a, "id_docente"), idDocente)
                    && "ACTIVO".equals(BaseDatos.texto(a, "estado"))) {
                cursosDelDocente.add(BaseDatos.enteroPrimitivo(a, "id_curso"));
            }
        }
        List<Object[]> filas = new ArrayList<>();
        Set<Integer> vistos = new LinkedHashSet<>();
        for (Map<String, Object> e : BaseDatos.tabla(BaseDatos.ESTUDIANTE)) {
            Integer idCurso = BaseDatos.entero(e, "id_curso");
            if (idCurso == null || !cursosDelDocente.contains(idCurso)) {
                continue;
            }
            Integer idUsuario = BaseDatos.entero(e, "id_usuario");
            if (!vistos.add(idUsuario)) {
                continue;
            }
            Map<String, Object> u = usuarios.get(idUsuario);
            if (u == null || !"ACTIVO".equals(BaseDatos.texto(u, "estado"))) {
                continue;
            }
            Map<String, Object> c = cursos.get(idCurso);
            String grado = c == null ? null : BaseDatos.texto(c, "grado");
            String grupo = c == null ? null : BaseDatos.texto(c, "grupo");
            String curso = Ordenes.limpio(grado) + "-" + Ordenes.limpio(grupo);
            filas.add(new Object[] {
                    idUsuario, BaseDatos.texto(u, "nombre"), curso, grado, grupo, BaseDatos.texto(u, "nombre") });
        }
        filas.sort(ORDEN);
        return recortar(filas, 3);
    }

    public List<Object[]> listarActivosDeInstitucion(int idInstitucion) throws DatosException {
        Map<Object, Map<String, Object>> cursos = BaseDatos.indice(BaseDatos.CURSO, "id_curso");
        List<Object[]> filas = new ArrayList<>();
        for (Map<String, Object> u : BaseDatos.tabla(BaseDatos.USUARIO)) {
            if (!BaseDatos.igual(BaseDatos.entero(u, "id_institucion"), idInstitucion)
                    || !"ACTIVO".equals(BaseDatos.texto(u, "estado"))) {
                continue;
            }
            Integer idUsuario = BaseDatos.entero(u, "id_usuario");
            Map<String, Object> e = BaseDatos.porId(BaseDatos.ESTUDIANTE, "id_usuario", idUsuario);
            if (e == null) {
                continue;
            }
            Map<String, Object> c = cursos.get(BaseDatos.entero(e, "id_curso"));
            String grado = c == null ? null : BaseDatos.texto(c, "grado");
            String grupo = c == null ? null : BaseDatos.texto(c, "grupo");
            String curso = c == null ? null : Ordenes.limpio(grado) + "-" + Ordenes.limpio(grupo);
            filas.add(new Object[] {
                    idUsuario, BaseDatos.texto(u, "nombre"), curso, grado, grupo, BaseDatos.texto(u, "nombre") });
        }
        filas.sort(ORDEN);
        return recortar(filas, 3);
    }

    public Object[] obtenerCurso(int idEstudiante) throws DatosException {
        Map<String, Object> e = BaseDatos.porId(BaseDatos.ESTUDIANTE, "id_usuario", idEstudiante);
        if (e == null) {
            return new Object[] { null, null };
        }
        Map<String, Object> c = BaseDatos.porId(BaseDatos.CURSO, "id_curso", BaseDatos.entero(e, "id_curso"));
        if (c == null) {
            return new Object[] { null, BaseDatos.texto(e, "jornada") };
        }
        return new Object[] {
                Ordenes.limpio(BaseDatos.texto(c, "grado")) + "-" + Ordenes.limpio(BaseDatos.texto(c, "grupo")),
                BaseDatos.texto(e, "jornada") };
    }

    public Object[] obtener(int idUsuario, int idInstitucion) throws DatosException {
        Map<String, Object> u = BaseDatos.porId(BaseDatos.USUARIO, "id_usuario", idUsuario);
        if (u == null || !BaseDatos.igual(BaseDatos.entero(u, "id_institucion"), idInstitucion)) {
            return null;
        }
        Map<String, Object> e = BaseDatos.porId(BaseDatos.ESTUDIANTE, "id_usuario", idUsuario);
        if (e == null) {
            return null;
        }
        Map<String, Object> ac = BaseDatos.porId(BaseDatos.ACUDIENTE, "id_estudiante", idUsuario);

        Estudiante est = new Estudiante();
        est.setIdUsuario(BaseDatos.enteroPrimitivo(u, "id_usuario"));
        est.setNombre(BaseDatos.texto(u, "nombre"));
        est.setDocumento(BaseDatos.texto(u, "documento"));
        est.setTelefono(BaseDatos.texto(u, "telefono"));
        est.setCorreo(BaseDatos.texto(u, "correo"));
        est.setIdCurso(BaseDatos.entero(e, "id_curso"));
        est.setFechaNacimiento(BaseDatos.fecha(e, "fecha_nacimiento"));
        est.setDireccion(BaseDatos.texto(e, "direccion"));
        est.setBarrio(BaseDatos.texto(e, "barrio"));
        est.setTipoDocumento(BaseDatos.texto(e, "tipo_documento"));

        Acudiente a = new Acudiente();
        a.setNombre(ac == null ? null : BaseDatos.texto(ac, "nombre"));
        a.setCorreo(ac == null ? null : BaseDatos.texto(ac, "correo"));
        a.setTelefono(ac == null ? null : BaseDatos.texto(ac, "telefono"));
        return new Object[] { est, a };
    }

    public void actualizar(Estudiante e, Acudiente a, int idInstitucion) throws DatosException {
        BaseDatos.enTransaccion(() -> {
            if (UsuarioDAO.actualizarDatosBasicos(e, idInstitucion) == 0) {
                throw new IllegalStateException("No se encontró el estudiante en tu institución.");
            }
            Map<String, Object> fila = BaseDatos.porId(BaseDatos.ESTUDIANTE, "id_usuario", e.getIdUsuario());
            if (fila != null) {
                fila.put("id_curso", e.getIdCurso());
                fila.put("fecha_nacimiento", e.getFechaNacimiento() == null ? null : e.getFechaNacimiento().toString());
                fila.put("edad", e.getEdad());
                fila.put("direccion", e.getDireccion());
                fila.put("barrio", e.getBarrio());
                fila.put("tipo_documento", e.getTipoDocumento());
                fila.put("grado", e.getGrado());
                fila.put("jornada", e.getJornada());
                BaseDatos.marcarSucia(BaseDatos.ESTUDIANTE);
            }
            Map<String, Object> ac = BaseDatos.porId(BaseDatos.ACUDIENTE, "id_estudiante", e.getIdUsuario());
            if (ac == null) {
                Map<String, Object> nuevo = BaseDatos.nuevo();
                nuevo.put("id_acudiente", BaseDatos.nuevoId(BaseDatos.ACUDIENTE, "id_acudiente"));
                nuevo.put("id_estudiante", e.getIdUsuario());
                nuevo.put("nombre", a.getNombre());
                nuevo.put("correo", a.getCorreo());
                nuevo.put("telefono", a.getTelefono());
                BaseDatos.insertar(BaseDatos.ACUDIENTE, nuevo);
            } else {
                ac.put("nombre", a.getNombre());
                ac.put("correo", a.getCorreo());
                ac.put("telefono", a.getTelefono());
                BaseDatos.marcarSucia(BaseDatos.ACUDIENTE);
            }
            return null;
        });
    }

    private static List<Object[]> recortar(List<Object[]> filas, int ocultas) {
        List<Object[]> resultado = new ArrayList<>(filas.size());
        for (Object[] f : filas) {
            Object[] nueva = new Object[f.length - ocultas];
            System.arraycopy(f, 0, nueva, 0, nueva.length);
            resultado.add(nueva);
        }
        return resultado;
    }
}
