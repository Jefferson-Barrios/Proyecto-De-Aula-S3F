package vista.psicosocial;

import dao.CitaDAO;
import dao.EstadisticasDAO;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import eipi.util.DatosException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import modelo.Usuario;
import vista.Refrescable;
import vista.componentes.BarraMensaje;
import vista.componentes.Estilo;
import vista.componentes.Tarjeta;
import vista.componentes.Ui;

public class PanelInicioPsico extends JPanel implements Refrescable {

    private final BarraMensaje barra = new BarraMensaje();
    private final JLabel lblPendientes = valor();
    private final JLabel lblSeguimiento = valor();
    private final JLabel lblCitas = valor();
    private final DefaultTableModel modelo = new DefaultTableModel(
            new Object[] { "Fecha", "Hora", "Estudiante", "Curso", "Motivo" }, 0) {
        @Override
        public boolean isCellEditable(int fila, int col) {
            return false;
        }
    };

    public PanelInicioPsico(Usuario usuario) {
        setLayout(new BorderLayout(0, 24));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(28, 32, 28, 32));

        JPanel encabezado = new JPanel();
        encabezado.setOpaque(false);
        encabezado.setLayout(new BoxLayout(encabezado, BoxLayout.Y_AXIS));
        JLabel titulo = Ui.titulo("Hola, " + usuario.getNombre());
        JLabel sub = Ui.subtitulo("Lo que necesita tu atención hoy");
        titulo.setAlignmentX(LEFT_ALIGNMENT);
        sub.setAlignmentX(LEFT_ALIGNMENT);
        barra.setAlignmentX(LEFT_ALIGNMENT);
        encabezado.add(titulo);
        encabezado.add(sub);
        encabezado.add(barra);
        add(encabezado, BorderLayout.NORTH);

        JPanel centro = new JPanel(new BorderLayout(0, 16));
        centro.setOpaque(false);
        JPanel cifras = new JPanel(new GridLayout(1, 3, 16, 16));
        cifras.setOpaque(false);
        cifras.add(tarjeta(lblPendientes, "Alertas pendientes por tomar"));
        cifras.add(tarjeta(lblSeguimiento, "Alertas en seguimiento"));
        cifras.add(tarjeta(lblCitas, "Citas agendadas"));
        centro.add(cifras, BorderLayout.NORTH);

        JTable tabla = new JTable(modelo);
        Ui.estilizarTabla(tabla);
        Tarjeta t = new Tarjeta(new BorderLayout(0, 8));
        t.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        JLabel prox = Ui.etiqueta("Próximas citas");
        prox.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 0));
        t.add(prox, BorderLayout.NORTH);
        t.add(Ui.scroll(tabla), BorderLayout.CENTER);
        centro.add(t, BorderLayout.CENTER);
        add(centro, BorderLayout.CENTER);
    }

    private static JLabel valor() {
        JLabel l = new JLabel("-");
        l.setFont(Estilo.fuentePx(Estilo.NEGRITA, 38f));
        l.setForeground(Estilo.AZUL);
        return l;
    }

    private static Tarjeta tarjeta(JLabel valor, String texto) {
        Tarjeta t = new Tarjeta(new BorderLayout(0, 6));
        t.add(valor, BorderLayout.CENTER);
        t.add(Ui.subtitulo(texto), BorderLayout.SOUTH);
        return t;
    }

    public void mostrar(Map<String, Integer> alertasPorEstado, List<Object[]> citas) {
        lblPendientes.setText(String.valueOf(alertasPorEstado.getOrDefault("PENDIENTE", 0)));
        lblSeguimiento.setText(String.valueOf(alertasPorEstado.getOrDefault("EN SEGUIMIENTO", 0)));
        List<Object[]> agendadas = new ArrayList<>();
        for (Object[] c : citas) {
            if ("AGENDADA".equals(c[6])) {
                agendadas.add(c);
            }
        }
        lblCitas.setText(String.valueOf(agendadas.size()));
        modelo.setRowCount(0);
        for (int i = 0; i < agendadas.size() && i < 8; i++) {
            Object[] c = agendadas.get(i);
            modelo.addRow(new Object[] { c[1], c[2], c[3], c[4] == null ? "" : c[4], c[5] });
        }
    }

    @Override
    public void refrescar() {
        try {
            mostrar(new EstadisticasDAO().alertasPorEstado(Contexto.idInstitucion()),
                    new CitaDAO().listarDePsicosocial(Contexto.idPsicosocial()));
            barra.limpiar();
        } catch (IllegalStateException ex) {
            barra.error(ex.getMessage());
        } catch (DatosException ex) {
            barra.error("No se pudo cargar el resumen. Revisa la carpeta datos del proyecto. ("
                    + ex.getMessage() + ")");
        }
    }
}
