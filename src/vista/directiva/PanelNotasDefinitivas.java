package vista.directiva;

import java.awt.FlowLayout;
import vista.componentes.BotonExportar;
import eipi.util.TablaExportable;
import dao.CursoDAO;
import dao.NotaPeriodoDAO;
import dao.PeriodoDAO;
import eipi.util.Formato;
import java.awt.BorderLayout;
import java.awt.Component;
import eipi.util.DatosException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import modelo.Curso;
import modelo.PeriodoAcademico;
import vista.Refrescable;
import vista.componentes.BarraMensaje;
import vista.componentes.Estilo;
import vista.componentes.Opcion;
import vista.componentes.Tarjeta;
import vista.componentes.Ui;

public class PanelNotasDefinitivas extends JPanel implements Refrescable {

    private static final double NOTA_MINIMA = 3.0;

    private final BarraMensaje barra = new BarraMensaje();
    private final JComboBox<Opcion> cbPeriodo = Ui.combo();
    private final JComboBox<Opcion> cbCurso = Ui.combo();
    private final JTable tabla = new JTable();
    private final JLabel lblResumen = Ui.subtitulo(" ");
    private boolean cargando = false;

    public PanelNotasDefinitivas() {
        setLayout(new BorderLayout(0, 16));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(28, 32, 28, 32));

        JPanel norte = new JPanel();
        norte.setOpaque(false);
        norte.setLayout(new BoxLayout(norte, BoxLayout.Y_AXIS));
        JLabel t = Ui.titulo("Notas definitivas");
        JLabel s = Ui.subtitulo("Las define cada docente al guardar las notas de su materia en el período");
        t.setAlignmentX(LEFT_ALIGNMENT);
        s.setAlignmentX(LEFT_ALIGNMENT);
        barra.setAlignmentX(LEFT_ALIGNMENT);
        norte.add(t);
        norte.add(s);
        norte.add(barra);
        add(norte, BorderLayout.NORTH);

        Ui.estilizarTabla(tabla);
        Tarjeta tarjeta = new Tarjeta(new BorderLayout(0, 8));
        tarjeta.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        tarjeta.add(Ui.rejilla(2, Ui.conEtiqueta("Período", cbPeriodo), Ui.conEtiqueta("Curso", cbCurso)),
                BorderLayout.NORTH);
        tarjeta.add(Ui.scroll(tabla), BorderLayout.CENTER);
        lblResumen.setBorder(BorderFactory.createEmptyBorder(4, 8, 0, 0));
        tarjeta.add(lblResumen, BorderLayout.SOUTH);
        add(tarjeta, BorderLayout.CENTER);

        BotonExportar exportar = new BotonExportar(
                () -> TablaExportable.de("Notas definitivas",
                        TablaExportable.unir(cbPeriodo.getSelectedItem(),
                                cbCurso.getSelectedItem() == null ? null : "Curso " + cbCurso.getSelectedItem()),
                        tabla.getModel()),
                barra);
        JPanel pie = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        pie.setOpaque(false);
        pie.add(exportar);
        add(pie, BorderLayout.SOUTH);

