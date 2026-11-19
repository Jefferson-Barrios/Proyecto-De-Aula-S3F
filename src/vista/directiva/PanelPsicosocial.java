package vista.directiva;

import dao.PsicosocialDAO;
import dao.UsuarioDAO;
import java.awt.Component;
import eipi.util.DatosException;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JTextField;
import modelo.Psicosocial;
import vista.componentes.Ui;

public class PanelPsicosocial extends PanelGestion {

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private final PsicosocialDAO psicosocialDAO = new PsicosocialDAO();
    private final CamposCuenta cuenta = new CamposCuenta();
    private final JTextField tfArea = Ui.campo();
    private final JTextField tfPerfil = Ui.campo();
    private final JTextField tfGrados = Ui.campo();

    public PanelPsicosocial() {
        iniciar();
    }

    @Override
    protected String titulo() {
        return "Psicosocial";
    }

    @Override
    protected String subtitulo() {
        return "Registra al equipo psicosocial que recibirá las alertas de deserción";
    }

    @Override
    protected String textoNuevo() {
        return "Nuevo profesional";
    }

    @Override
    protected String tituloFormNuevo() {
        return "Nuevo profesional psicosocial";
    }

    @Override
    protected String tituloFormEditar() {
        return "Editar profesional psicosocial";
    }

    @Override
    protected boolean permiteEditar() {
        return true;
    }

    @Override
    protected String[] columnas() {
        return new String[] { "Nombre", "Documento", "Usuario", "Área", "Grados a monitorear", "Estado" };
    }

    @Override
    protected int[] anchos() {
        return new int[] { 240, 120, 130, 150, 170, 90 };
    }

    @Override
    protected List<Object[]> consultar() throws DatosException {
        return psicosocialDAO.listar(idInstitucion());
    }

    @Override
    protected Component construirCampos() {
        return Ui.rejilla(2, CamposCuenta.unir(
                cuenta.personales(),
                new JComponent[] {
                        Ui.conEtiqueta("Área *", tfArea),
                        Ui.conEtiqueta("Perfil profesional", tfPerfil),
                        Ui.conEtiqueta("Grados a monitorear", tfGrados) },
                cuenta.acceso()));
    }

    @Override
    protected void prepararFormulario(Object[] fila) throws DatosException {
        if (fila == null) {
            cuenta.limpiar();
            tfArea.setText("");
            tfPerfil.setText("");
            tfGrados.setText("");
            cuenta.mostrarAcceso(true);
            return;
        }
        Psicosocial p = psicosocialDAO.obtener((Integer) fila[0], idInstitucion());
        if (p == null) {
            throw new IllegalStateException("No se encontró el profesional en tu institución.");
        }
        cuenta.cargar(p);
        tfArea.setText(p.getArea() == null ? "" : p.getArea());
        tfPerfil.setText(p.getPerfilProfesional() == null ? "" : p.getPerfilProfesional());
        tfGrados.setText(p.getGradosMonitorear() == null ? "" : p.getGradosMonitorear());
        cuenta.mostrarAcceso(false);
    }

    @Override
    protected void guardar(Integer idEdicion) throws DatosException {
        cuenta.validarPersonales();
        if (tfArea.getText().trim().isEmpty()) {
            throw new IllegalArgumentException("Indica el área del profesional (por ejemplo, Psicología).");
        }
        if (idEdicion == null) {
            cuenta.validarAcceso();
            cuenta.validarDuplicados(usuarioDAO);
        } else {
            cuenta.validarDocumentoLibre(usuarioDAO, idEdicion);
        }
        Psicosocial p = new Psicosocial();
        cuenta.aplicar(p, idInstitucion());
        p.setArea(tfArea.getText().trim());
        p.setPerfilProfesional(nulo(tfPerfil.getText()));
        p.setGradosMonitorear(nulo(tfGrados.getText()));
        if (idEdicion == null) {
            psicosocialDAO.crear(p);
        } else {
            p.setIdUsuario(idEdicion);
            psicosocialDAO.actualizar(p, idInstitucion());
        }
    }

    @Override
    protected boolean permiteRestablecerClave() {
        return true;
    }

    @Override
    protected String mensajeGuardado(boolean nuevo) {
        return nuevo ? "Profesional registrado. Usuario de acceso: " + cuenta.usuario.getText().trim()
                : "Datos del profesional actualizados.";
    }

    @Override
    protected void cambiarEstado(int id, String nuevoEstado) throws DatosException {
        usuarioDAO.cambiarEstado(id, nuevoEstado);
    }
}
