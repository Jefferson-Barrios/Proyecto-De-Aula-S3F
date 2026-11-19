package vista.docente;

import dao.ComentarioDAO;
import dao.EstudianteDAO;
import java.awt.BorderLayout;
import java.awt.Component;
import eipi.util.DatosException;
import java.util.List;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import vista.componentes.AreaTexto;
import vista.componentes.Opcion;
import vista.componentes.Ui;
import vista.directiva.PanelGestion;

public class PanelComentarios extends PanelGestion {

    private final ComentarioDAO comentarioDAO = new ComentarioDAO();
    private final EstudianteDAO estudianteDAO = new EstudianteDAO();
    private final JComboBox<Opcion> cbEstudiante = Ui.combo();
    private final AreaTexto txtComentario = new AreaTexto(5);

    public PanelComentarios() {
        iniciar();
    }

    @Override
    protected String titulo() {
        return "Comentarios";
    }

    @Override
    protected String subtitulo() {
        return "Observaciones que quedan en el expediente de cada estudiante";
    }

    @Override
    protected String textoNuevo() {
        return "Nuevo comentario";
    }

    @Override
    protected String tituloFormNuevo() {
        return "Nuevo comentario";
    }

    @Override
    protected boolean tieneEstado() {
        return false;
    }

    @Override
    protected String[] columnas() {
        return new String[]{"Estudiante", "Fecha", "Comentario"};
    }

    @Override
    protected int[] anchos() {
        return new int[]{240, 110, 520};
    }

    @Override
    protected List<Object[]> consultar() throws DatosException {
        return comentarioDAO.listarDeDocente(Selectores.idDocente());
    }

    @Override
    protected Component construirCampos() {
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setOpaque(false);
        p.add(Ui.rejilla(2, Ui.conEtiqueta("Estudiante *", cbEstudiante)), BorderLayout.NORTH);
        JPanel texto = new JPanel(new BorderLayout());
        texto.setOpaque(false);
        texto.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 8, 8, 8));
        texto.add(Ui.conEtiqueta("Comentario *", txtComentario), BorderLayout.CENTER);
        p.add(texto, BorderLayout.CENTER);
        return p;
    }

    @Override
    protected void prepararFormulario(Object[] fila) throws DatosException {
        List<Object[]> estudiantes = estudianteDAO.listarDeDocente(Selectores.idDocente());
        if (estudiantes.isEmpty()) {
            throw new IllegalStateException("Aún no tienes estudiantes a cargo.");
        }
        cbEstudiante.removeAllItems();
        for (Object[] e : estudiantes) {
            cbEstudiante.addItem(new Opcion((Integer) e[0], e[1] + " (" + e[2] + ")", e));
        }
        txtComentario.setTexto("");
    }

    @Override
    protected void guardar(Integer idEdicion) throws DatosException {
        Opcion estudiante = (Opcion) cbEstudiante.getSelectedItem();
        String texto = txtComentario.getTexto().trim();
        if (estudiante == null) {
            throw new IllegalArgumentException("Selecciona el estudiante.");
        }
        if (texto.length() < 5) {
            throw new IllegalArgumentException("Escribe el comentario (mínimo 5 caracteres).");
        }
        comentarioDAO.crear(estudiante.getId(), Selectores.idDocente(), texto);
    }

    @Override
    protected String mensajeGuardado(boolean nuevo) {
        return "Comentario agregado al expediente.";
    }
}
