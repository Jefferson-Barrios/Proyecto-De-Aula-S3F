package vista.docente;

import controlador.AnalisisRiesgo;
import dao.CalificacionDAO;
import eipi.util.Formato;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import eipi.util.DatosException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import modelo.PeriodoAcademico;
import vista.componentes.BarraMensaje;
import vista.componentes.BotonSimple;
import vista.componentes.Opcion;
import vista.componentes.Tarjeta;
import vista.componentes.Ui;

public class PanelCalificaciones extends PanelBaseDocente {

    private static final String[] TIPOS = { "Taller", "Quiz", "Examen", "Tarea", "Participación", "Proyecto" };

    private final CalificacionDAO calificacionDAO = new CalificacionDAO();
    private List<Object[]> datos = new ArrayList<>();

    private final BarraMensaje barraNota = new BarraMensaje();
    private final JLabel lblEstudiante = Ui.titulo(" ");
    private final JTextField tfValor = Ui.campo();
    private final JComboBox<String> cbTipo = Ui.combo();
    private final JTextField tfFechaNota = Ui.campo();
    private int idEstudianteNota = -1;

    public PanelCalificaciones() {
        for (String t : TIPOS) {
            cbTipo.addItem(t);
        }
        iniciar();
    }

    @Override
    protected String titulo() {
        return "Calificaciones";
    }

    @Override
    protected String subtitulo() {
        return "Registra las notas de tus estudiantes (de 0,0 a 5,0) por período";
    }

    @Override
    protected boolean usaPeriodo() {
        return true;
    }

    @Override
    protected String[] columnas() {
        return new String[] { "Estudiante", "Notas registradas", "Promedio" };
    }

    @Override
    protected int[] anchos() {
        return new int[] { 280, 360, 100 };
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void cargarTabla() throws DatosException {
        datos = calificacionDAO.listarResumen(asignacion().getId(), periodo().getId());
        modelo.setRowCount(0);
        for (Object[] r : datos) {
            List<Double> notas = (List<Double>) r[2];
            Double promedio = AnalisisRiesgo.calcularPromedio(notas);
            StringBuilder sb = new StringBuilder();
            for (Double n : notas) {
                if (sb.length() > 0) {
                    sb.append("  ·  ");
                }
                sb.append(Formato.nota(n));
            }
            modelo.addRow(new Object[] { r[1], notas.isEmpty() ? "Sin notas" : sb.toString(),
                    promedio == null ? "—" : Formato.nota(promedio) });
        }
    }

    @Override
    protected JComponent construirBotones() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        p.setOpaque(false);
        BotonSimple nota = new BotonSimple("+ Registrar nota", BotonSimple.Tipo.PRIMARIO);
        BotonSimple definitivas = new BotonSimple("Guardar notas definitivas", BotonSimple.Tipo.SECUNDARIO);
        nota.addActionListener(e -> abrirFormularioNota());
        definitivas.addActionListener(e -> guardarDefinitivas());
        p.add(nota);
        p.add(definitivas);
        return p;
    }

    @Override
    protected JComponent construirExtra() {
        JPanel p = new JPanel(new BorderLayout(0, 16));
        p.setOpaque(false);

        JPanel norte = new JPanel();
        norte.setOpaque(false);
        norte.setLayout(new BoxLayout(norte, BoxLayout.Y_AXIS));
        JLabel t = Ui.titulo("Registrar nota");
        lblEstudiante.setAlignmentX(LEFT_ALIGNMENT);
        t.setAlignmentX(LEFT_ALIGNMENT);
        barraNota.setAlignmentX(LEFT_ALIGNMENT);
        JLabel s = Ui.subtitulo("Los campos con * son obligatorios");
        s.setAlignmentX(LEFT_ALIGNMENT);
        norte.add(t);
        norte.add(s);
        norte.add(barraNota);
        p.add(norte, BorderLayout.NORTH);

        Tarjeta tarjeta = new Tarjeta(new BorderLayout());
        lblEstudiante.setFont(vista.componentes.Estilo.fuentePx(vista.componentes.Estilo.NEGRITA, 17f));
        lblEstudiante.setForeground(vista.componentes.Estilo.AZUL);
        JPanel contenido = new JPanel(new BorderLayout());
        contenido.setOpaque(false);
        JPanel cab = new JPanel(new BorderLayout());
        cab.setOpaque(false);
        cab.setBorder(BorderFactory.createEmptyBorder(0, 8, 4, 0));
        cab.add(lblEstudiante, BorderLayout.CENTER);
        contenido.add(cab, BorderLayout.NORTH);
        contenido.add(Ui.rejilla(2,
                Ui.conEtiqueta("Nota * (0,0 a 5,0)", tfValor),
                Ui.conEtiqueta("Tipo *", cbTipo),
                Ui.conEtiqueta("Fecha * (dd/mm/aaaa)", tfFechaNota)), BorderLayout.CENTER);
        tarjeta.add(contenido, BorderLayout.NORTH);
        p.add(tarjeta, BorderLayout.CENTER);

        BotonSimple guardar = new BotonSimple("Guardar nota", BotonSimple.Tipo.PRIMARIO);
        BotonSimple cancelar = new BotonSimple("Cancelar", BotonSimple.Tipo.SECUNDARIO);
        guardar.addActionListener(e -> guardarNota());
        cancelar.addActionListener(e -> mostrarPrincipal());
        JPanel sur = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        sur.setOpaque(false);
        sur.add(guardar);
        sur.add(cancelar);
        p.add(sur, BorderLayout.SOUTH);
        return p;
    }

