package vista.psicosocial;

import dao.CitaDAO;
import dao.EstudianteDAO;
import eipi.util.Formato;
import java.awt.Component;
import eipi.util.DatosException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import modelo.CitaPsicosocial;
import vista.componentes.BotonSimple;
import vista.componentes.Opcion;
import vista.componentes.Ui;
import vista.directiva.PanelGestion;

public class PanelCitas extends PanelGestion {

    private final CitaDAO citaDAO = new CitaDAO();
    private final EstudianteDAO estudianteDAO = new EstudianteDAO();
    private final JComboBox<Opcion> cbEstudiante = Ui.combo();
    private final JTextField tfFecha = Ui.campo();
    private final JTextField tfHora = Ui.campo();
    private final JTextField tfMotivo = Ui.campo();
    private final BotonSimple btnRealizada = new BotonSimple("Marcar realizada", BotonSimple.Tipo.SECUNDARIO);
    private final BotonSimple btnCancelar = new BotonSimple("Cancelar cita", BotonSimple.Tipo.PELIGRO);

    public PanelCitas() {
        btnRealizada.addActionListener(e -> cambiar("REALIZADA", "Cita marcada como realizada."));
        btnCancelar.addActionListener(e -> cambiar("CANCELADA", "Cita cancelada."));
        iniciar();
    }

    @Override
    protected String titulo() {
        return "Citas";
    }

    @Override
    protected String subtitulo() {
        return "Tus citas con estudiantes; las agendadas aparecen primero";
    }

    @Override
    protected String textoNuevo() {
        return "Nueva cita";
    }

    @Override
    protected String tituloFormNuevo() {
        return "Nueva cita";
    }

    @Override
    protected boolean tieneEstado() {
        return false;
    }

    @Override
    protected List<Component> botonesExtra() {
        List<Component> l = new ArrayList<>();
        l.add(btnRealizada);
        l.add(btnCancelar);
        return l;
    }

    @Override
    protected void alSeleccionar(Object[] fila) {
        boolean agendada = fila != null && "AGENDADA".equals(fila[fila.length - 1]);
        btnRealizada.setEnabled(agendada);
        btnCancelar.setEnabled(agendada);
    }

    @Override
    protected String[] columnas() {
        return new String[]{"Fecha", "Hora", "Estudiante", "Curso", "Motivo", "Estado"};
    }

    @Override
    protected int[] anchos() {
        return new int[]{100, 70, 230, 70, 330, 110};
    }

    @Override
    protected List<Object[]> consultar() throws DatosException {
        return citaDAO.listarDePsicosocial(Contexto.idPsicosocial());
    }

    @Override
    protected Component construirCampos() {
        return Ui.rejilla(2,
                Ui.conEtiqueta("Estudiante *", cbEstudiante),
                Ui.conEtiqueta("Fecha * (dd/mm/aaaa)", tfFecha),
                Ui.conEtiqueta("Hora * (formato 24 h, por ejemplo 09:30)", tfHora),
                Ui.conEtiqueta("Motivo * (máximo 255 caracteres)", tfMotivo));
    }

    @Override
    protected void prepararFormulario(Object[] fila) throws DatosException {
        List<Object[]> estudiantes = estudianteDAO.listarActivosDeInstitucion(Contexto.idInstitucion());
        if (estudiantes.isEmpty()) {
            throw new IllegalStateException("Aún no hay estudiantes activos en tu institución.");
        }
        cbEstudiante.removeAllItems();
        for (Object[] e : estudiantes) {
            cbEstudiante.addItem(new Opcion((Integer) e[0],
                    e[1] + (e[2] == null ? "" : " (" + e[2] + ")"), e));
        }
        tfFecha.setText(Formato.texto(LocalDate.now()));
        tfHora.setText("");
        tfMotivo.setText("");
    }

    @Override
    protected void guardar(Integer idEdicion) throws DatosException {
        Opcion estudiante = (Opcion) cbEstudiante.getSelectedItem();
        if (estudiante == null) {
            throw new IllegalArgumentException("Selecciona el estudiante.");
        }
        LocalDate fecha = Contexto.fechaCita(tfFecha.getText());
        String hora = Contexto.horaCita(tfHora.getText());
        String motivo = Contexto.motivoCita(tfMotivo.getText());
        int idPsico = Contexto.idPsicosocial();
        if (citaDAO.hayConflicto(idPsico, fecha, hora)) {
            throw new IllegalArgumentException("Ya tienes una cita agendada el " + Formato.texto(fecha)
                    + " a las " + hora + ".");
        }
        CitaPsicosocial c = new CitaPsicosocial();
        c.setIdEstudiante(estudiante.getId());
        c.setIdPsicosocial(idPsico);
        c.setFecha(fecha);
        c.setHora(hora);
        c.setMotivo(motivo);
        citaDAO.agendar(c);
    }

    @Override
    protected String mensajeGuardado(boolean nuevo) {
        return "Cita agendada correctamente.";
    }

    private void cambiar(String estado, String mensaje) {
        Object[] f = filaActual();
        if (f == null) {
            return;
        }
        try {
            citaDAO.cambiarEstado((Integer) f[0], estado);
            refrescar();
            barraLista.exito(mensaje);
        } catch (DatosException ex) {
            barraLista.error("No se pudo actualizar la cita: " + ex.getMessage());
        }
    }
}
