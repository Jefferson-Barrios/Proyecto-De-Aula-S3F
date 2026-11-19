package dao;

import eipi.util.BaseDatos;
import eipi.util.DatosException;
import eipi.util.Formato;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ComentarioDAO {

    public void crear(int idEstudiante, int idDocente, String contenido) throws DatosException {
        Map<String, Object> expediente = BaseDatos.porId(BaseDatos.EXPEDIENTE, "id_estudiante", idEstudiante);
        if (expediente == null) {
            throw new IllegalStateException("El estudiante no tiene expediente.");
        }
        Map<String, Object> fila = BaseDatos.nuevo();
        fila.put("id_comentario", BaseDatos.nuevoId(BaseDatos.COMENTARIO, "id_comentario"));
        fila.put("id_expediente", BaseDatos.entero(expediente, "id_expediente"));
        fila.put("id_docente", idDocente);
        fila.put("contenido", contenido);
        fila.put("fecha", LocalDate.now().toString());
        BaseDatos.insertar(BaseDatos.COMENTARIO, fila);
    }

    public List<Object[]> listarDeDocente(int idDocente) throws DatosException {
        List<Object[]> filas = new ArrayList<>();
        for (Map<String, Object> c : BaseDatos.tabla(BaseDatos.COMENTARIO)) {
            if (!BaseDatos.igual(BaseDatos.entero(c, "id_docente"), idDocente)) {
                continue;
            }
            Map<String, Object> x = BaseDatos.porId(BaseDatos.EXPEDIENTE, "id_expediente",
                    BaseDatos.entero(c, "id_expediente"));
            Map<String, Object> u = x == null ? null
                    : BaseDatos.porId(BaseDatos.USUARIO, "id_usuario",
                            BaseDatos.entero(x, "id_estudiante"));
            filas.add(new Object[] {
                    BaseDatos.enteroPrimitivo(c, "id_comentario"),
                    u == null ? null : BaseDatos.texto(u, "nombre"),
                    Formato.texto(BaseDatos.fecha(c, "fecha")),
                    BaseDatos.texto(c, "contenido"),
                    Ordenes.limpio(BaseDatos.texto(c, "fecha")),
                    BaseDatos.enteroPrimitivo(c, "id_comentario") });
        }
        filas.sort((a, b) -> {
            int c = ((String) b[4]).compareTo((String) a[4]);
            return c != 0 ? c : Integer.compare((Integer) b[5], (Integer) a[5]);
        });
        return recortar(filas);
    }

    public List<Object[]> listarDeEstudiante(int idEstudiante) throws DatosException {
        Map<String, Object> expediente = BaseDatos.porId(BaseDatos.EXPEDIENTE, "id_estudiante", idEstudiante);
        List<Object[]> filas = new ArrayList<>();
        if (expediente == null) {
            return filas;
        }
        Integer idExpediente = BaseDatos.entero(expediente, "id_expediente");
        for (Map<String, Object> c : BaseDatos.tabla(BaseDatos.COMENTARIO)) {
            if (!BaseDatos.igual(BaseDatos.entero(c, "id_expediente"), idExpediente)) {
                continue;
            }
            Map<String, Object> d = BaseDatos.porId(BaseDatos.USUARIO, "id_usuario",
                    BaseDatos.entero(c, "id_docente"));
            filas.add(new Object[] {
                    Formato.texto(BaseDatos.fecha(c, "fecha")),
                    d == null ? null : BaseDatos.texto(d, "nombre"),
                    BaseDatos.texto(c, "contenido"),
                    Ordenes.limpio(BaseDatos.texto(c, "fecha")),
                    BaseDatos.enteroPrimitivo(c, "id_comentario") });
        }
        filas.sort((a, b) -> {
            int c = ((String) b[3]).compareTo((String) a[3]);
            return c != 0 ? c : Integer.compare((Integer) b[4], (Integer) a[4]);
        });
        return recortar(filas);
    }

    private static List<Object[]> recortar(List<Object[]> filas) {
        List<Object[]> resultado = new ArrayList<>(filas.size());
        for (Object[] f : filas) {
            Object[] nueva = new Object[f.length - 2];
            System.arraycopy(f, 0, nueva, 0, nueva.length);
            resultado.add(nueva);
        }
        return resultado;
    }
}