    private void abrirFormularioNota() {
        int fila = filaSeleccionada();
        if (fila < 0 || fila >= datos.size()) {
            barra.error("Selecciona primero un estudiante de la lista.");
            return;
        }
        PeriodoAcademico p = (PeriodoAcademico) periodo().getDato();
        if (!"ABIERTO".equals(p.getEstado())) {
            barra.error("El período \"" + p.getNombre() + "\" está cerrado: no se pueden registrar notas.");
            return;
        }
        idEstudianteNota = (Integer) datos.get(fila)[0];
        lblEstudiante.setText(String.valueOf(datos.get(fila)[1]));
        tfValor.setText("");
        cbTipo.setSelectedIndex(0);
        tfFechaNota.setText(Formato.texto(LocalDate.now()));
        barraNota.limpiar();
        barra.limpiar();
        mostrarExtra();
        tfValor.requestFocusInWindow();
    }

    private void guardarNota() {
        double valor;
        try {
            valor = Double.parseDouble(tfValor.getText().trim().replace(',', '.'));
        } catch (NumberFormatException ex) {
            barraNota.error("Escribe la nota como número, por ejemplo 4,5.");
            return;
        }
        if (valor < 0 || valor > 5) {
            barraNota.error("La nota debe estar entre 0,0 y 5,0.");
            return;
        }
        valor = Math.round(valor * 10) / 10.0;

        LocalDate fecha;
        try {
            fecha = Formato.parsear(tfFechaNota.getText());
        } catch (java.time.format.DateTimeParseException ex) {
            barraNota.error("La fecha debe tener el formato dd/mm/aaaa.");
            return;
        }
        if (fecha.isAfter(LocalDate.now())) {
            barraNota.error("La fecha de la nota no puede ser futura.");
            return;
        }
        try {
            calificacionDAO.insertar(idEstudianteNota, asignacion().getId(), periodo().getId(),
                    valor, (String) cbTipo.getSelectedItem(), fecha);
            mostrarPrincipal();
            cargarTabla();
            barra.exito("Nota " + Formato.nota(valor) + " registrada a " + lblEstudiante.getText() + ".");
        } catch (DatosException ex) {
            barraNota.error("No se pudo guardar la nota: " + ex.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private void guardarDefinitivas() {
        Map<Integer, Double> definitivas = new LinkedHashMap<>();
        for (Object[] r : datos) {
            Double promedio = AnalisisRiesgo.calcularPromedio((List<Double>) r[2]);
            if (promedio != null) {
                definitivas.put((Integer) r[0], Math.round(promedio * 10) / 10.0);
            }
        }
        if (definitivas.isEmpty()) {
            barra.error("Todavía no hay notas registradas para calcular la definitiva.");
            return;
        }
        try {
            calificacionDAO.guardarDefinitivas(asignacion().getId(), periodo().getId(), definitivas);
            barra.exito("Notas definitivas guardadas para " + definitivas.size() + " estudiante(s).");
        } catch (DatosException ex) {
            barra.error("No se pudo guardar la nota definitiva: " + ex.getMessage());
        }
    }
}
