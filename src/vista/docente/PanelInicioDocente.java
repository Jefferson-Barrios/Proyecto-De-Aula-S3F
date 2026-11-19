package vista.docente;

import dao.ComentarioDAO;
import dao.AsignacionDAO;
import dao.EstudianteDAO;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import eipi.util.DatosException;
import java.util.List;
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

public class PanelInicioDocente extends JPanel implements Refrescable {

    private final BarraMensaje barra = new BarraMensaje();
    private final JLabel lblAsignaciones = valor();
    private final JLabel lblEstudiantes = valor();
    private final JLabel lblAlertas = valor();
    private final DefaultTableModel modelo = new DefaultTableModel(
            new Object[] { "Curso", "Materia", "Jornada", "Estudiantes" }, 0) {
        @Override
        public boolean isCellEditable(int fila, int col) {
            return false;
        }
    };

    public PanelInicioDocente(Usuario usuario) {
        setLayout(new BorderLayout(0, 24));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(28, 32, 28, 32));

        JPanel encabezado = new JPanel();
        encabezado.setOpaque(false);
        encabezado.setLayout(new BoxLayout(encabezado, BoxLayout.Y_AXIS));
        JLabel titulo = Ui.titulo("Hola, " + usuario.getNombre());
        JLabel sub = Ui.subtitulo("Tus cursos y estudiantes de un vistazo");
        titulo.setAlignmentX(LEFT_ALIGNMENT);
        sub.setAlignmentX(LEFT_ALIGNMENT);
        barra.setAlignmentX(LEFT_ALIGNMENT);
        encabezado.add(titulo);
        encabezado.add(sub);
        encabezado.add(barra);
        add(encabezado, BorderLayout.NORTH);

        JPanel centro = new JPanel(new BorderLayout(0, 16));
        centro.setOpaque(false);
        JPanel rejilla = new JPanel(new GridLayout(1, 3, 16, 16));
        rejilla.setOpaque(false);
        rejilla.add(tarjeta(lblAsignaciones, "Asignaciones activas"));
        rejilla.add(tarjeta(lblEstudiantes, "Estudiantes a cargo"));
        rejilla.add(tarjeta(lblAlertas, "Comentarios en expedientes"));
        centro.add(rejilla, BorderLayout.NORTH);

        JTable tabla = new JTable(modelo);
        Ui.estilizarTabla(tabla);
        Tarjeta t = new Tarjeta(new BorderLayout(0, 8));
        t.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        JLabel mis = Ui.etiqueta("Mis cursos");
        mis.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 0));
        t.add(mis, BorderLayout.NORTH);
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

    public void mostrar(List<Object[]> asignaciones, int estudiantes, int comentarios) {
        lblAsignaciones.setText(String.valueOf(asignaciones.size()));
        lblEstudiantes.setText(String.valueOf(estudiantes));
        lblAlertas.setText(String.valueOf(comentarios));
        modelo.setRowCount(0);
        for (Object[] f : asignaciones) {
            modelo.addRow(new Object[] { f[2], f[3], f[4], f[5] });
        }
    }

    @Override
    public void refrescar() {
        try {
            int id = Selectores.idDocente();
            mostrar(new AsignacionDAO().listarDeDocente(id),
                    new EstudianteDAO().listarDeDocente(id).size(),
                    new ComentarioDAO().listarDeDocente(id).size());
            barra.limpiar();
        } catch (IllegalStateException ex) {
            barra.error(ex.getMessage());
        } catch (DatosException ex) {
            barra.error("No se pudo cargar el resumen. Revisa la carpeta datos del proyecto. ("
                    + ex.getMessage() + ")");
        }
    }
}
