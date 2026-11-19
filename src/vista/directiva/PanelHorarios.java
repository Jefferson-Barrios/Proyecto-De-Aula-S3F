package vista.directiva;

import dao.AsignacionDAO;
import dao.HorarioDAO;
import eipi.util.Formato;
import java.awt.Component;
import eipi.util.DatosException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import vista.componentes.BotonSimple;
import vista.componentes.Opcion;
import vista.componentes.Ui;

public class PanelHorarios extends PanelGestion {

    private static final String[] DIAS = { "Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado" };

    private final HorarioDAO horarioDAO = new HorarioDAO();
    private final AsignacionDAO asignacionDAO = new AsignacionDAO();
    private final JComboBox<Opcion> cbAsignacion = Ui.combo();
    private final JComboBox<String> cbDia = Ui.combo();
    private final JTextField tfInicio = Ui.campo();
    private final JTextField tfFin = Ui.campo();
    private final JTextField tfAula = Ui.campo();
    private final BotonSimple btnEliminar = new BotonSimple("Eliminar franja", BotonSimple.Tipo.PELIGRO);

    private final Map<Integer, Integer> asignacionDeFranja = new HashMap<>();
    private boolean confirmando = false;

    public PanelHorarios() {
        for (String d : DIAS) {
            cbDia.addItem(d);
        }
        btnEliminar.addActionListener(e -> eliminar());
        iniciar();
    }

    @Override
    protected String titulo() {
        return "Horarios";
    }

    @Override
    protected String subtitulo() {
        return "Planifica las clases de cada curso; se evitan cruces de docente, curso y aula";
    }

    @Override
    protected String textoNuevo() {
        return "Nueva franja";
    }

    @Override
    protected String tituloFormNuevo() {
        return "Nueva franja de horario";
    }

    @Override
    protected String tituloFormEditar() {
        return "Editar franja de horario";
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
    protected List<Component> botonesExtra() {
        List<Component> l = new ArrayList<>();
        l.add(btnEliminar);
        return l;
    }

    @Override
    protected void alSeleccionar(Object[] fila) {
        confirmando = false;
        btnEliminar.setText("Eliminar franja");
        btnEliminar.setEnabled(fila != null);
    }

    @Override
    protected String[] columnas() {
        return new String[] { "Día", "Inicio", "Fin", "Curso", "Materia", "Docente", "Aula" };
    }

    @Override
    protected int[] anchos() {
        return new int[] { 100, 70, 70, 70, 200, 230, 100 };
    }

    @Override
    protected List<Object[]> consultar() throws DatosException {
        asignacionDeFranja.clear();
        List<Object[]> filas = new ArrayList<>();
        for (Object[] f : horarioDAO.listar(idInstitucion())) {
            asignacionDeFranja.put((Integer) f[0], (Integer) f[8]);
            filas.add(new Object[] { f[0], f[1], f[2], f[3], f[4], f[5], f[6], f[7] });
        }
        return filas;
    }

    @Override
    protected Component construirCampos() {
        return Ui.rejilla(2,
                Ui.conEtiqueta("Asignación * (curso · materia · docente)", cbAsignacion),
                Ui.conEtiqueta("Día *", cbDia),
                Ui.conEtiqueta("Hora de inicio * (formato 24 h, por ejemplo 07:00)", tfInicio),
                Ui.conEtiqueta("Hora de fin * (por ejemplo 07:55)", tfFin),
                Ui.conEtiqueta("Aula", tfAula));
    }

    @Override
    protected void prepararFormulario(Object[] fila) throws DatosException {
        List<Object[]> asignaciones = new ArrayList<>();
        for (Object[] a : asignacionDAO.listar(idInstitucion())) {
            if ("ACTIVO".equals(a[5])) {
                asignaciones.add(a);
            }
        }
        if (asignaciones.isEmpty()) {
            throw new IllegalStateException("Primero crea al menos una asignación activa en la sección Asignaciones.");
        }
        Integer actual = fila == null ? null : asignacionDeFranja.get((Integer) fila[0]);
        cbAsignacion.removeAllItems();
        Opcion elegida = null;

        for (Object[] a : asignaciones) {
            Opcion o = new Opcion((Integer) a[0], a[2] + " · " + a[3] + " · " + a[1], a);
            cbAsignacion.addItem(o);
            if (actual != null && actual == o.getId()) {
                elegida = o;
            }
        }
        if (elegida != null) {
            cbAsignacion.setSelectedItem(elegida);
        }
        if (fila == null) {
            cbDia.setSelectedIndex(0);
            tfInicio.setText("");
            tfFin.setText("");
            tfAula.setText("");
        } else {
            cbDia.setSelectedItem(fila[1]);
            tfInicio.setText(String.valueOf(fila[2]));
            tfFin.setText(String.valueOf(fila[3]));
            tfAula.setText(fila[7] == null ? "" : String.valueOf(fila[7]));
        }
    }

    @Override
    protected void guardar(Integer idEdicion) throws DatosException {
        Opcion asignacion = (Opcion) cbAsignacion.getSelectedItem();
        if (asignacion == null) {
            throw new IllegalArgumentException("Selecciona la asignación.");
        }
        String dia = (String) cbDia.getSelectedItem();
        String inicio = Formato.hora(tfInicio.getText());
        String fin = Formato.hora(tfFin.getText());
        if (inicio.compareTo(fin) >= 0) {
            throw new IllegalArgumentException("La hora de fin debe ser posterior a la de inicio.");
        }
        String aula = nulo(tfAula.getText());
        if (aula != null && aula.length() > 30) {
            throw new IllegalArgumentException("El nombre del aula es muy largo (máximo 30 caracteres).");
        }
        String conflicto = horarioDAO.buscarConflicto(idInstitucion(), asignacion.getId(), dia, inicio, fin,
                aula, idEdicion == null ? 0 : idEdicion);
        if (conflicto != null) {
            throw new IllegalArgumentException(conflicto);
        }
        if (idEdicion == null) {
            horarioDAO.insertar(asignacion.getId(), dia, inicio, fin, aula);
        } else {
            horarioDAO.actualizar(idEdicion, asignacion.getId(), dia, inicio, fin, aula);
        }
    }

    @Override
    protected String mensajeGuardado(boolean nuevo) {
        return nuevo ? "Franja agregada al horario." : "Franja actualizada.";
    }

    private void eliminar() {
        Object[] f = filaActual();
        if (f == null) {
            return;
        }
        if (!confirmando) {
            confirmando = true;
            btnEliminar.setText("¿Seguro? Clic para eliminar");
            return;
        }
        try {
            horarioDAO.eliminar((Integer) f[0]);
            refrescar();
            barraLista.exito("Franja eliminada del horario.");
        } catch (DatosException ex) {
            barraLista.error("No se pudo eliminar la franja: " + ex.getMessage());
        }
    }
}
