package vista.estudiante;

import dao.CalificacionDAO;
import dao.CitaDAO;
import dao.EstudianteDAO;
import dao.PeriodoDAO;
import eipi.util.Formato;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import eipi.util.DatosException;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import modelo.PeriodoAcademico;
import modelo.Usuario;
import vista.Refrescable;
import vista.componentes.BarraMensaje;
import vista.componentes.Estilo;
import vista.componentes.Tarjeta;
import vista.componentes.Ui;

public class PanelInicioEstudiante extends JPanel implements Refrescable {

    private final BarraMensaje barra = new BarraMensaje();
    private final JLabel lblCurso = valor();
    private final JLabel lblJornada = valor();
    private final JLabel lblPromedio = valor();
    private final JLabel lblCitas = valor();
    private final JLabel textoPromedio = Ui.subtitulo("Promedio del período actual");

    public PanelInicioEstudiante(Usuario usuario) {
        setLayout(new BorderLayout(0, 24));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(28, 32, 28, 32));

        JPanel encabezado = new JPanel();
        encabezado.setOpaque(false);
        encabezado.setLayout(new BoxLayout(encabezado, BoxLayout.Y_AXIS));
        JLabel titulo = Ui.titulo("Hola, " + usuario.getNombre());
        JLabel sub = Ui.subtitulo("Tu resumen en E.I.P.I");
        titulo.setAlignmentX(LEFT_ALIGNMENT);
        sub.setAlignmentX(LEFT_ALIGNMENT);
        barra.setAlignmentX(LEFT_ALIGNMENT);
        encabezado.add(titulo);
        encabezado.add(sub);
        encabezado.add(barra);
        add(encabezado, BorderLayout.NORTH);

        JPanel rejilla = new JPanel(new GridLayout(0, 2, 16, 16));
        rejilla.setOpaque(false);
        rejilla.add(tarjeta(lblCurso, Ui.subtitulo("Mi curso")));
        rejilla.add(tarjeta(lblJornada, Ui.subtitulo("Jornada")));
        rejilla.add(tarjeta(lblPromedio, textoPromedio));
        rejilla.add(tarjeta(lblCitas, Ui.subtitulo("Citas agendadas")));
        JPanel contenedor = new JPanel(new BorderLayout());
        contenedor.setOpaque(false);
        contenedor.add(rejilla, BorderLayout.NORTH);
        add(contenedor, BorderLayout.CENTER);
    }

    private static JLabel valor() {
        JLabel l = new JLabel("-");
        l.setFont(Estilo.fuentePx(Estilo.NEGRITA, 38f));
        l.setForeground(Estilo.AZUL);
        return l;
    }

    private static Tarjeta tarjeta(JLabel valor, JLabel texto) {
        Tarjeta t = new Tarjeta(new BorderLayout(0, 6));
        t.add(valor, BorderLayout.CENTER);
        t.add(texto, BorderLayout.SOUTH);
        return t;
    }

    public void mostrar(String curso, String jornada, Double promedio, String periodo, int citas) {
        lblCurso.setText(curso == null ? "—" : curso);
        lblJornada.setText(jornada == null ? "—" : jornada);
        lblPromedio.setText(promedio == null ? "—" : Formato.nota(promedio));
        textoPromedio.setText(periodo == null ? "Promedio (no hay período abierto)" : "Promedio · " + periodo);
        lblCitas.setText(String.valueOf(citas));
    }

    @Override
    public void refrescar() {
        try {
            int id = Contexto.idEstudiante();
            Object[] curso = new EstudianteDAO().obtenerCurso(id);

            PeriodoAcademico abierto = null;
            for (PeriodoAcademico p : new PeriodoDAO().listar(Contexto.idInstitucion())) {
                if ("ABIERTO".equals(p.getEstado())) {
                    abierto = p;
                    break;
                }
            }
            Double promedio = abierto == null ? null
                    : new CalificacionDAO().promedioGeneral(id, abierto.getIdPeriodo());

            int agendadas = 0;
            List<Object[]> citas = new CitaDAO().listarDeEstudiante(id);
            for (Object[] c : citas) {
                if ("AGENDADA".equals(c[4])) {
                    agendadas++;
                }
            }
            mostrar((String) curso[0], (String) curso[1], promedio,
                    abierto == null ? null : abierto.getNombre(), agendadas);
            barra.limpiar();
        } catch (IllegalStateException ex) {
            barra.error(ex.getMessage());
        } catch (DatosException ex) {
            barra.error("No se pudo cargar el resumen. Revisa la carpeta datos del proyecto. ("
                    + ex.getMessage() + ")");
        }
    }
}
