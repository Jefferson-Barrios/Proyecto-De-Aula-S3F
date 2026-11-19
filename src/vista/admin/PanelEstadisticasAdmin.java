package vista.admin;

import dao.BalanceDAO;
import dao.EstadisticasDAO;
import dao.InstitucionDAO;
import eipi.util.TablaExportable;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import eipi.util.DatosException;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import modelo.Institucion;
import vista.Refrescable;
import vista.componentes.BarraMensaje;
import vista.componentes.BotonExportar;
import vista.componentes.Estilo;
import vista.componentes.GraficoBarras;
import vista.componentes.GraficoDona;
import vista.componentes.GraficoLinea;
import vista.componentes.Opcion;
import vista.componentes.PanelAjustado;
import vista.componentes.Tarjeta;
import vista.componentes.Ui;

public class PanelEstadisticasAdmin extends JPanel implements Refrescable {

    private static final Color NARANJA = new Color(0xC47A00);
    private static final Locale ES = Locale.forLanguageTag("es-CO");
    private static final int MESES = 6;

    public static class Datos {
        public boolean global;
        public Institucion institucion;
        public int instituciones;
        public int estudiantes;
        public int docentes;
        public int cursos;
        public int conAlertaAbierta;
        public Map<String, Integer> roles = new LinkedHashMap<>();
        public Map<String, Integer> nivel = new LinkedHashMap<>();
        public Map<String, Integer> estado = new LinkedHashMap<>();
        public Map<String, Integer> citas = new LinkedHashMap<>();
        public Map<String, Integer> meses = new LinkedHashMap<>();
        public Map<String, Integer> curso = new LinkedHashMap<>();
        public Map<String, Integer> estudiantesPorInstitucion = new LinkedHashMap<>();
        public Map<String, Integer> alertasPorInstitucion = new LinkedHashMap<>();

        public List<Object[]> comparativo = new ArrayList<>();
    }

    private final EstadisticasDAO dao = new EstadisticasDAO();
    private final BarraMensaje barra = new BarraMensaje();
    private final JTextField tfBuscar = Ui.campo();
    private final JComboBox<Opcion> cbInstitucion = Ui.combo();
    private final JLabel lblInfo = Ui.subtitulo(" ");
    private List<Institucion> instituciones = new ArrayList<>();
    private boolean cargando = false;
    private Datos ultimo;

    private final JLabel lblPrimeraValor = valor();
    private final JLabel lblPrimeraTexto = Ui.subtitulo("Instituciones activas");
    private final JLabel lblEstudiantes = valor();
    private final JLabel lblDocentes = valor();
    private final JLabel lblAlertas = valor();
    private final JLabel lblPendientes = valor();
    private final JLabel lblRiesgo = valor();
    private final JLabel lblRiesgoTexto = Ui.subtitulo("Estudiantes con alerta abierta");

    private final GraficoDona gRoles = new GraficoDona()
            .colorDe("Estudiantes", Estilo.AZUL)
            .colorDe("Docentes", Estilo.VERDE)
            .colorDe("Directivas", new Color(0x7B61A8))
            .colorDe("Psicosocial", NARANJA);
    private final GraficoDona gNivel = new GraficoDona()
            .colorDe("ALTO", Estilo.ROJO).colorDe("MEDIO", NARANJA).colorDe("PREVENTIVA", new Color(0x2E7DD1));
    private final GraficoDona gEstado = new GraficoDona()
            .colorDe("PENDIENTE", NARANJA).colorDe("EN SEGUIMIENTO", Estilo.AZUL)
            .colorDe("CERRADA", new Color(0x9AA3B5));
    private final GraficoDona gCitas = new GraficoDona()
            .colorDe("AGENDADA", Estilo.AZUL).colorDe("REALIZADA", Estilo.VERDE).colorDe("CANCELADA", Estilo.ROJO);
    private final GraficoLinea gMeses = new GraficoLinea().colorLinea(Estilo.AZUL);
    private final GraficoBarras gCurso = new GraficoBarras();
    private final GraficoBarras gEstudiantesInst = new GraficoBarras();
    private final GraficoBarras gAlertasInst = new GraficoBarras().colorBase(Estilo.ROJO);
    private final Tarjeta tNivel = tarjetaGrafico("Alertas por nivel de riesgo", gNivel);
    private final Tarjeta tEstado = tarjetaGrafico("Alertas por estado", gEstado);
    private final Tarjeta tRoles = tarjetaGrafico("Usuarios activos por rol", gRoles);
    private final Tarjeta tCitas = tarjetaGrafico("Citas por estado", gCitas);
    private final Tarjeta tMeses = tarjetaGrafico("Alertas por mes (últimos " + MESES + " meses)", gMeses);
    private final Tarjeta tCurso = tarjetaGrafico("Cursos con más alertas", gCurso);
    private final Tarjeta tEstudiantesInst = tarjetaGrafico("Instituciones con más estudiantes", gEstudiantesInst);
    private final Tarjeta tAlertasInst = tarjetaGrafico("Instituciones con más alertas", gAlertasInst);
    private final JPanel panelGraficos = new JPanel();

