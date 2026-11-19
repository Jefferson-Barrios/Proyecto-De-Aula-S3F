package vista.directiva;

import dao.CursoDAO;
import dao.EstudianteDAO;
import dao.UsuarioDAO;
import eipi.util.Formato;
import java.awt.Component;
import eipi.util.DatosException;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeParseException;
import java.util.List;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextField;
import modelo.Acudiente;
import modelo.Curso;
import modelo.Estudiante;
import vista.componentes.Opcion;
import vista.componentes.Ui;

public class PanelEstudiantes extends PanelGestion {

    private static final String[] TIPOS_DOCUMENTO = {
            "Tarjeta de identidad", "Registro civil", "Cédula de ciudadanía", "Cédula de extranjería", "Otro" };

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private final EstudianteDAO estudianteDAO = new EstudianteDAO();
    private final CursoDAO cursoDAO = new CursoDAO();
    private final CamposCuenta cuenta = new CamposCuenta();
    private final JComboBox<String> cbTipoDoc = Ui.combo();
    private final JTextField tfFecha = Ui.campo();
    private final JTextField tfDireccion = Ui.campo();
    private final JTextField tfBarrio = Ui.campo();
    private final JComboBox<Opcion> cbCurso = Ui.combo();
    private final JTextField tfAcudiente = Ui.campo();
    private final JTextField tfAcuTelefono = Ui.campo();
    private final JTextField tfAcuCorreo = Ui.campo();
    private final JLabel tituloAcceso = seccion("Credenciales de acceso del estudiante");

    public PanelEstudiantes() {
        for (String t : TIPOS_DOCUMENTO) {
            cbTipoDoc.addItem(t);
        }
        iniciar();
    }

    @Override
    protected String titulo() {
        return "Estudiantes";
    }

    @Override
    protected String subtitulo() {
        return "Registra a los estudiantes de tu institución, su acudiente y su acceso";
    }

    @Override
    protected String textoNuevo() {
        return "Nuevo estudiante";
    }

    @Override
    protected String tituloFormNuevo() {
        return "Nuevo estudiante";
    }

    @Override
    protected String tituloFormEditar() {
        return "Editar estudiante";
    }

    @Override
    protected boolean permiteEditar() {
        return true;
    }

    @Override
    protected String[] columnas() {
        return new String[] { "Nombre", "Documento", "Curso", "Acudiente", "Tel. acudiente", "Estado" };
    }

    @Override
    protected int[] anchos() {
        return new int[] { 240, 120, 80, 200, 130, 90 };
    }

    @Override
    protected List<Object[]> consultar() throws DatosException {
        return estudianteDAO.listar(idInstitucion());
    }

    @Override
    protected Component construirCampos() {
        return formulario(2,
                "Datos del estudiante",
                cuenta.personales(),
                Ui.conEtiqueta("Tipo de documento *", cbTipoDoc),
                Ui.conEtiqueta("Fecha de nacimiento * (dd/mm/aaaa)", tfFecha),
                Ui.conEtiqueta("Dirección", tfDireccion),
                Ui.conEtiqueta("Barrio", tfBarrio),
                Ui.conEtiqueta("Curso *", cbCurso),
                "Datos del acudiente",
                Ui.conEtiqueta("Nombre del acudiente *", tfAcudiente),
                Ui.conEtiqueta("Teléfono del acudiente *", tfAcuTelefono),
                Ui.conEtiqueta("Correo del acudiente", tfAcuCorreo),
                tituloAcceso,
                cuenta.acceso());
    }

