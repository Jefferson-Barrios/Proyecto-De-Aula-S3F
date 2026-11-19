package vista.psicosocial;

import dao.EstadisticasDAO;
import eipi.util.Sesion;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import eipi.util.DatosException;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import modelo.Usuario;
import vista.Refrescable;
import vista.componentes.BarraMensaje;
import vista.componentes.Estilo;
import vista.componentes.GraficoBarras;
import vista.componentes.Tarjeta;
import vista.componentes.Ui;

public class PanelEstadisticas extends JPanel implements Refrescable {

    private static final Color NARANJA = new Color(0xC47A00);

    private final BarraMensaje barra = new BarraMensaje();
    private final JLabel lblTotal = valor();
    private final JLabel lblPendientes = valor();
    private final JLabel lblAltas = valor();
    private final JLabel lblCitas = valor();
    private final GraficoBarras porNivel = new GraficoBarras()
            .colorDe("ALTO", Estilo.ROJO).colorDe("MEDIO", NARANJA).colorDe("PREVENTIVA", new Color(0x2E7DD1));
    private final GraficoBarras porEstado = new GraficoBarras()
            .colorDe("PENDIENTE", NARANJA).colorDe("EN SEGUIMIENTO", Estilo.AZUL)
            .colorDe("CERRADA", new Color(0x9AA3B5));
    private final GraficoBarras porCurso = new GraficoBarras();
    private final GraficoBarras citas = new GraficoBarras()
            .colorDe("AGENDADA", Estilo.AZUL).colorDe("REALIZADA", Estilo.VERDE)
            .colorDe("CANCELADA", Estilo.ROJO);

    public PanelEstadisticas() {
        setLayout(new BorderLayout(0, 20));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(28, 32, 28, 32));

        JPanel encabezado = new JPanel();
        encabezado.setOpaque(false);
        encabezado.setLayout(new BoxLayout(encabezado, BoxLayout.Y_AXIS));
        JLabel titulo = Ui.titulo("Estadísticas");
        JLabel sub = Ui.subtitulo("Seguimiento de la deserción escolar en tu institución");
        titulo.setAlignmentX(LEFT_ALIGNMENT);
        sub.setAlignmentX(LEFT_ALIGNMENT);
        barra.setAlignmentX(LEFT_ALIGNMENT);
        encabezado.add(titulo);
        encabezado.add(sub);
        encabezado.add(barra);
        add(encabezado, BorderLayout.NORTH);

        JPanel centro = new JPanel(new BorderLayout(0, 16));
        centro.setOpaque(false);
        JPanel cifras = new JPanel(new GridLayout(1, 4, 16, 16));
        cifras.setOpaque(false);
        cifras.add(tarjetaCifra(lblTotal, "Alertas en total"));
        cifras.add(tarjetaCifra(lblPendientes, "Alertas pendientes"));
        cifras.add(tarjetaCifra(lblAltas, "Alertas de nivel alto"));
        cifras.add(tarjetaCifra(lblCitas, "Citas agendadas"));
        centro.add(cifras, BorderLayout.NORTH);

        JPanel graficos = new JPanel(new GridLayout(2, 2, 16, 16));
        graficos.setOpaque(false);
        graficos.add(tarjetaGrafico("Alertas por nivel de riesgo", porNivel));
        graficos.add(tarjetaGrafico("Alertas por estado", porEstado));
        graficos.add(tarjetaGrafico("Cursos con más alertas", porCurso));
        graficos.add(tarjetaGrafico("Citas por estado", citas));
        centro.add(graficos, BorderLayout.CENTER);
        add(centro, BorderLayout.CENTER);
    }

    private static JLabel valor() {
        JLabel l = new JLabel("-");
        l.setFont(Estilo.fuentePx(Estilo.NEGRITA, 32f));
        l.setForeground(Estilo.AZUL);
        return l;
    }

    private static Tarjeta tarjetaCifra(JLabel valor, String texto) {
        Tarjeta t = new Tarjeta(new BorderLayout(0, 4));
        t.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));
        t.add(valor, BorderLayout.CENTER);
        t.add(Ui.subtitulo(texto), BorderLayout.SOUTH);
        return t;
    }

    private static Tarjeta tarjetaGrafico(String titulo, GraficoBarras grafico) {
        Tarjeta t = new Tarjeta(new BorderLayout(0, 8));
        t.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));
        t.add(Ui.etiqueta(titulo), BorderLayout.NORTH);
        JPanel envoltura = new JPanel(new BorderLayout());
        envoltura.setOpaque(false);
        envoltura.add(grafico, BorderLayout.NORTH);
        t.add(envoltura, BorderLayout.CENTER);
        return t;
    }

    public void mostrar(Map<String, Integer> nivel, Map<String, Integer> estado,
            Map<String, Integer> curso, Map<String, Integer> cita) {
        int total = 0;
        for (int v : nivel.values()) {
            total += v;
        }
        lblTotal.setText(String.valueOf(total));
        lblPendientes.setText(String.valueOf(estado.getOrDefault("PENDIENTE", 0)));
        lblAltas.setText(String.valueOf(nivel.getOrDefault("ALTO", 0)));
        lblCitas.setText(String.valueOf(cita.getOrDefault("AGENDADA", 0)));
        porNivel.setDatos(nivel);
        porEstado.setDatos(estado);
        porCurso.setDatos(new LinkedHashMap<>(curso));
        citas.setDatos(cita);
    }

    @Override
    public void refrescar() {
        Usuario u = Sesion.getUsuarioActual();
        if (u == null || u.getIdInstitucion() == null) {
            barra.error("Tu usuario no está asociado a ninguna institución.");
            return;
        }
        try {
            EstadisticasDAO dao = new EstadisticasDAO();
            int inst = u.getIdInstitucion();
            mostrar(dao.alertasPorNivel(inst), dao.alertasPorEstado(inst),
                    dao.alertasPorCurso(inst), dao.citasPorEstado(inst));
            barra.limpiar();
        } catch (DatosException ex) {
            barra.error("No se pudieron cargar las estadísticas. Revisa la carpeta datos del proyecto. ("
                    + ex.getMessage() + ")");
        }
    }
}
