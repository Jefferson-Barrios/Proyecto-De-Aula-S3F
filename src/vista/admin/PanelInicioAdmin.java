package vista.admin;

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

public class PanelInicioAdmin extends JPanel implements Refrescable {

    private final BarraMensaje barra = new BarraMensaje();
    private final Map<String, JLabel> valores = new LinkedHashMap<>();

    public PanelInicioAdmin(Usuario usuario) {
        setLayout(new BorderLayout(0, 24));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(28, 32, 28, 32));

        JPanel encabezado = new JPanel();
        encabezado.setOpaque(false);
        encabezado.setLayout(new BoxLayout(encabezado, BoxLayout.Y_AXIS));
        JLabel titulo = Ui.titulo("Hola, " + usuario.getNombre());
        JLabel sub = Ui.subtitulo("Resumen general de las instituciones registradas en E.I.P.I");
        titulo.setAlignmentX(LEFT_ALIGNMENT);
        sub.setAlignmentX(LEFT_ALIGNMENT);
        barra.setAlignmentX(LEFT_ALIGNMENT);
        encabezado.add(titulo);
        encabezado.add(sub);
        encabezado.add(barra);
        add(encabezado, BorderLayout.NORTH);

        JPanel rejilla = new JPanel(new GridLayout(0, 3, 16, 16));
        rejilla.setOpaque(false);
        agregarTarjeta(rejilla, "INSTITUCIONES", "Instituciones activas");
        agregarTarjeta(rejilla, "DIRECTIVA", "Directivas activas");
        agregarTarjeta(rejilla, "DOCENTE", "Docentes activos");
        agregarTarjeta(rejilla, "ESTUDIANTE", "Estudiantes activos");
        agregarTarjeta(rejilla, "PSICOSOCIAL", "Profesionales psicosociales");

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
        JLabel etiqueta = Ui.subtitulo(texto);
        t.add(valor, BorderLayout.CENTER);
        t.add(etiqueta, BorderLayout.SOUTH);
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
        try {
            BalanceDAO dao = new BalanceDAO();
            Map<String, Integer> datos = new HashMap<>(dao.contarUsuariosActivosPorRol());
            datos.put("INSTITUCIONES", dao.contarInstitucionesActivas());
            mostrar(datos);
            barra.limpiar();
        } catch (DatosException ex) {
            barra.error("No se pudo cargar el resumen. Revisa la carpeta datos del proyecto. ("
                    + ex.getMessage() + ")");
        }
    }
}
