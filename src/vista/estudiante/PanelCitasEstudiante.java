package vista.estudiante;

import dao.CitaDAO;
import eipi.util.DatosException;
import java.util.List;

public class PanelCitasEstudiante extends PanelSoloLista {

    public PanelCitasEstudiante() {
        iniciar();
    }

    @Override
    protected String titulo() {
        return "Mis citas";
    }

    @Override
    protected String subtituloInicial() {
        return "Citas con el equipo psicosocial de tu institución";
    }

    @Override
    protected String[] columnas() {
        return new String[] { "Fecha", "Hora", "Profesional", "Motivo", "Estado" };
    }

    @Override
    protected int[] anchos() {
        return new int[] { 100, 70, 230, 380, 110 };
    }

    @Override
    protected List<Object[]> consultar() throws DatosException {
        return new CitaDAO().listarDeEstudiante(Contexto.idEstudiante());
    }

    @Override
    protected String textoVacio() {
        return "No tienes citas registradas.";
    }
}
