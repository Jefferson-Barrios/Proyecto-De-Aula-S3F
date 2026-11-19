package vista.directiva;

import dao.CursoDAO;
import java.awt.Component;
import eipi.util.DatosException;
import java.util.List;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import modelo.Curso;
import vista.componentes.Ui;

public class PanelCursos extends PanelGestion {

    private final CursoDAO cursoDAO = new CursoDAO();
    private final JTextField tfGrado = Ui.campo();
    private final JTextField tfGrupo = Ui.campo();
    private final JComboBox<String> cbJornada = Ui.combo();

    public PanelCursos() {
        for (String j : JORNADAS) {
            cbJornada.addItem(j);
        }
        iniciar();
    }

    @Override
    protected String titulo() {
        return "Cursos";
    }

    @Override
    protected String subtitulo() {
        return "Define los grados y grupos de tu institución";
    }

    @Override
    protected String textoNuevo() {
        return "Nuevo curso";
    }

    @Override
    protected String tituloFormNuevo() {
        return "Nuevo curso";
    }

    @Override
    protected String tituloFormEditar() {
        return "Editar curso";
    }

    @Override
    protected boolean tieneEstado() {
        return false;
    }

    @Override
    protected boolean permiteEditar() {
        return true;
    }

    @Override
    protected String[] columnas() {
        return new String[] { "Grado", "Grupo", "Jornada", "Estudiantes" };
    }

    @Override
    protected int[] anchos() {
        return new int[] { 150, 120, 150, 120 };
    }

    @Override
    protected List<Object[]> consultar() throws DatosException {
        return cursoDAO.listarConConteo(idInstitucion());
    }

    @Override
    protected Component construirCampos() {
        return Ui.rejilla(2,
                Ui.conEtiqueta("Grado * (por ejemplo: 6, 7, 11)", tfGrado),
                Ui.conEtiqueta("Grupo * (por ejemplo: A, 01)", tfGrupo),
                Ui.conEtiqueta("Jornada *", cbJornada));
    }

    @Override
    protected void prepararFormulario(Object[] fila) {
        if (fila == null) {
            tfGrado.setText("");
            tfGrupo.setText("");
            cbJornada.setSelectedIndex(0);
        } else {
            tfGrado.setText(String.valueOf(fila[1]));
            tfGrupo.setText(String.valueOf(fila[2]));
            cbJornada.setSelectedItem(fila[3]);
        }
    }

    @Override
    protected void guardar(Integer idEdicion) throws DatosException {
        String grado = tfGrado.getText().trim();
        String grupo = tfGrupo.getText().trim();
        if (grado.isEmpty() || grupo.isEmpty()) {
            throw new IllegalArgumentException("Completa el grado y el grupo.");
        }
        Curso c = new Curso();
        c.setIdInstitucion(idInstitucion());
        c.setGrado(grado);
        c.setGrupo(grupo);
        c.setJornada((String) cbJornada.getSelectedItem());
        if (idEdicion == null) {
            cursoDAO.insertar(c);
        } else {
            c.setIdCurso(idEdicion);
            cursoDAO.actualizar(c);
        }
    }
}
