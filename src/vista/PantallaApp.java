package vista;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.MatteBorder;
import modelo.Usuario;
import vista.admin.PanelDirectivas;
import vista.admin.PanelEstadisticasAdmin;
import vista.admin.PanelEnConstruccion;
import vista.admin.PanelInicioAdmin;
import vista.admin.PanelInstituciones;
import vista.componentes.BotonMenu;
import vista.componentes.BotonSimple;
import vista.componentes.Estilo;
import vista.componentes.Ui;
import vista.directiva.PanelAsignaciones;
import vista.directiva.PanelCursos;
import vista.directiva.PanelDocentes;
import vista.directiva.PanelEstudiantes;
import vista.directiva.PanelHorarios;
import vista.directiva.PanelInicioDirectiva;
import vista.directiva.PanelMaterias;
import vista.directiva.PanelNotasDefinitivas;
import vista.directiva.PanelPeriodos;
import vista.docente.PanelAsistencia;
import vista.docente.PanelCalificaciones;
import vista.docente.PanelComentarios;
import vista.docente.PanelInicioDocente;
import vista.docente.PanelRendimiento;
import vista.estudiante.PanelCitasEstudiante;
import vista.estudiante.PanelExpediente;
import vista.estudiante.PanelHorarioEstudiante;
import vista.estudiante.PanelInicioEstudiante;
import vista.estudiante.PanelNotasEstudiante;
import vista.psicosocial.PanelAlertasPsico;
import vista.psicosocial.PanelCitas;
import vista.psicosocial.PanelEstadisticas;
import vista.psicosocial.PanelInicioPsico;
import vista.psicosocial.PanelRiesgo;
import vista.directiva.PanelPsicosocial;

public class PantallaApp extends JPanel {

    private final CardLayout cartas = new CardLayout();
    private final JPanel contenido = new JPanel(cartas);
    private final JPanel listaMenu = new JPanel(new GridLayout(0, 1, 0, 3));
    private final Map<String, BotonMenu> botones = new LinkedHashMap<>();
    private final Map<String, JComponent> paneles = new HashMap<>();

    public PantallaApp(Usuario usuario, Runnable alCerrarSesion) {
        setLayout(new BorderLayout());
        setBackground(Estilo.FONDO);
        contenido.setOpaque(false);

        add(construirLateral(usuario, alCerrarSesion), BorderLayout.WEST);
        add(contenido, BorderLayout.CENTER);
        construirModulos(usuario);
        agregarModuloOculto("Mi cuenta", new PanelMiCuenta(usuario));
    }

    private void construirModulos(Usuario usuario) {
        switch (usuario.getRol()) {
            case "ADMINISTRADOR":
                agregarModulo("Inicio", new PanelInicioAdmin(usuario));
                agregarModulo("Instituciones", new PanelInstituciones());
                agregarModulo("Directivas", new PanelDirectivas());
                agregarModulo("Estadísticas", new PanelEstadisticasAdmin());
                break;
            case "DIRECTIVA":
                agregarModulo("Inicio", new PanelInicioDirectiva(usuario));
                agregarModulo("Estudiantes", new PanelEstudiantes());
                agregarModulo("Docentes", new PanelDocentes());
                agregarModulo("Psicosocial", new PanelPsicosocial());
                agregarModulo("Cursos", new PanelCursos());
                agregarModulo("Materias", new PanelMaterias());
                agregarModulo("Asignaciones", new PanelAsignaciones());
                agregarModulo("Horarios", new PanelHorarios());
                agregarModulo("Períodos", new PanelPeriodos());
                agregarModulo("Notas", new PanelNotasDefinitivas());
                agregarModulo("Estadísticas", new PanelEstadisticas());
                break;
            case "DOCENTE":
                agregarModulo("Inicio", new PanelInicioDocente(usuario));
                agregarModulo("Calificaciones", new PanelCalificaciones());
                agregarModulo("Asistencia", new PanelAsistencia());
                agregarModulo("Rendimiento", new PanelRendimiento());
                agregarModulo("Comentarios", new PanelComentarios());
                break;
            case "PSICOSOCIAL":
                agregarModulo("Inicio", new PanelInicioPsico(usuario));
                agregarModulo("Alertas", new PanelAlertasPsico());
                agregarModulo("Citas", new PanelCitas());
                agregarModulo("Riesgo", new PanelRiesgo());
                agregarModulo("Estadísticas", new PanelEstadisticas());
                break;
            case "ESTUDIANTE":
                agregarModulo("Inicio", new PanelInicioEstudiante(usuario));
                agregarModulo("Calificaciones", new PanelNotasEstudiante());
                agregarModulo("Horario", new PanelHorarioEstudiante());
                agregarModulo("Expediente", new PanelExpediente());
                agregarModulo("Citas", new PanelCitasEstudiante());
                break;
            default:
                agregarModulo("Inicio", new PanelEnConstruccion(nombreRol(usuario.getRol())));
                break;
        }
        mostrar("Inicio");
    }

