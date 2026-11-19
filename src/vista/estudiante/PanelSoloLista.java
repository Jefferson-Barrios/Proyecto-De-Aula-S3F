package vista.estudiante;

import java.awt.FlowLayout;
import vista.componentes.BotonExportar;
import eipi.util.TablaExportable;
import java.awt.BorderLayout;
import eipi.util.DatosException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import vista.Refrescable;
import vista.componentes.BarraMensaje;
import vista.componentes.Tarjeta;
import vista.componentes.Ui;

public abstract class PanelSoloLista extends JPanel implements Refrescable {

    protected final BarraMensaje barra = new BarraMensaje();
    protected final JLabel lblSubtitulo = Ui.subtitulo(" ");
    private DefaultTableModel modelo;
    private JTable tabla;

    protected abstract String titulo();

    protected abstract String subtituloInicial();

    protected abstract String[] columnas();

    protected abstract int[] anchos();

    protected abstract List<Object[]> consultar() throws DatosException;

    protected abstract String textoVacio();

    protected final void iniciar() {
        setLayout(new BorderLayout(0, 16));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(28, 32, 28, 32));

        lblSubtitulo.setText(subtituloInicial());
        JPanel norte = new JPanel();
        norte.setOpaque(false);
        norte.setLayout(new BoxLayout(norte, BoxLayout.Y_AXIS));
        JLabel t = Ui.titulo(titulo());
        t.setAlignmentX(LEFT_ALIGNMENT);
        lblSubtitulo.setAlignmentX(LEFT_ALIGNMENT);
        barra.setAlignmentX(LEFT_ALIGNMENT);
        norte.add(t);
        norte.add(lblSubtitulo);
        norte.add(barra);
        add(norte, BorderLayout.NORTH);

        modelo = new DefaultTableModel(columnas(), 0) {
            @Override
            public boolean isCellEditable(int fila, int col) {
                return false;
            }
        };
        tabla = new JTable(modelo);
        Ui.estilizarTabla(tabla);
        int[] an = anchos();
        for (int i = 0; i < an.length && i < tabla.getColumnCount(); i++) {
            tabla.getColumnModel().getColumn(i).setPreferredWidth(an[i]);
        }
        Tarjeta tarjeta = new Tarjeta(new BorderLayout());
        tarjeta.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        tarjeta.add(Ui.scroll(tabla), BorderLayout.CENTER);
        add(tarjeta, BorderLayout.CENTER);

        BotonExportar exportar = new BotonExportar(
                () -> TablaExportable.de(titulo(), lblSubtitulo.getText().trim(), modelo), barra);
        JPanel pie = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        pie.setOpaque(false);
        pie.add(exportar);
        add(pie, BorderLayout.SOUTH);
    }

    public void cargar(List<Object[]> filas) {
        modelo.setRowCount(0);
        for (Object[] f : new ArrayList<>(filas)) {
            Object[] visibles = new Object[f.length];
            for (int i = 0; i < f.length; i++) {
                visibles[i] = f[i] == null ? "" : f[i];
            }
            modelo.addRow(visibles);
        }
    }

    @Override
    public void refrescar() {
        try {
            List<Object[]> filas = consultar();
            cargar(filas);
            if (filas.isEmpty()) {
                barra.exito(textoVacio());
            } else {
                barra.limpiar();
            }
        } catch (IllegalStateException ex) {
            barra.error(ex.getMessage());
        } catch (DatosException ex) {
            barra.error("No se pudo cargar la información. Revisa la carpeta datos del proyecto. ("
                    + ex.getMessage() + ")");
        }
    }
}
