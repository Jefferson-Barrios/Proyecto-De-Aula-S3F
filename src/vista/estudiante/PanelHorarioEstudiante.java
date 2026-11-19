package vista.estudiante;

import java.awt.FlowLayout;
import vista.componentes.BotonExportar;
import eipi.util.TablaExportable;
import dao.HorarioDAO;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import eipi.util.DatosException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import vista.Refrescable;
import vista.componentes.BarraMensaje;
import vista.componentes.Estilo;
import vista.componentes.Tarjeta;
import vista.componentes.Ui;

public class PanelHorarioEstudiante extends JPanel implements Refrescable {

    private static final List<String> DIAS = Arrays.asList("Lunes", "Martes", "Miércoles", "Jueves", "Viernes",
            "Sábado");

    private final BarraMensaje barra = new BarraMensaje();
    private final JTable tabla = new JTable();

    public PanelHorarioEstudiante() {
        setLayout(new BorderLayout(0, 16));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(28, 32, 28, 32));

        JPanel norte = new JPanel();
        norte.setOpaque(false);
        norte.setLayout(new BoxLayout(norte, BoxLayout.Y_AXIS));
        JLabel t = Ui.titulo("Mi horario");
        JLabel s = Ui.subtitulo("Tus clases de la semana");
        t.setAlignmentX(LEFT_ALIGNMENT);
        s.setAlignmentX(LEFT_ALIGNMENT);
        barra.setAlignmentX(LEFT_ALIGNMENT);
        norte.add(t);
        norte.add(s);
        norte.add(barra);
        add(norte, BorderLayout.NORTH);

        Ui.estilizarTabla(tabla);
        tabla.setRowHeight(64);
        tabla.setRowSelectionAllowed(false);
        Tarjeta tarjeta = new Tarjeta(new BorderLayout());
        tarjeta.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        tarjeta.add(Ui.scroll(tabla), BorderLayout.CENTER);
        add(tarjeta, BorderLayout.CENTER);

        BotonExportar exportar = new BotonExportar(this::tablaParaExportar, barra);
        JPanel pie = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        pie.setOpaque(false);
        pie.add(exportar);
        add(pie, BorderLayout.SOUTH);
    }

    private List<Object[]> ultimasFranjas = new ArrayList<>();

    private TablaExportable tablaParaExportar() {
        List<Object[]> orden = new ArrayList<>(ultimasFranjas);
        orden.sort((a, b) -> {
            int c = Integer.compare(DIAS.indexOf(String.valueOf(a[0])), DIAS.indexOf(String.valueOf(b[0])));
            return c != 0 ? c : String.valueOf(a[1]).compareTo(String.valueOf(b[1]));
        });
        List<String[]> filas = new ArrayList<>();
        for (Object[] f : orden) {
            filas.add(new String[] { String.valueOf(f[0]), f[1] + " - " + f[2], String.valueOf(f[3]),
                    String.valueOf(f[4]), f[5] == null ? "" : String.valueOf(f[5]) });
        }
        modelo.Usuario u = eipi.util.Sesion.getUsuarioActual();
        return new TablaExportable("Horario de clases", u == null ? "" : u.getNombre(),
                new String[] { "Día", "Hora", "Materia", "Docente", "Aula" }, filas);
    }

    public void mostrar(List<Object[]> franjas) {
        ultimasFranjas = new ArrayList<>(franjas);
        boolean haySabado = false;
        for (Object[] f : franjas) {
            if ("Sábado".equals(f[0])) {
                haySabado = true;
            }
        }
        List<String> dias = new ArrayList<>(DIAS.subList(0, haySabado ? 6 : 5));

        Map<String, Object[]> filasPorHora = new TreeMap<>();
        for (Object[] f : franjas) {
            String hora = f[1] + " - " + f[2];
            Object[] fila = filasPorHora.get(hora);
            if (fila == null) {
                fila = new Object[dias.size() + 1];
                fila[0] = hora;
                filasPorHora.put(hora, fila);
            }
            int col = dias.indexOf(String.valueOf(f[0])) + 1;
            if (col > 0) {
                String celda = "<b>" + f[3] + "</b><br>" + f[4] + (f[5] == null ? "" : " · " + f[5]);
                fila[col] = fila[col] == null ? celda : fila[col] + "<hr>" + celda;
            }
        }

        String[] encabezados = new String[dias.size() + 1];
        encabezados[0] = "Hora";
        for (int i = 0; i < dias.size(); i++) {
            encabezados[i + 1] = dias.get(i);
        }
        DefaultTableModel modelo = new DefaultTableModel(encabezados, 0) {
            @Override
            public boolean isCellEditable(int fila, int col) {
                return false;
            }
        };
        for (Object[] f : filasPorHora.values()) {
            modelo.addRow(f);
        }
        tabla.setModel(modelo);

        DefaultTableCellRenderer render = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object valor, boolean sel,
                    boolean foco, int fila, int col) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, null, false, false, fila, col);
                l.setFont(Estilo.fuentePx(Estilo.REGULAR, 12f));
                l.setVerticalAlignment(SwingConstants.TOP);
                l.setBorder(BorderFactory.createEmptyBorder(8, 10, 4, 6));
                l.setForeground(Estilo.TEXTO);
                if (col == 0) {
                    l.setText(valor == null ? "" : String.valueOf(valor));
                    l.setFont(Estilo.fuentePx(Estilo.MEDIO, 12f));
                    l.setBackground(Color.WHITE);
                } else if (valor == null) {
                    l.setText("");
                    l.setBackground(Color.WHITE);
                } else {
                    l.setText("<html>" + valor + "</html>");
                    l.setBackground(new Color(0xEAF0FF));
                }
                l.setOpaque(true);
                return l;
            }
        };
        for (int i = 0; i < tabla.getColumnCount(); i++) {
            tabla.getColumnModel().getColumn(i).setCellRenderer(render);
            tabla.getColumnModel().getColumn(i).setPreferredWidth(i == 0 ? 110 : 170);
        }
    }

    @Override
    public void refrescar() {
        try {
            List<Object[]> franjas = new HorarioDAO().listarDeEstudiante(Contexto.idEstudiante());
            mostrar(franjas);
            if (franjas.isEmpty()) {
                barra.exito("Tu curso aún no tiene horario publicado.");
            } else {
                barra.limpiar();
            }
        } catch (IllegalStateException ex) {
            barra.error(ex.getMessage());
        } catch (DatosException ex) {
            barra.error("No se pudo cargar el horario. Revisa la carpeta datos del proyecto. ("
                    + ex.getMessage() + ")");
        }
    }
}