    public final void agregarModulo(String nombre, JComponent panel) {
        BotonMenu boton = new BotonMenu(nombre);
        boton.addActionListener(e -> mostrar(nombre));
        botones.put(nombre, boton);
        listaMenu.add(boton);
        paneles.put(nombre, panel);
        contenido.add(panel, nombre);
    }

    public final void agregarModuloOculto(String nombre, JComponent panel) {
        paneles.put(nombre, panel);
        contenido.add(panel, nombre);
    }

    public final void mostrar(String nombre) {
        cartas.show(contenido, nombre);
        for (Map.Entry<String, BotonMenu> e : botones.entrySet()) {
            e.getValue().setActivo(e.getKey().equals(nombre));
        }
        JComponent panel = paneles.get(nombre);
        if (panel instanceof Refrescable) {
            ((Refrescable) panel).refrescar();
        }
    }

    public static String nombreRol(String rol) {
        switch (rol) {
            case "ADMINISTRADOR":
                return "Administrador general";
            case "DIRECTIVA":
                return "Directiva";
            case "DOCENTE":
                return "Docente";
            case "ESTUDIANTE":
                return "Estudiante";
            case "PSICOSOCIAL":
                return "Psicosocial";
            default:
                return rol;
        }
    }

    private JPanel construirLateral(Usuario usuario, Runnable alCerrarSesion) {
        JPanel lateral = new JPanel(new BorderLayout(0, 16));
        lateral.setBackground(java.awt.Color.WHITE);
        lateral.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(0, 0, 0, 1, Estilo.BORDE),
                BorderFactory.createEmptyBorder(24, 16, 20, 16)));
        lateral.setPreferredSize(new Dimension(250, 100));

        lateral.add(new Cabecera(), BorderLayout.NORTH);

        JPanel envoltura = new JPanel(new BorderLayout());
        envoltura.setOpaque(false);
        listaMenu.setOpaque(false);
        envoltura.add(listaMenu, BorderLayout.NORTH);

        JScrollPane desplazable = new JScrollPane(envoltura);
        desplazable.setBorder(BorderFactory.createEmptyBorder());
        desplazable.setOpaque(false);
        desplazable.getViewport().setOpaque(false);
        desplazable.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        desplazable.getVerticalScrollBar().setUnitIncrement(16);
        desplazable.getVerticalScrollBar().setPreferredSize(new Dimension(10, 0));
        lateral.add(desplazable, BorderLayout.CENTER);

        JPanel pie = new JPanel(new BorderLayout(0, 12));
        pie.setOpaque(false);
        JPanel datos = new JPanel(new GridLayout(2, 1, 0, 2));
        datos.setOpaque(false);
        JLabel nombre = new JLabel(usuario.getNombre());
        nombre.setFont(Estilo.fuentePx(Estilo.MEDIO, 14f));
        nombre.setForeground(Estilo.TEXTO);
        nombre.setToolTipText(usuario.getNombre());
        JLabel rol = Ui.subtitulo(nombreRol(usuario.getRol()));
        datos.add(nombre);
        JLabel miCuenta = new JLabel("Mi cuenta");
        miCuenta.setFont(Estilo.fuentePx(Estilo.MEDIO, 12f));
        miCuenta.setForeground(Estilo.AZUL_LINK);
        miCuenta.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
        miCuenta.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                mostrar("Mi cuenta");
            }

            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                miCuenta.setText("<html><u>Mi cuenta</u></html>");
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                miCuenta.setText("Mi cuenta");
            }
        });
        JPanel filaRol = new JPanel(new BorderLayout());
        filaRol.setOpaque(false);
        filaRol.add(rol, BorderLayout.WEST);
        filaRol.add(miCuenta, BorderLayout.EAST);
        datos.add(filaRol);
        BotonSimple salir = new BotonSimple("Cerrar sesión", BotonSimple.Tipo.SECUNDARIO);
        salir.addActionListener(e -> alCerrarSesion.run());
        pie.add(datos, BorderLayout.NORTH);
        pie.add(salir, BorderLayout.SOUTH);
        lateral.add(pie, BorderLayout.SOUTH);
        return lateral;
    }

    private static class Cabecera extends JPanel {
        Cabecera() {
            setOpaque(false);
            setPreferredSize(new Dimension(100, 116));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            BufferedImage logo = Estilo.logo();
            int w = getWidth();
            if (logo != null) {
                int lw = 76;
                int lh = Math.round(lw * logo.getHeight() / (float) logo.getWidth());
                Image esc = logo;
                g2.drawImage(esc, (w - lw) / 2, 4, lw, lh, null);
            }
            g2.setFont(Estilo.fuentePx(Estilo.NEGRITA, 22f));
            g2.setColor(Estilo.AZUL);
            String t = "E.I.P.I";
            int tw = g2.getFontMetrics().stringWidth(t);
            g2.drawString(t, (w - tw) / 2, 98);
            g2.dispose();
        }
    }
}