    @Override
    protected void prepararFormulario(Object[] fila) throws DatosException {
        int inst = idInstitucion();
        List<Curso> cursos = cursoDAO.listar(inst);
        if (cursos.isEmpty()) {
            throw new IllegalStateException("Primero crea al menos un curso en la sección Cursos.");
        }

        Estudiante actual = null;
        Acudiente acudiente = null;
        if (fila != null) {
            Object[] datos = estudianteDAO.obtener((Integer) fila[0], inst);
            if (datos == null) {
                throw new IllegalStateException("No se encontró el estudiante en tu institución.");
            }
            actual = (Estudiante) datos[0];
            acudiente = (Acudiente) datos[1];
        }

        cbCurso.removeAllItems();
        Opcion elegida = null;
        for (Curso c : cursos) {
            Opcion o = new Opcion(c.getIdCurso(),
                    c.getGrado() + "-" + c.getGrupo() + " (" + c.getJornada() + ")", c);
            cbCurso.addItem(o);
            if (actual != null && actual.getIdCurso() != null && actual.getIdCurso() == c.getIdCurso()) {
                elegida = o;
            }
        }
        if (elegida != null) {
            cbCurso.setSelectedItem(elegida);
        }

        if (actual == null) {
            cuenta.limpiar();
            cbTipoDoc.setSelectedIndex(0);
            tfFecha.setText("");
            tfDireccion.setText("");
            tfBarrio.setText("");
            tfAcudiente.setText("");
            tfAcuTelefono.setText("");
            tfAcuCorreo.setText("");
        } else {
            cuenta.cargar(actual);
            cbTipoDoc.setSelectedItem(actual.getTipoDocumento());
            tfFecha.setText(Formato.texto(actual.getFechaNacimiento()));
            tfDireccion.setText(actual.getDireccion() == null ? "" : actual.getDireccion());
            tfBarrio.setText(actual.getBarrio() == null ? "" : actual.getBarrio());
            tfAcudiente.setText(acudiente.getNombre() == null ? "" : acudiente.getNombre());
            tfAcuTelefono.setText(acudiente.getTelefono() == null ? "" : acudiente.getTelefono());
            tfAcuCorreo.setText(acudiente.getCorreo() == null ? "" : acudiente.getCorreo());
        }
        cuenta.mostrarAcceso(fila == null);
        tituloAcceso.setVisible(fila == null);
    }

    @Override
    protected void guardar(Integer idEdicion) throws DatosException {
        cuenta.validarPersonales();
        if (idEdicion == null) {
            cuenta.validarAcceso();
        }

        LocalDate nacimiento;
        try {
            nacimiento = Formato.parsear(tfFecha.getText());
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException(
                    "La fecha de nacimiento debe tener el formato dd/mm/aaaa (por ejemplo 15/03/2012).");
        }
        if (nacimiento.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("La fecha de nacimiento no puede ser futura.");
        }
        int edad = Period.between(nacimiento, LocalDate.now()).getYears();
        if (edad > 30) {
            throw new IllegalArgumentException(
                    "Revisa la fecha de nacimiento: la edad resultante es de " + edad + " años.");
        }

        String acuNombre = tfAcudiente.getText().trim();
        String acuTel = tfAcuTelefono.getText().trim();
        String acuCorreo = tfAcuCorreo.getText().trim();
        if (acuNombre.isEmpty() || acuTel.isEmpty()) {
            throw new IllegalArgumentException("Completa el nombre y el teléfono del acudiente.");
        }
        if (!acuTel.matches("^[0-9+()\\s-]{7,20}$")) {
            throw new IllegalArgumentException("El teléfono del acudiente debe tener solo números (mínimo 7 dígitos).");
        }
        if (!acuCorreo.isEmpty() && !acuCorreo.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            throw new IllegalArgumentException("El correo del acudiente no tiene un formato válido.");
        }
        Opcion opcionCurso = (Opcion) cbCurso.getSelectedItem();
        if (opcionCurso == null) {
            throw new IllegalArgumentException("Selecciona el curso del estudiante.");
        }

        if (idEdicion == null) {
            cuenta.validarDuplicados(usuarioDAO);
        } else {
            cuenta.validarDocumentoLibre(usuarioDAO, idEdicion);
        }

        Curso curso = (Curso) opcionCurso.getDato();
        Estudiante e = new Estudiante();
        cuenta.aplicar(e, idInstitucion());
        e.setIdCurso(curso.getIdCurso());
        e.setFechaNacimiento(nacimiento);
        e.setEdad(edad);
        e.setDireccion(nulo(tfDireccion.getText()));
        e.setBarrio(nulo(tfBarrio.getText()));
        e.setTipoDocumento((String) cbTipoDoc.getSelectedItem());
        e.setGrado(curso.getGrado());
        e.setJornada(curso.getJornada());

        Acudiente a = new Acudiente();
        a.setNombre(acuNombre);
        a.setTelefono(acuTel);
        a.setCorreo(acuCorreo.isEmpty() ? null : acuCorreo);

        if (idEdicion == null) {
            estudianteDAO.crear(e, a);
        } else {
            e.setIdUsuario(idEdicion);
            estudianteDAO.actualizar(e, a, idInstitucion());
        }
    }

    @Override
    protected boolean permiteRestablecerClave() {
        return true;
    }

    @Override
    protected String mensajeGuardado(boolean nuevo) {
        return nuevo ? "Estudiante registrado con su acudiente y expediente. Usuario de acceso: "
                + cuenta.usuario.getText().trim() : "Datos del estudiante actualizados.";
    }

    @Override
    protected void cambiarEstado(int id, String nuevoEstado) throws DatosException {
        usuarioDAO.cambiarEstado(id, nuevoEstado);
    }
}