    private final DefaultTableModel modeloComparativo = new DefaultTableModel(
            new Object[] { "Institución", "Estado", "Estudiantes", "Docentes", "Alertas", "Pendientes",
                    "Nivel alto", "% con alerta" },
            0) {
        @Override
        public boolean isCellEditable(int fila, int col) {
            return false;
        }
    };
    private final JTable tablaComparativo = new JTable(modeloComparativo);
    private final Tarjeta tarjetaComparativo = new Tarjeta(new BorderLayout(0, 8));
    private List<Object[]> filasComparativo = new ArrayList<>();

    public PanelEstadisticasAdmin() {
        setLayout(new BorderLayout(0, 16));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(28, 32, 28, 32));

        JPanel textos = new JPanel();
        textos.setOpaque(false);
        textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));
        JLabel t = Ui.titulo("Estadísticas");
        JLabel s = Ui.subtitulo("Panorama de matrícula, alertas de riesgo y acompañamiento");
        t.setAlignmentX(LEFT_ALIGNMENT);
        s.setAlignmentX(LEFT_ALIGNMENT);
        barra.setAlignmentX(LEFT_ALIGNMENT);
        textos.add(t);
        textos.add(s);
        textos.add(barra);

        BotonExportar exportar = new BotonExportar(this::paraExportar, barra);
        JPanel derecha = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        derecha.setOpaque(false);
        derecha.add(exportar);
        JPanel cabecera = new JPanel(new BorderLayout());
        cabecera.setOpaque(false);
        cabecera.add(textos, BorderLayout.CENTER);
        cabecera.add(derecha, BorderLayout.EAST);

        Tarjeta selector = new Tarjeta(new BorderLayout(0, 4));
        selector.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        selector.add(Ui.rejilla(2,
                Ui.conEtiqueta("Institución", cbInstitucion),
                Ui.conEtiqueta("Buscar por nombre o código", tfBuscar)), BorderLayout.NORTH);
        lblInfo.setBorder(BorderFactory.createEmptyBorder(0, 8, 4, 0));
        selector.add(lblInfo, BorderLayout.SOUTH);

        JPanel norte = new JPanel(new BorderLayout(0, 16));
        norte.setOpaque(false);
        norte.add(cabecera, BorderLayout.NORTH);
        norte.add(selector, BorderLayout.CENTER);
        add(norte, BorderLayout.NORTH);

        JPanel cifras = new JPanel(new GridLayout(0, 3, 16, 16));
        cifras.setOpaque(false);
        cifras.add(tarjetaCifra(lblPrimeraValor, lblPrimeraTexto, Estilo.AZUL));
        cifras.add(tarjetaCifra(lblEstudiantes, Ui.subtitulo("Estudiantes activos"), Estilo.VERDE));
        cifras.add(tarjetaCifra(lblDocentes, Ui.subtitulo("Docentes activos"), new Color(0x7B61A8)));
        cifras.add(tarjetaCifra(lblAlertas, Ui.subtitulo("Alertas en total"), Estilo.ROJO));
        cifras.add(tarjetaCifra(lblPendientes, Ui.subtitulo("Alertas pendientes por atender"), NARANJA));
        cifras.add(tarjetaCifra(lblRiesgo, lblRiesgoTexto, new Color(0x2E7DD1)));

        panelGraficos.setOpaque(false);
        panelGraficos.setLayout(new BoxLayout(panelGraficos, BoxLayout.Y_AXIS));

        Ui.estilizarTabla(tablaComparativo);

        int[] an = { 340, 108, 112, 100, 82, 105, 98, 114 };
        for (int i = 0; i < an.length; i++) {
            tablaComparativo.getColumnModel().getColumn(i).setPreferredWidth(an[i]);
            if (i > 0) {
                tablaComparativo.getColumnModel().getColumn(i).setMinWidth(an[i]);
            }
        }
        configurarOrden();
        tablaComparativo.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int fila = tablaComparativo.getSelectedRow();
                if (e.getClickCount() == 2 && fila >= 0) {
                    abrir(filasComparativo.get(tablaComparativo.convertRowIndexToModel(fila)));
                }
            }
        });
        JLabel tituloComparativo = Ui
                .etiqueta("Comparativo por institución  (doble clic en una fila para ver solo esa institución)");
        tituloComparativo.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 0));
        tarjetaComparativo.setBorder(BorderFactory.createEmptyBorder(16, 12, 12, 12));
        tarjetaComparativo.add(tituloComparativo, BorderLayout.NORTH);
        JPanel tabla = new JPanel(new BorderLayout());
        tabla.setOpaque(false);
        tabla.add(tablaComparativo.getTableHeader(), BorderLayout.NORTH);
        tabla.add(tablaComparativo, BorderLayout.CENTER);
        tarjetaComparativo.add(tabla, BorderLayout.CENTER);

        JPanel contenido = new JPanel();
        contenido.setOpaque(false);
        contenido.setLayout(new BoxLayout(contenido, BoxLayout.Y_AXIS));
        for (JPanel p : new JPanel[] { cifras, panelGraficos, tarjetaComparativo }) {
            p.setAlignmentX(LEFT_ALIGNMENT);
        }
        contenido.add(cifras);
        contenido.add(Box.createVerticalStrut(16));
        contenido.add(panelGraficos);
        contenido.add(Box.createVerticalStrut(16));
        contenido.add(tarjetaComparativo);
        JPanel envoltura = new PanelAjustado(new BorderLayout());
        envoltura.add(contenido, BorderLayout.NORTH);
        JScrollPane sp = new JScrollPane(envoltura);
        sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.getVerticalScrollBar().setUnitIncrement(16);
        add(sp, BorderLayout.CENTER);

        cbInstitucion.addActionListener(e -> {
            if (!cargando) {
                cargar();
            }
        });
        tfBuscar.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                alBuscar();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                alBuscar();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                alBuscar();
            }
        });
    }

    private static JLabel valor() {
        JLabel l = new JLabel("-");
        l.setFont(Estilo.fuentePx(Estilo.NEGRITA, 34f));
        l.setForeground(Estilo.AZUL);
        return l;
    }

    private static Tarjeta tarjetaCifra(JLabel valor, JLabel texto, Color acento) {
        Tarjeta t = new Tarjeta(new BorderLayout(12, 0));
        t.setBorder(BorderFactory.createEmptyBorder(15, 18, 15, 18));
        JPanel franja = new JPanel();
        franja.setBackground(acento);
        franja.setPreferredSize(new Dimension(4, 1));
        JPanel datos = new JPanel();
        datos.setOpaque(false);
        datos.setLayout(new BoxLayout(datos, BoxLayout.Y_AXIS));
        texto.setAlignmentX(LEFT_ALIGNMENT);
        valor.setAlignmentX(LEFT_ALIGNMENT);
        valor.setForeground(acento);
        datos.add(texto);
        datos.add(Box.createVerticalStrut(5));
        datos.add(valor);
        t.add(franja, BorderLayout.WEST);
        t.add(datos, BorderLayout.CENTER);
        return t;
    }

    private static Tarjeta tarjetaGrafico(String titulo, JComponent grafico) {
        Tarjeta t = new Tarjeta(new BorderLayout(0, 8));
        t.setBorder(BorderFactory.createEmptyBorder(15, 18, 14, 18));
        t.add(Ui.etiqueta(titulo), BorderLayout.NORTH);
        JPanel envoltura = new JPanel(new BorderLayout());
        envoltura.setOpaque(false);
        envoltura.add(grafico, BorderLayout.NORTH);
        t.add(envoltura, BorderLayout.CENTER);
        return t;
    }

    private void configurarOrden() {
        TableRowSorter<DefaultTableModel> orden = new TableRowSorter<>(modeloComparativo);
        Comparator<Object> numerico = Comparator.comparingDouble(PanelEstadisticasAdmin::numero);
        for (int c = 2; c < modeloComparativo.getColumnCount(); c++) {
            orden.setComparator(c, numerico);
        }
        tablaComparativo.setRowSorter(orden);
    }

    private static double numero(Object o) {
        try {
            return Double.parseDouble(String.valueOf(o).replaceAll("[^0-9,.-]", "").replace(',', '.'));
        } catch (NumberFormatException ex) {
            return -1;
        }
    }

    private static String porcentaje(double v) {
        return String.format(ES, "%.1f %%", v);
    }

    private static String plano(String s) {
        if (s == null) {
            return "";
        }
        return Normalizer.normalize(s, Normalizer.Form.NFD).replaceAll("\\p{M}", "").toLowerCase(ES).trim();
    }

    private Integer idSeleccionado() {
        Opcion op = (Opcion) cbInstitucion.getSelectedItem();
        return op == null || op.getId() == 0 ? null : op.getId();
    }

    private boolean reconstruirLista() {
        Integer anterior = idSeleccionado();
        String filtro = plano(tfBuscar.getText());
        cargando = true;
        try {
            cbInstitucion.removeAllItems();
            cbInstitucion.addItem(new Opcion(0, "Todas las instituciones", null));
            Opcion previa = null;
            Opcion unica = null;
            int coincidencias = 0;
            for (Institucion i : instituciones) {
                if (filtro.isEmpty() || plano(i.getNombre()).contains(filtro)
                        || plano(i.getCodigoOficial()).contains(filtro)) {
                    Opcion o = new Opcion(i.getIdInstitucion(),
                            i.getNombre() + ("ACTIVO".equals(i.getEstado()) ? "" : " (inactiva)"), i);
                    cbInstitucion.addItem(o);
                    coincidencias++;
                    unica = o;
                    if (anterior != null && anterior == i.getIdInstitucion()) {
                        previa = o;
                    }
                }
            }
            if (!filtro.isEmpty() && coincidencias == 1) {
                cbInstitucion.setSelectedItem(unica);
            } else if (previa != null) {
                cbInstitucion.setSelectedItem(previa);
            } else {
                cbInstitucion.setSelectedIndex(0);
            }
        } finally {
            cargando = false;
        }
        Integer actual = idSeleccionado();
        return anterior == null ? actual != null : !anterior.equals(actual);
    }

    private void alBuscar() {
        if (cargando) {
            return;
        }
        if (reconstruirLista()) {
            cargar();
        }
    }

    private void abrir(Object[] fila) {
        int id = (Integer) fila[0];
        cargando = true;
        tfBuscar.setText("");
        cargando = false;

        Integer anterior = idSeleccionado();
        cargando = true;
        try {
            cbInstitucion.removeAllItems();
            cbInstitucion.addItem(new Opcion(0, "Todas las instituciones", null));
            Opcion elegida = null;
            for (Institucion i : instituciones) {
                Opcion o = new Opcion(i.getIdInstitucion(),
                        i.getNombre() + ("ACTIVO".equals(i.getEstado()) ? "" : " (inactiva)"), i);
                cbInstitucion.addItem(o);
                if (i.getIdInstitucion() == id) {
                    elegida = o;
                }
            }
            if (elegida != null) {
                cbInstitucion.setSelectedItem(elegida);
            }
        } finally {
            cargando = false;
        }
        if (anterior == null || anterior != id) {
            cargar();
        }
    }

    @Override
    public void refrescar() {
        try {
            instituciones = new InstitucionDAO().listar();
        } catch (DatosException ex) {
            barra.error("No se pudo cargar las instituciones. Revisa la carpeta datos del proyecto. ("
                    + ex.getMessage() + ")");
            return;
        }
        reconstruirLista();
        cargar();
    }

    private void cargar() {
        Opcion op = (Opcion) cbInstitucion.getSelectedItem();
        Integer inst = idSeleccionado();
        try {
            Datos d = new Datos();
            d.global = inst == null;
            d.institucion = inst == null || op == null ? null : (Institucion) op.getDato();
            d.roles = dao.usuariosPorRol(inst);
            d.nivel = dao.alertasPorNivel(inst);
            d.estado = dao.alertasPorEstado(inst);
            d.citas = dao.citasPorEstado(inst);
            d.meses = dao.alertasPorMes(inst, MESES);
            d.estudiantes = d.roles.getOrDefault("ESTUDIANTE", 0);
            d.docentes = d.roles.getOrDefault("DOCENTE", 0);
            d.conAlertaAbierta = dao.estudiantesConAlertaAbierta(inst);
            if (d.global) {
                d.instituciones = new BalanceDAO().contarInstitucionesActivas();
                d.estudiantesPorInstitucion = dao.estudiantesPorInstitucion();
                d.alertasPorInstitucion = dao.alertasPorInstitucion();
                d.comparativo = dao.comparativo();
            } else {
                d.cursos = dao.contarCursos(inst);
                d.curso = dao.alertasPorCurso(inst);
            }
            mostrar(d);
            barra.limpiar();
        } catch (DatosException ex) {
            barra.error("No se pudieron cargar las estadísticas. Revisa la carpeta datos del proyecto. ("
                    + ex.getMessage() + ")");
        }
    }

    public void mostrar(Datos d) {
        ultimo = d;
        int totalAlertas = 0;
        for (int v : d.nivel.values()) {
            totalAlertas += v;
        }

        if (d.global) {
            lblPrimeraValor.setText(String.valueOf(d.instituciones));
            lblPrimeraTexto.setText("Instituciones activas");
            lblInfo.setText("Vista general: se suman todas las instituciones registradas.");
        } else {
            lblPrimeraValor.setText(String.valueOf(d.cursos));
            lblPrimeraTexto.setText("Cursos");
            Institucion i = d.institucion;
            lblInfo.setText(i == null ? " "
                    : TablaExportable.unir("Código " + i.getCodigoOficial(),
                            i.getNombreRector() == null ? null : "Rector: " + i.getNombreRector(),
                            i.getSedePrincipal() == null ? null : "Sede: " + i.getSedePrincipal(),
                            "Estado: " + i.getEstado()));
        }
        lblEstudiantes.setText(String.valueOf(d.estudiantes));
        lblDocentes.setText(String.valueOf(d.docentes));
        lblAlertas.setText(String.valueOf(totalAlertas));
        lblPendientes.setText(String.valueOf(d.estado.getOrDefault("PENDIENTE", 0)));
        double tasa = d.estudiantes == 0 ? 0 : d.conAlertaAbierta * 100.0 / d.estudiantes;
        lblRiesgo.setText(porcentaje(tasa));
        lblRiesgoTexto.setText("Estudiantes en alerta (" + d.conAlertaAbierta + ")");
        lblRiesgoTexto.setToolTipText("Estudiantes con alerta abierta: " + d.conAlertaAbierta);

        Map<String, Integer> roles = new LinkedHashMap<>();
        roles.put("Estudiantes", d.roles.getOrDefault("ESTUDIANTE", 0));
        roles.put("Docentes", d.roles.getOrDefault("DOCENTE", 0));
        roles.put("Directivas", d.roles.getOrDefault("DIRECTIVA", 0));
        roles.put("Psicosocial", d.roles.getOrDefault("PSICOSOCIAL", 0));
        gRoles.setDatos(roles);
        gNivel.setDatos(d.nivel);
        gEstado.setDatos(d.estado);
        gCitas.setDatos(d.citas);
        gMeses.setDatos(d.meses);
        gCurso.setDatos(d.curso);
        gEstudiantesInst.setDatos(d.estudiantesPorInstitucion);
        gAlertasInst.setDatos(d.alertasPorInstitucion);

        List<Tarjeta> visibles = new ArrayList<>();
        visibles.add(tNivel);
        visibles.add(tEstado);
        visibles.add(tRoles);
        visibles.add(tCitas);
        visibles.add(tMeses);
        if (d.global) {
            visibles.add(tEstudiantesInst);
            visibles.add(tAlertasInst);
        } else {
            visibles.add(tCurso);
        }
        panelGraficos.removeAll();
        for (int i = 0; i < visibles.size(); i += 2) {
            JPanel fila = new JPanel(new GridLayout(1, 2, 16, 0));
            fila.setOpaque(false);
            fila.setAlignmentX(LEFT_ALIGNMENT);
            fila.add(visibles.get(i));
            if (i + 1 < visibles.size()) {
                fila.add(visibles.get(i + 1));
            } else {
                JPanel hueco = new JPanel();
                hueco.setOpaque(false);
                fila.add(hueco);
            }
            if (i > 0) {
                panelGraficos.add(Box.createVerticalStrut(16));
            }
            panelGraficos.add(fila);
        }

        tarjetaComparativo.setVisible(d.global);
        filasComparativo = new ArrayList<>(d.comparativo);
        modeloComparativo.setRowCount(0);
        for (Object[] f : filasComparativo) {
            int est = (Integer) f[3];
            int abiertas = (Integer) f[8];
            modeloComparativo.addRow(new Object[] { f[1], f[2], f[3], f[4], f[5], f[6], f[7],
                    porcentaje(est == 0 ? 0 : abiertas * 100.0 / est) });
        }
        tablaComparativo.setPreferredSize(null);
        panelGraficos.revalidate();
        revalidate();
        repaint();
    }

    private TablaExportable paraExportar() {
        Datos d = ultimo;
        if (d == null) {
            throw new IllegalStateException("Aún no hay datos para exportar.");
        }
        if (d.global) {
            return TablaExportable.de("Estadísticas generales", "Comparativo de instituciones", modeloComparativo)
                    .conColumnasNumericas(2, 3, 4, 5, 6);
        }
        List<String[]> filas = new ArrayList<>();
        int totalAlertas = 0;
        for (int v : d.nivel.values()) {
            totalAlertas += v;
        }
        double tasa = d.estudiantes == 0 ? 0 : d.conAlertaAbierta * 100.0 / d.estudiantes;
        filas.add(new String[] { "Estudiantes activos", String.valueOf(d.estudiantes) });
        filas.add(new String[] { "Docentes activos", String.valueOf(d.docentes) });
        filas.add(new String[] { "Cursos", String.valueOf(d.cursos) });
        filas.add(new String[] { "Alertas en total", String.valueOf(totalAlertas) });
        filas.add(new String[] { "Estudiantes con alerta abierta",
                d.conAlertaAbierta + " (" + porcentaje(tasa) + ")" });
        agregar(filas, "Alertas por nivel", d.nivel);
        agregar(filas, "Alertas por estado", d.estado);
        agregar(filas, "Citas", d.citas);
        agregar(filas, "Alertas por mes", d.meses);
        agregar(filas, "Alertas por curso", d.curso);
        Institucion i = d.institucion;
        String nombre = i == null ? "" : i.getNombre();
        return new TablaExportable("Estadísticas - " + nombre,
                i == null ? "" : "Código " + i.getCodigoOficial(),
                new String[] { "Indicador", "Valor" }, filas).conColumnasNumericas(1);
    }

    private static void agregar(List<String[]> filas, String grupo, Map<String, Integer> datos) {
        for (Map.Entry<String, Integer> e : datos.entrySet()) {
            filas.add(new String[] { grupo + " · " + e.getKey(), String.valueOf(e.getValue()) });
        }
    }
}