        cbPeriodo.addActionListener(e -> alCambiar());
        cbCurso.addActionListener(e -> alCambiar());
    }

    private void alCambiar() {
        if (!cargando && cbPeriodo.getItemCount() > 0 && cbCurso.getItemCount() > 0) {
            cargarNotas();
        }
    }

    private static int idInstitucion() {
        modelo.Usuario u = eipi.util.Sesion.getUsuarioActual();
        if (u == null || u.getIdInstitucion() == null) {
            throw new IllegalStateException("Tu usuario no está asociado a ninguna institución.");
        }
        return u.getIdInstitucion();
    }

    public void mostrar(NotaPeriodoDAO.Matriz m) {
        List<String> columnas = new ArrayList<>();
        columnas.add("Estudiante");
        columnas.addAll(m.materias);
        columnas.add("Promedio");
        DefaultTableModel modelo = new DefaultTableModel(columnas.toArray(), 0) {
            @Override
            public boolean isCellEditable(int fila, int col) {
                return false;
            }
        };

        int conNotas = 0;
        int reprobando = 0;
        for (int i = 0; i < m.estudiantes.size(); i++) {
            Object[] fila = new Object[columnas.size()];
            fila[0] = m.estudiantes.get(i);
            double suma = 0;
            int cuantas = 0;
            for (int j = 0; j < m.materias.size(); j++) {
                Double n = m.notas[i][j];
                fila[j + 1] = n == null ? "—" : Formato.nota(n);
                if (n != null) {
                    suma += n;
                    cuantas++;
                }
            }
            fila[columnas.size() - 1] = cuantas == 0 ? "—" : Formato.nota(suma / cuantas);
            if (cuantas > 0) {
                conNotas++;
                if (suma / cuantas < NOTA_MINIMA) {
                    reprobando++;
                }
            }
            modelo.addRow(fila);
        }
        tabla.setModel(modelo);

        DefaultTableCellRenderer render = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object valor, boolean sel,
                    boolean foco, int fila, int col) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, valor, sel, false, fila, col);
                l.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
                l.setFont(Estilo.fuentePx(Estilo.REGULAR, 13f));
                l.setForeground(Estilo.TEXTO);
                l.setHorizontalAlignment(SwingConstants.LEFT);
                String texto = valor == null ? "" : String.valueOf(valor);
                if (col > 0 && !"—".equals(texto)) {
                    try {
                        double nota = Double.parseDouble(texto.replace(',', '.'));
                        if (nota < NOTA_MINIMA) {
                            l.setForeground(Estilo.ROJO);
                            l.setFont(Estilo.fuentePx(Estilo.MEDIO, 13f));
                        }
                    } catch (NumberFormatException ex) {

                    }
                }
                if (col == t.getColumnCount() - 1) {
                    l.setFont(Estilo.fuentePx(Estilo.NEGRITA, 13f));
                }
                return l;
            }
        };
        for (int i = 0; i < tabla.getColumnCount(); i++) {
            tabla.getColumnModel().getColumn(i).setCellRenderer(render);
            tabla.getColumnModel().getColumn(i).setPreferredWidth(i == 0 ? 240 : 110);
        }

        lblResumen.setText(m.estudiantes.size() + " estudiante(s)  ·  " + conNotas + " con notas definitivas  ·  "
                + reprobando + " con promedio menor a " + Formato.nota(NOTA_MINIMA));
    }

    private void cargarNotas() {
        Opcion per = (Opcion) cbPeriodo.getSelectedItem();
        Opcion cur = (Opcion) cbCurso.getSelectedItem();
        if (per == null || cur == null) {
            return;
        }
        try {
            mostrar(new NotaPeriodoDAO().matriz(cur.getId(), per.getId()));
            barra.limpiar();
        } catch (DatosException ex) {
            barra.error("No se pudieron cargar las notas: " + ex.getMessage());
        }
    }

    @Override
    public void refrescar() {
        cargando = true;
        try {
            int inst = idInstitucion();
            int periodoAnterior = cbPeriodo.getSelectedItem() == null ? -1
                    : ((Opcion) cbPeriodo.getSelectedItem()).getId();
            int cursoAnterior = cbCurso.getSelectedItem() == null ? -1 : ((Opcion) cbCurso.getSelectedItem()).getId();

            List<PeriodoAcademico> periodos = new PeriodoDAO().listar(inst);
            cbPeriodo.removeAllItems();
            Opcion ultimo = null;
            Opcion previo = null;
            for (PeriodoAcademico p : periodos) {
                Opcion o = new Opcion(p.getIdPeriodo(),
                        p.getNombre() + ("ABIERTO".equals(p.getEstado()) ? "" : " (cerrado)"), p);
                cbPeriodo.addItem(o);
                ultimo = o;
                if (o.getId() == periodoAnterior) {
                    previo = o;
                }
            }
            cbPeriodo.setSelectedItem(previo != null ? previo : ultimo);

            List<Curso> cursos = new CursoDAO().listar(inst);
            cbCurso.removeAllItems();
            Opcion cursoPrevio = null;
            for (Curso c : cursos) {
                Opcion o = new Opcion(c.getIdCurso(), c.getGrado() + "-" + c.getGrupo() + " (" + c.getJornada() + ")",
                        c);
                cbCurso.addItem(o);
                if (o.getId() == cursoAnterior) {
                    cursoPrevio = o;
                }
            }
            if (cursoPrevio != null) {
                cbCurso.setSelectedItem(cursoPrevio);
            }
        } catch (IllegalStateException ex) {
            barra.error(ex.getMessage());
            return;
        } catch (DatosException ex) {
            barra.error("No se pudo cargar la información. Revisa la carpeta datos del proyecto. ("
                    + ex.getMessage() + ")");
            return;
        } finally {
            cargando = false;
        }
        if (cbPeriodo.getItemCount() == 0) {
            barra.error("Primero crea al menos un período en la sección Períodos.");
            return;
        }
        if (cbCurso.getItemCount() == 0) {
            barra.error("Primero crea al menos un curso en la sección Cursos.");
            return;
        }
        cargarNotas();
    }
}
