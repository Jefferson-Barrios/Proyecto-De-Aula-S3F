package dao;

import eipi.util.BaseDatos;
import eipi.util.DatosException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import modelo.Curso;

public class CursoDAO {

    public List<Curso> listar(int idInstitucion) throws DatosException {
        List<Curso> lista = new ArrayList<>();
        for (Map<String, Object> c : BaseDatos.tabla(BaseDatos.CURSO)) {
            if (!BaseDatos.igual(BaseDatos.entero(c, "id_institucion"), idInstitucion)) {
                continue;
            }
            Curso curso = new Curso();
            curso.setIdCurso(BaseDatos.enteroPrimitivo(c, "id_curso"));
            curso.setIdInstitucion(BaseDatos.enteroPrimitivo(c, "id_institucion"));
            curso.setGrado(BaseDatos.texto(c, "grado"));
            curso.setGrupo(BaseDatos.texto(c, "grupo"));
            curso.setJornada(BaseDatos.texto(c, "jornada"));
            lista.add(curso);
        }
        lista.sort((a, b) -> ORDEN.compare(orden(a.getGrado(), a.getGrupo()), orden(b.getGrado(), b.getGrupo())));
        return lista;
    }

    public List<Object[]> listarConConteo(int idInstitucion) throws DatosException {
        List<Object[]> filas = new ArrayList<>();
        for (Map<String, Object> c : BaseDatos.tabla(BaseDatos.CURSO)) {
            if (!BaseDatos.igual(BaseDatos.entero(c, "id_institucion"), idInstitucion)) {
                continue;
            }
            int estudiantes = 0;
            for (Map<String, Object> e : BaseDatos.tabla(BaseDatos.ESTUDIANTE)) {
                if (BaseDatos.igual(BaseDatos.entero(e, "id_curso"), BaseDatos.enteroPrimitivo(c, "id_curso"))) {
                    estudiantes++;
                }
            }
            filas.add(new Object[] {
                    BaseDatos.enteroPrimitivo(c, "id_curso"),
                    BaseDatos.texto(c, "grado"),
                    BaseDatos.texto(c, "grupo"),
                    BaseDatos.texto(c, "jornada"),
                    estudiantes,
                    orden(BaseDatos.texto(c, "grado"), BaseDatos.texto(c, "grupo")) });
        }
        filas.sort((a, b) -> ORDEN.compare((Object[]) a[5], (Object[]) b[5]));
        List<Object[]> resultado = new ArrayList<>(filas.size());
        for (Object[] f : filas) {
            resultado.add(new Object[] { f[0], f[1], f[2], f[3], f[4] });
        }
        return resultado;
    }

    public int insertar(Curso c) throws DatosException {
        validarDuplicado(c, null);
        Map<String, Object> fila = BaseDatos.nuevo();
        int id = BaseDatos.nuevoId(BaseDatos.CURSO, "id_curso");
        fila.put("id_curso", id);
        fila.put("id_institucion", c.getIdInstitucion());
        fila.put("grado", c.getGrado());
        fila.put("grupo", c.getGrupo());
        fila.put("jornada", c.getJornada());
        BaseDatos.insertar(BaseDatos.CURSO, fila);
        return id;
    }

    public void actualizar(Curso c) throws DatosException {
        validarDuplicado(c, c.getIdCurso());
        BaseDatos.enTransaccion(() -> {
            Map<String, Object> fila = BaseDatos.porId(BaseDatos.CURSO, "id_curso", c.getIdCurso());
            if (fila != null) {
                fila.put("grado", c.getGrado());
                fila.put("grupo", c.getGrupo());
                fila.put("jornada", c.getJornada());
                BaseDatos.marcarSucia(BaseDatos.CURSO);
            }
            for (Map<String, Object> e : BaseDatos.tabla(BaseDatos.ESTUDIANTE)) {
                if (BaseDatos.igual(BaseDatos.entero(e, "id_curso"), c.getIdCurso())) {
                    e.put("grado", c.getGrado());
                    e.put("jornada", c.getJornada());
                    BaseDatos.marcarSucia(BaseDatos.ESTUDIANTE);
                }
            }
            return null;
        });
    }

    private static final java.util.Comparator<Object[]> ORDEN = new java.util.Comparator<>() {
        @Override
        public int compare(Object[] a, Object[] b) {
            int g = Integer.compare(Ordenes.grado((String) a[0]), Ordenes.grado((String) b[0]));
            if (g != 0) {
                return g;
            }
            g = Ordenes.limpio((String) a[0]).compareTo(Ordenes.limpio((String) b[0]));
            return g != 0 ? g : Ordenes.limpio((String) a[1]).compareTo(Ordenes.limpio((String) b[1]));
        }
    };

    private static Object[] orden(String grado, String grupo) {
        return new Object[] { grado, grupo };
    }

    private void validarDuplicado(Curso c, Integer excluirId) throws DatosException {
        for (Map<String, Object> fila : BaseDatos.tabla(BaseDatos.CURSO)) {
            if (excluirId != null && BaseDatos.igual(BaseDatos.entero(fila, "id_curso"), excluirId)) {
                continue;
            }
            if (BaseDatos.igual(BaseDatos.entero(fila, "id_institucion"), c.getIdInstitucion())
                    && BaseDatos.igual(BaseDatos.texto(fila, "grado"), c.getGrado())
                    && BaseDatos.igual(BaseDatos.texto(fila, "grupo"), c.getGrupo())
                    && BaseDatos.igual(BaseDatos.texto(fila, "jornada"), c.getJornada())) {
                throw new DatosException("Ya existe ese curso (mismo grado, grupo y jornada).");
            }
        }
    }
}
