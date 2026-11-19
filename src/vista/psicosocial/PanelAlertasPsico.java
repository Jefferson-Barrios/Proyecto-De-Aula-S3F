package vista.psicosocial;

import vista.componentes.BotonExportar;
import eipi.util.TablaExportable;
import controlador.AnalisisRiesgo;
import dao.AlertaDAO;
import dao.CitaDAO;
import eipi.util.Formato;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.FlowLayout;
import eipi.util.DatosException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import modelo.CitaPsicosocial;
import vista.Refrescable;
import vista.componentes.BarraMensaje;
import vista.componentes.BotonSimple;
import vista.componentes.Estilo;
import vista.componentes.Tarjeta;
import vista.componentes.Ui;

public class PanelAlertasPsico extends JPanel implements Refrescable {

    private static final String LISTA = "LISTA";
    private static final String CITA = "CITA";
    private static final String TODAS = "Todas";

    private final AlertaDAO alertaDAO = new AlertaDAO();
    private final CitaDAO citaDAO = new CitaDAO();
    private final CardLayout cartas = new CardLayout();
    private final JPanel cuerpo = new JPanel(cartas);
    private List<Object[]> filas = new ArrayList<>();
    private boolean cargando = false;

    private final BarraMensaje barraLista = new BarraMensaje();
    private final JComboBox<String> cbFiltro = Ui.combo();
    private final DefaultTableModel modelo = new DefaultTableModel(
            new Object[] { "Estudiante", "Curso", "Nivel", "Asistencia", "Perdidas", "Motivo", "Fecha", "Estado" }, 0) {
        @Override
        public boolean isCellEditable(int fila, int col) {
            return false;
        }
    };
    private final JTable tabla = new JTable(modelo);
    private final BotonSimple btnTomar = new BotonSimple("Tomar alerta", BotonSimple.Tipo.PRIMARIO);
    private final BotonSimple btnCita = new BotonSimple("Agendar cita", BotonSimple.Tipo.SECUNDARIO);
    private final BotonSimple btnCerrar = new BotonSimple("Cerrar alerta", BotonSimple.Tipo.PELIGRO);

    private final BarraMensaje barraCita = new BarraMensaje();
    private final JLabel lblEstudiante = new JLabel(" ");
    private final JTextField tfFecha = Ui.campo();
    private final JTextField tfHora = Ui.campo();
    private final JTextField tfMotivo = Ui.campo();
    private Object[] alertaCita;

