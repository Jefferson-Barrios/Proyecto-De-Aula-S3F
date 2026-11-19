package vista.estudiante;

import dao.ComentarioDAO;
import dao.ExpedienteDAO;
import eipi.util.DatosException;
import java.util.List;

public class PanelExpediente extends PanelSoloLista {

    public PanelExpediente() {
        iniciar();
    }

    @Override
    protected String titulo() {
        return "Mi expediente";
    }

    @Override
    protected String subtituloInicial() {
        return "Observaciones de tus docentes";
    }

    @Override
    protected String[] columnas() {
        return new String[] { "Fecha", "Docente", "Comentario" };
    }

    @Override
    protected int[] anchos() {
        return new int[] { 100, 220, 560 };
    }

    @Override
    protected List<Object[]> consultar() throws DatosException {
        int id = Contexto.idEstudiante();
        Object[] exp = new ExpedienteDAO().obtener(id);
        List<Object[]> comentarios = new ComentarioDAO().listarDeEstudiante(id);
        mostrarResumen(exp, comentarios.size());
        return comentarios;
    }

    public void mostrarResumen(Object[] expediente, int comentarios) {
        if (expediente == null) {
            lblSubtitulo.setText("Aún no tienes expediente creado.");
        } else {
            lblSubtitulo.setText("Expediente creado el " + expediente[0] + "  ·  Estado: " + expediente[1]
                    + "  ·  " + comentarios + (comentarios == 1 ? " observación" : " observaciones"));
        }
    }

    @Override
    protected String textoVacio() {
        return "Todavía no tienes observaciones en tu expediente.";
    }
}
