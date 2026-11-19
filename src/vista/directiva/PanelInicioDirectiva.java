package vista.directiva;

import dao.BalanceDAO;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import eipi.util.DatosException;
import java.util.HashMap;
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
import vista.componentes.Tarjeta;
import vista.componentes.Ui;

public class PanelInicioDirectiva extends JPanel implements Refrescable {

    private final Usuario usuario;
    private final BarraMensaje barra = new BarraMensaje();
    private final Map<String, JLabel> valores = new LinkedHashMap<>();

    public PanelInicioDirectiva(Usuario usuario) {
        this.usuario = usuario;
        setLayout(new BorderLayout(0, 24));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(28, 32, 28, 32));

        JPanel encabezado = new JPanel();
        encabezado.setOpaque(false);
        encabezado.setLayout(new BoxLayout(encabezado, BoxLayout.Y_AXIS));
        JLabel titulo = Ui.titulo("Hola, " + usuario.getNombre());
        JLabel sub = Ui.subtitulo("Resumen de tu institución");
        titulo.setAlignmentX(LEFT_ALIGNMENT);
        sub.setAlignmentX(LEFT_ALIGNMENT);
        barra.setAlignmentX(LEFT_ALIGNMENT);
        encabezado.add(titulo);
        encabezado.add(sub);
        encabezado.add(barra);
        add(encabezado, BorderLayout.NORTH);

        JPanel rejilla = new JPanel(new GridLayout(0, 3, 16, 16));
        rejilla.setOpaque(false);
        agregarTarjeta(rejilla, "ESTUDIANTE", "Estudiantes activos");
        agregarTarjeta(rejilla, "DOCENTE", "Docentes activos");
        agregarTarjeta(rejilla, "PSICOSOCIAL", "Profesionales psicosociales");
        agregarTarjeta(rejilla, "CURSOS", "Cursos");
        agregarTarjeta(rejilla, "MATERIAS", "Materias");

        JPanel contenedor = new JPanel(new BorderLayout());
        contenedor.setOpaque(false);
        contenedor.add(rejilla, BorderLayout.NORTH);
        add(contenedor, BorderLayout.CENTER);
    }

    private void agregarTarjeta(JPanel destino, String clave, String texto) {
        Tarjeta t = new Tarjeta(new BorderLayout(0, 6));
        JLabel valor = new JLabel("-");
        valor.setFont(Estilo.fuentePx(Estilo.NEGRITA, 38f));
        valor.setForeground(Estilo.AZUL);
        t.add(valor, BorderLayout.CENTER);
        t.add(Ui.subtitulo(texto), BorderLayout.SOUTH);
        valores.put(clave, valor);
        destino.add(t);
    }

    public void mostrar(Map<String, Integer> datos) {
        for (Map.Entry<String, JLabel> e : valores.entrySet()) {
            e.getValue().setText(String.valueOf(datos.getOrDefault(e.getKey(), 0)));
        }
    }

    @Override
    public void refrescar() {
        if (usuario.getIdInstitucion() == null) {
            barra.error("Tu usuario no está asociado a ninguna institución.");
            return;
        }
        try {
            int inst = usuario.getIdInstitucion();
            BalanceDAO dao = new BalanceDAO();
            Map<String, Integer> datos = new HashMap<>(dao.contarUsuariosActivosPorRol(inst));
            datos.put("CURSOS", dao.contarCursos(inst));
            datos.put("MATERIAS", dao.contarMaterias(inst));
            mostrar(datos);
            barra.limpiar();
        } catch (DatosException ex) {
            barra.error("No se pudo cargar el resumen. Revisa la carpeta datos del proyecto. ("
                    + ex.getMessage() + ")");
        }
    }
}