    public PanelAlertasPsico() {
        setLayout(new BorderLayout());
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(28, 32, 28, 32));
        cbFiltro.addItem(TODAS);
        cbFiltro.addItem("PENDIENTE");
        cbFiltro.addItem("EN SEGUIMIENTO");
        cbFiltro.addItem("CERRADA");
        cuerpo.setOpaque(false);
        cuerpo.add(construirLista(), LISTA);
        cuerpo.add(construirCita(), CITA);
        add(cuerpo, BorderLayout.CENTER);
        cbFiltro.addActionListener(e -> {
            if (!cargando) {
                recargar();
            }
        });
    }

    private JPanel construirLista() {
        JPanel p = new JPanel(new BorderLayout(0, 16));
        p.setOpaque(false);

        JPanel norte = new JPanel(new BorderLayout());
        norte.setOpaque(false);
        JPanel textos = new JPanel();
        textos.setOpaque(false);
        textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));
        JLabel t = Ui.titulo("Alertas de deserción");
        JLabel s = Ui.subtitulo("Las alertas se generan solas al cerrar cada período; las más graves aparecen primero");
        t.setAlignmentX(LEFT_ALIGNMENT);
        s.setAlignmentX(LEFT_ALIGNMENT);
        barraLista.setAlignmentX(LEFT_ALIGNMENT);
        textos.add(t);
        textos.add(s);
        textos.add(barraLista);
        norte.add(textos, BorderLayout.CENTER);
        JPanel filtro = new JPanel(new BorderLayout());
        filtro.setOpaque(false);
        cbFiltro.setPreferredSize(new java.awt.Dimension(190, 42));
        filtro.add(Ui.conEtiqueta("Estado", cbFiltro), BorderLayout.CENTER);
        norte.add(filtro, BorderLayout.EAST);
        p.add(norte, BorderLayout.NORTH);

        Ui.estilizarTabla(tabla);

        int[] an = { 155, 62, 110, 100, 85, 240, 115, 145 };
        for (int i = 0; i < an.length; i++) {
            tabla.getColumnModel().getColumn(i).setPreferredWidth(an[i]);
            if (i != 5) {
                tabla.getColumnModel().getColumn(i).setMinWidth(an[i] - 5);
            }
        }
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.getSelectionModel().addListSelectionListener(e -> actualizarBotones());
        Tarjeta tarjeta = new Tarjeta(new BorderLayout());
        tarjeta.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        tarjeta.add(Ui.scroll(tabla), BorderLayout.CENTER);
        p.add(tarjeta, BorderLayout.CENTER);

        btnTomar.addActionListener(e -> tomar());
        btnCita.addActionListener(e -> abrirCita());
        btnCerrar.addActionListener(e -> cerrar());
        JPanel sur = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        sur.setOpaque(false);
        sur.add(btnTomar);
        sur.add(btnCita);
        sur.add(btnCerrar);
        BotonExportar exportar = new BotonExportar(
                () -> TablaExportable.de("Alertas de deserción", "Estado: " + cbFiltro.getSelectedItem(), modelo),
                barraLista);
        JPanel derecha = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        derecha.setOpaque(false);
        derecha.add(exportar);
        JPanel pie = new JPanel(new BorderLayout());
        pie.setOpaque(false);
        pie.add(sur, BorderLayout.CENTER);
        pie.add(derecha, BorderLayout.EAST);
        p.add(pie, BorderLayout.SOUTH);
        actualizarBotones();
        return p;
    }

    private Object[] seleccionada() {
        int fila = tabla.getSelectedRow();
        return (fila >= 0 && fila < filas.size()) ? filas.get(fila) : null;
    }

    private void actualizarBotones() {
        Object[] f = seleccionada();
        String estado = f == null ? "" : String.valueOf(f[9]);
        btnTomar.setEnabled(f != null && "PENDIENTE".equals(estado));
        btnCita.setEnabled(f != null && !"CERRADA".equals(estado));
        btnCerrar.setEnabled(f != null && !"CERRADA".equals(estado));
    }

    public void cargar(List<Object[]> lista) {
        filas = new ArrayList<>(lista);
        modelo.setRowCount(0);
        for (Object[] f : filas) {
            modelo.addRow(new Object[] { f[1], f[2] == null ? "" : f[2], f[3],
                    AnalisisRiesgo.formatoPorcentaje((Double) f[4]), f[5], f[6], f[8], f[9] });
        }
        actualizarBotones();
    }

    private void recargar() {
        try {
            String filtro = (String) cbFiltro.getSelectedItem();
            cargar(alertaDAO.listarDeInstitucion(Contexto.idInstitucion(), TODAS.equals(filtro) ? null : filtro));
        } catch (IllegalStateException ex) {
            barraLista.error(ex.getMessage());
        } catch (DatosException ex) {
            barraLista.error("No se pudieron cargar las alertas. Revisa la carpeta datos del proyecto. ("
                    + ex.getMessage() + ")");
        }
    }

    @Override
    public void refrescar() {
        cartas.show(cuerpo, LISTA);
        cargando = true;
        try {
            barraLista.limpiar();
            recargar();
        } finally {
            cargando = false;
        }
    }

    private void tomar() {
        Object[] f = seleccionada();
        if (f == null) {
            return;
        }
        try {
            alertaDAO.tomar((Integer) f[0], Contexto.idPsicosocial());
            recargar();
            barraLista.exito("Tomaste la alerta de " + f[1] + ": ahora está en seguimiento.");
        } catch (DatosException ex) {
            barraLista.error("No se pudo tomar la alerta: " + ex.getMessage());
        }
    }

    private void cerrar() {
        Object[] f = seleccionada();
        if (f == null) {
            return;
        }
        try {
            alertaDAO.cerrar((Integer) f[0], Contexto.idPsicosocial());
            recargar();
            barraLista.exito("Alerta de " + f[1] + " cerrada.");
        } catch (DatosException ex) {
            barraLista.error("No se pudo cerrar la alerta: " + ex.getMessage());
        }
    }

    private JPanel construirCita() {
        JPanel p = new JPanel(new BorderLayout(0, 16));
        p.setOpaque(false);

        JPanel norte = new JPanel();
        norte.setOpaque(false);
        norte.setLayout(new BoxLayout(norte, BoxLayout.Y_AXIS));
        JLabel t = Ui.titulo("Agendar cita");
        JLabel s = Ui.subtitulo("Los campos con * son obligatorios");
        t.setAlignmentX(LEFT_ALIGNMENT);
        s.setAlignmentX(LEFT_ALIGNMENT);
        barraCita.setAlignmentX(LEFT_ALIGNMENT);
        norte.add(t);
        norte.add(s);
        norte.add(barraCita);
        p.add(norte, BorderLayout.NORTH);

        lblEstudiante.setFont(Estilo.fuentePx(Estilo.NEGRITA, 17f));
        lblEstudiante.setForeground(Estilo.AZUL);
        lblEstudiante.setBorder(BorderFactory.createEmptyBorder(0, 8, 4, 0));
        JPanel contenido = new JPanel(new BorderLayout());
        contenido.setOpaque(false);
        contenido.add(lblEstudiante, BorderLayout.NORTH);
        contenido.add(Ui.rejilla(2,
                Ui.conEtiqueta("Fecha * (dd/mm/aaaa)", tfFecha),
                Ui.conEtiqueta("Hora * (formato 24 h, por ejemplo 09:30)", tfHora),
                Ui.conEtiqueta("Motivo * (máximo 255 caracteres)", tfMotivo)), BorderLayout.CENTER);
        Tarjeta tarjeta = new Tarjeta(new BorderLayout());
        tarjeta.add(contenido, BorderLayout.NORTH);
        p.add(tarjeta, BorderLayout.CENTER);

        BotonSimple guardar = new BotonSimple("Agendar cita", BotonSimple.Tipo.PRIMARIO);
        BotonSimple cancelar = new BotonSimple("Cancelar", BotonSimple.Tipo.SECUNDARIO);
        guardar.addActionListener(e -> guardarCita());
        cancelar.addActionListener(e -> cartas.show(cuerpo, LISTA));
        JPanel sur = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        sur.setOpaque(false);
        sur.add(guardar);
        sur.add(cancelar);
        p.add(sur, BorderLayout.SOUTH);
        return p;
    }

    private void abrirCita() {
        Object[] f = seleccionada();
        if (f == null) {
            return;
        }
        alertaCita = f;
        lblEstudiante.setText(f[1] + (f[2] == null ? "" : "  ·  Curso " + f[2]));
        tfFecha.setText(Formato.texto(LocalDate.now()));
        tfHora.setText("");
        tfMotivo.setText("Seguimiento de alerta de deserción");
        barraCita.limpiar();
        barraLista.limpiar();
        cartas.show(cuerpo, CITA);
        tfHora.requestFocusInWindow();
    }

    private void guardarCita() {
        try {
            LocalDate fecha = Contexto.fechaCita(tfFecha.getText());
            String hora = Contexto.horaCita(tfHora.getText());
            String motivo = Contexto.motivoCita(tfMotivo.getText());
            int idPsico = Contexto.idPsicosocial();
            if (citaDAO.hayConflicto(idPsico, fecha, hora)) {
                barraCita.error("Ya tienes una cita agendada el " + Formato.texto(fecha) + " a las " + hora + ".");
                return;
            }
            CitaPsicosocial c = new CitaPsicosocial();
            c.setIdEstudiante((Integer) alertaCita[10]);
            c.setIdPsicosocial(idPsico);
            c.setIdAlerta((Integer) alertaCita[0]);
            c.setFecha(fecha);
            c.setHora(hora);
            c.setMotivo(motivo);
            citaDAO.agendar(c);
            cartas.show(cuerpo, LISTA);
            recargar();
            barraLista.exito("Cita agendada para el " + Formato.texto(fecha) + " a las " + hora + ".");
        } catch (IllegalArgumentException | IllegalStateException ex) {
            barraCita.error(ex.getMessage());
        } catch (DatosException ex) {
            barraCita.error("No se pudo agendar la cita: " + ex.getMessage());
        }
    }
}
