package vista.directiva;

import dao.MateriaDAO;
import java.awt.Component;
import eipi.util.DatosException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JTextField;
import modelo.Materia;
import vista.componentes.Ui;

public class PanelMaterias extends PanelGestion {

    private final MateriaDAO materiaDAO = new MateriaDAO();
    private final JTextField tfNombre = Ui.campo();
    private final JTextField tfDescripcion = Ui.campo();

    public PanelMaterias() {
        iniciar();
    }

    @Override
    protected String titulo() {
        return "Materias";
    }

    @Override
    protected String subtitulo() {
        return "Registra las materias que se dictan en tu institución";
    }

    @Override
    protected String textoNuevo() {
        return "Nueva materia";
    }

    @Override
    protected String tituloFormNuevo() {
        return "Nueva materia";
    }

    @Override
    protected String tituloFormEditar() {
        return "Editar materia";
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
        return new String[] { "Nombre", "Descripción" };
    }

    @Override
    protected int[] anchos() {
        return new int[] { 250, 450 };
    }

    @Override
    protected List<Object[]> consultar() throws DatosException {
        List<Object[]> filas = new ArrayList<>();
        for (Materia m : materiaDAO.listar(idInstitucion())) {
            filas.add(new Object[] { m.getIdMateria(), m.getNombre(), m.getDescripcion() });
        }
        return filas;
    }

    @Override
    protected Component construirCampos() {
        return Ui.rejilla(2,
                Ui.conEtiqueta("Nombre de la materia *", tfNombre),
                Ui.conEtiqueta("Descripción", tfDescripcion));
    }

    @Override
    protected void prepararFormulario(Object[] fila) {
        tfNombre.setText(fila == null ? "" : String.valueOf(fila[1]));
        tfDescripcion.setText(fila == null || fila[2] == null ? "" : String.valueOf(fila[2]));
    }

    @Override
    protected void guardar(Integer idEdicion) throws DatosException {
        String nombre = tfNombre.getText().trim();
        if (nombre.isEmpty()) {
            throw new IllegalArgumentException("Escribe el nombre de la materia.");
        }
        Materia m = new Materia();
        m.setIdInstitucion(idInstitucion());
        m.setNombre(nombre);
        m.setDescripcion(nulo(tfDescripcion.getText()));
        if (idEdicion == null) {
            materiaDAO.insertar(m);
        } else {
            m.setIdMateria(idEdicion);
            materiaDAO.actualizar(m);
        }
    }
}
