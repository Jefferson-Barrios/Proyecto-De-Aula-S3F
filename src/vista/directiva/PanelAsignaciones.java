package vista.directiva;

import dao.AsignacionDAO;
import dao.CursoDAO;
import dao.DocenteDAO;
import dao.MateriaDAO;
import java.awt.Component;
import eipi.util.DatosException;
import java.util.List;
import javax.swing.JComboBox;
import modelo.Curso;
import modelo.Docente;
import modelo.Materia;
import vista.componentes.Opcion;
import vista.componentes.Ui;

public class PanelAsignaciones extends PanelGestion {

    private final AsignacionDAO asignacionDAO = new AsignacionDAO();
    private final DocenteDAO docenteDAO = new DocenteDAO();
    private final CursoDAO cursoDAO = new CursoDAO();
    private final MateriaDAO materiaDAO = new MateriaDAO();
    private final JComboBox<Opcion> cbDocente = Ui.combo();
    private final JComboBox<Opcion> cbCurso = Ui.combo();
    private final JComboBox<Opcion> cbMateria = Ui.combo();
    private final JComboBox<String> cbJornada = Ui.combo();

    public PanelAsignaciones() {
        for (String j : JORNADAS) {
            cbJornada.addItem(j);
        }

        cbCurso.addActionListener(e -> {
            Opcion o = (Opcion) cbCurso.getSelectedItem();
            if (o != null && o.getDato() instanceof Curso) {
                cbJornada.setSelectedItem(((Curso) o.getDato()).getJornada());
            }
        });
        iniciar();
    }

    @Override
    protected String titulo() {
        return "Asignaciones";
    }

    @Override
    protected String subtitulo() {
        return "Define qué docente dicta cada materia en cada curso";
    }

    @Override
    protected String textoNuevo() {
        return "Nueva asignación";
    }

    @Override
    protected String tituloFormNuevo() {
        return "Nueva asignación";
    }

    @Override
    protected String[] columnas() {
        return new String[] { "Docente", "Curso", "Materia", "Jornada", "Estado" };
    }

    @Override
    protected int[] anchos() {
        return new int[] { 260, 90, 220, 110, 90 };
    }

    @Override
    protected List<Object[]> consultar() throws DatosException {
        return asignacionDAO.listar(idInstitucion());
    }

    @Override
    protected Component construirCampos() {
        return Ui.rejilla(2,
                Ui.conEtiqueta("Docente *", cbDocente),
                Ui.conEtiqueta("Curso *", cbCurso),
                Ui.conEtiqueta("Materia *", cbMateria),
                Ui.conEtiqueta("Jornada *", cbJornada));
    }

    @Override
    protected void prepararFormulario(Object[] fila) throws DatosException {
        int inst = idInstitucion();
        List<Docente> docentes = docenteDAO.listarActivos(inst);
        List<Curso> cursos = cursoDAO.listar(inst);
        List<Materia> materias = materiaDAO.listar(inst);
        if (docentes.isEmpty()) {
            throw new IllegalStateException("Primero registra al menos un docente activo.");
        }
        if (cursos.isEmpty()) {
            throw new IllegalStateException("Primero crea al menos un curso en la sección Cursos.");
        }
        if (materias.isEmpty()) {
            throw new IllegalStateException("Primero crea al menos una materia en la sección Materias.");
        }
        cbDocente.removeAllItems();
        for (Docente d : docentes) {
            cbDocente.addItem(new Opcion(d.getIdUsuario(), d.getNombre(), d));
        }
        cbMateria.removeAllItems();
        for (Materia m : materias) {
            cbMateria.addItem(new Opcion(m.getIdMateria(), m.getNombre(), m));
        }
        cbCurso.removeAllItems();
        for (Curso c : cursos) {
            cbCurso.addItem(new Opcion(c.getIdCurso(), c.getGrado() + "-" + c.getGrupo()
                    + " (" + c.getJornada() + ")", c));
        }
    }

    @Override
    protected void guardar(Integer idEdicion) throws DatosException {
        Opcion docente = (Opcion) cbDocente.getSelectedItem();
        Opcion curso = (Opcion) cbCurso.getSelectedItem();
        Opcion materia = (Opcion) cbMateria.getSelectedItem();
        if (docente == null || curso == null || materia == null) {
            throw new IllegalArgumentException("Selecciona el docente, el curso y la materia.");
        }
        asignacionDAO.insertar(docente.getId(), curso.getId(), materia.getId(),
                (String) cbJornada.getSelectedItem());
    }

    @Override
    protected String mensajeGuardado(boolean nuevo) {
        return "Asignación registrada correctamente.";
    }

    @Override
    protected void cambiarEstado(int id, String nuevoEstado) throws DatosException {
        asignacionDAO.cambiarEstado(id, nuevoEstado);
    }
}
