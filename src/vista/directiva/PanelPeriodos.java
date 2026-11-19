package vista.directiva;

import dao.CierrePeriodoDAO;
import dao.PeriodoDAO;
import eipi.util.Formato;
import java.awt.Component;
import eipi.util.DatosException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JTextField;
import modelo.PeriodoAcademico;
import vista.componentes.Ui;

public class PanelPeriodos extends PanelGestion {

    private final PeriodoDAO periodoDAO = new PeriodoDAO();
    private final JTextField tfNombre = Ui.campo();
    private final JTextField tfInicio = Ui.campo();
    private final JTextField tfFin = Ui.campo();
    private String resumenCierre = "Período cerrado.";

    public PanelPeriodos() {
        iniciar();
    }

    @Override
    protected String titulo() {
        return "Períodos académicos";
    }

    @Override
    protected String subtitulo() {
        return "Los docentes solo registran notas en períodos abiertos. Al cerrar un período, el sistema genera las alertas de deserción";
    }

    @Override
    protected String textoNuevo() {
        return "Nuevo período";
    }

    @Override
    protected String tituloFormNuevo() {
        return "Nuevo período";
    }

    @Override
    protected String tituloFormEditar() {
        return "Editar período";
    }

    @Override
    protected boolean permiteEditar() {
        return true;
    }

    @Override
    protected String estadoActivo() {
        return "ABIERTO";
    }

    @Override
    protected String estadoInactivo() {
        return "CERRADO";
    }

    @Override
    protected String textoActivar() {
        return "Abrir período";
    }

    @Override
    protected String textoDesactivar() {
        return "Cerrar período";
    }

    @Override
    protected String mensajeEstado(boolean activo) {
        return activo
                ? "Período abierto. Las alertas ya generadas se conservan; al cerrarlo de nuevo se actualizan las que sigan pendientes."
                : resumenCierre;
    }

    @Override
    protected String confirmacionEstado(boolean activar) {
        return activar ? null : "¿Cerrar y generar alertas? Clic para confirmar";
    }

    @Override
    protected String[] columnas() {
        return new String[] { "Nombre", "Inicio", "Fin", "Estado" };
    }

    @Override
    protected int[] anchos() {
        return new int[] { 240, 140, 140, 120 };
    }

    @Override
    protected List<Object[]> consultar() throws DatosException {
        List<Object[]> filas = new ArrayList<>();
        for (PeriodoAcademico p : periodoDAO.listar(idInstitucion())) {
            filas.add(new Object[] { p.getIdPeriodo(), p.getNombre(), Formato.texto(p.getFechaInicio()),
                    Formato.texto(p.getFechaFin()), p.getEstado() });
        }
        return filas;
    }

    @Override
    protected Component construirCampos() {
        return Ui.rejilla(2,
                Ui.conEtiqueta("Nombre * (por ejemplo: Período 1)", tfNombre),
                Ui.conEtiqueta("Fecha de inicio * (dd/mm/aaaa)", tfInicio),
                Ui.conEtiqueta("Fecha de fin * (dd/mm/aaaa)", tfFin));
    }

    @Override
    protected void prepararFormulario(Object[] fila) {
        tfNombre.setText(fila == null ? "" : String.valueOf(fila[1]));
        tfInicio.setText(fila == null ? "" : String.valueOf(fila[2]));
        tfFin.setText(fila == null ? "" : String.valueOf(fila[3]));
    }

    private static LocalDate fecha(String texto, String campo) {
        try {
            return Formato.parsear(texto);
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException(
                    "La " + campo + " debe tener el formato dd/mm/aaaa (por ejemplo 20/01/2026).");
        }
    }

    @Override
    protected void guardar(Integer idEdicion) throws DatosException {
        String nombre = tfNombre.getText().trim();
        if (nombre.isEmpty()) {
            throw new IllegalArgumentException("Escribe el nombre del período.");
        }
        LocalDate inicio = fecha(tfInicio.getText(), "fecha de inicio");
        LocalDate fin = fecha(tfFin.getText(), "fecha de fin");
        if (!fin.isAfter(inicio)) {
            throw new IllegalArgumentException("La fecha de fin debe ser posterior a la de inicio.");
        }
        PeriodoAcademico p = new PeriodoAcademico();
        p.setIdInstitucion(idInstitucion());
        p.setNombre(nombre);
        p.setFechaInicio(inicio);
        p.setFechaFin(fin);
        if (idEdicion == null) {
            p.setEstado("ABIERTO");
            periodoDAO.insertar(p);
        } else {
            p.setIdPeriodo(idEdicion);
            periodoDAO.actualizar(p);
        }
    }

    @Override
    protected void cambiarEstado(int id, String nuevoEstado) throws DatosException {
        if ("CERRADO".equals(nuevoEstado)) {
            PeriodoAcademico periodo = null;
            for (PeriodoAcademico p : periodoDAO.listar(idInstitucion())) {
                if (p.getIdPeriodo() == id) {
                    periodo = p;
                }
            }
            if (periodo == null) {
                throw new IllegalStateException("No se encontró el período en tu institución.");
            }
            resumenCierre = new CierrePeriodoDAO().cerrar(idInstitucion(), periodo).resumen();
        } else {
            periodoDAO.cambiarEstado(id, nuevoEstado);
        }
    }
}
