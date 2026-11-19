package vista.directiva;

import dao.DocenteDAO;
import dao.UsuarioDAO;
import java.awt.Component;
import eipi.util.DatosException;
import java.util.List;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JTextField;
import modelo.Docente;
import vista.componentes.Ui;

public class PanelDocentes extends PanelGestion {

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private final DocenteDAO docenteDAO = new DocenteDAO();
    private final CamposCuenta cuenta = new CamposCuenta();
    private final JComboBox<String> cbJornada = Ui.combo();
    private final JTextField tfCarga = Ui.campo();

    public PanelDocentes() {
        for (String j : JORNADAS) {
            cbJornada.addItem(j);
        }
        iniciar();
    }

    @Override
    protected String titulo() {
        return "Docentes";
    }

    @Override
    protected String subtitulo() {
        return "Registra los docentes de tu institución y crea su acceso";
    }

    @Override
    protected String textoNuevo() {
        return "Nuevo docente";
    }

    @Override
    protected String tituloFormNuevo() {
        return "Nuevo docente";
    }

    @Override
    protected String tituloFormEditar() {
        return "Editar docente";
    }

    @Override
    protected boolean permiteEditar() {
        return true;
    }

    @Override
    protected String[] columnas() {
        return new String[] { "Nombre", "Documento", "Usuario", "Jornada", "Carga académica", "Estado" };
    }

    @Override
    protected int[] anchos() {
        return new int[] { 240, 120, 130, 100, 160, 90 };
    }

    @Override
    protected List<Object[]> consultar() throws DatosException {
        return docenteDAO.listar(idInstitucion());
    }

    @Override
    protected Component construirCampos() {
        return Ui.rejilla(2, CamposCuenta.unir(
                cuenta.personales(),
                new JComponent[] {
                        Ui.conEtiqueta("Jornada *", cbJornada),
                        Ui.conEtiqueta("Carga académica", tfCarga) },
                cuenta.acceso()));
    }

    @Override
    protected void prepararFormulario(Object[] fila) throws DatosException {
        if (fila == null) {
            cuenta.limpiar();
            tfCarga.setText("");
            cbJornada.setSelectedIndex(0);
            cuenta.mostrarAcceso(true);
            return;
        }
        Docente d = docenteDAO.obtener((Integer) fila[0], idInstitucion());
        if (d == null) {
            throw new IllegalStateException("No se encontró el docente en tu institución.");
        }
        cuenta.cargar(d);
        cbJornada.setSelectedItem(d.getJornada());
        tfCarga.setText(d.getCargaAcademica() == null ? "" : d.getCargaAcademica());
        cuenta.mostrarAcceso(false);
    }

    @Override
    protected void guardar(Integer idEdicion) throws DatosException {
        cuenta.validarPersonales();
        if (idEdicion == null) {
            cuenta.validarAcceso();
            cuenta.validarDuplicados(usuarioDAO);
        } else {
            cuenta.validarDocumentoLibre(usuarioDAO, idEdicion);
        }
        Docente d = new Docente();
        cuenta.aplicar(d, idInstitucion());
        d.setJornada((String) cbJornada.getSelectedItem());
        d.setCargaAcademica(nulo(tfCarga.getText()));
        if (idEdicion == null) {
            docenteDAO.crear(d);
        } else {
            d.setIdUsuario(idEdicion);
            docenteDAO.actualizar(d, idInstitucion());
        }
    }

    @Override
    protected boolean permiteRestablecerClave() {
        return true;
    }

    @Override
    protected String mensajeGuardado(boolean nuevo) {
        return nuevo ? "Docente registrado. Usuario de acceso: " + cuenta.usuario.getText().trim()
                : "Datos del docente actualizados.";
    }

    @Override
    protected void cambiarEstado(int id, String nuevoEstado) throws DatosException {
        usuarioDAO.cambiarEstado(id, nuevoEstado);
    }
}
