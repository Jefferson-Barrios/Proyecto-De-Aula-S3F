package vista.componentes;

import eipi.util.ExportadorPdf;
import eipi.util.ExportadorXlsx;
import eipi.util.TablaExportable;
import java.awt.Window;
import java.io.File;
import java.io.IOException;
import java.util.function.Supplier;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;

public class BotonExportar extends BotonSimple {

    private static File ultimaCarpeta;

    private final Supplier<TablaExportable> origen;
    private final BarraMensaje barra;

    public BotonExportar(Supplier<TablaExportable> origen, BarraMensaje barra) {
        super("Exportar", Tipo.SECUNDARIO);
        this.origen = origen;
        this.barra = barra;
        addActionListener(e -> mostrarMenu());
    }

    private void mostrarMenu() {
        JPopupMenu menu = new JPopupMenu();
        JMenuItem excel = new JMenuItem("Excel (.xlsx)");
        JMenuItem pdf = new JMenuItem("PDF (.pdf)");
        excel.setFont(Estilo.fuentePx(Estilo.REGULAR, 13f));
        pdf.setFont(Estilo.fuentePx(Estilo.REGULAR, 13f));
        excel.addActionListener(e -> exportar(true));
        pdf.addActionListener(e -> exportar(false));
        menu.add(excel);
        menu.add(pdf);
        menu.show(this, 0, -menu.getPreferredSize().height);
    }

    private void exportar(boolean excel) {
        TablaExportable tabla;
        try {
            tabla = origen.get();
        } catch (RuntimeException ex) {
            barra.error(ex.getMessage() == null ? "No se pudo preparar la exportación." : ex.getMessage());
            return;
        }
        if (tabla.filas.isEmpty()) {
            barra.error("No hay datos para exportar.");
            return;
        }

        String extension = excel ? "xlsx" : "pdf";
        Window ventana = SwingUtilities.getWindowAncestor(this);
        JFileChooser selector = new JFileChooser(ultimaCarpeta);
        selector.setDialogTitle("Guardar como " + (excel ? "Excel" : "PDF"));
        selector.setFileFilter(new FileNameExtensionFilter(
                excel ? "Libro de Excel (*.xlsx)" : "Documento PDF (*.pdf)", extension));
        selector.setSelectedFile(new File(selector.getCurrentDirectory(), tabla.nombreArchivoBase() + "." + extension));
        if (selector.showSaveDialog(ventana) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File destino = selector.getSelectedFile();
        if (!destino.getName().toLowerCase().endsWith("." + extension)) {
            destino = new File(destino.getParentFile(), destino.getName() + "." + extension);
        }
        if (destino.exists()) {
            int r = JOptionPane.showConfirmDialog(ventana,
                    "El archivo \"" + destino.getName() + "\" ya existe. ¿Deseas reemplazarlo?",
                    "Reemplazar archivo", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (r != JOptionPane.YES_OPTION) {
                return;
            }
        }

        try {
            if (excel) {
                ExportadorXlsx.escribir(tabla, destino);
            } else {
                ExportadorPdf.escribir(tabla, destino);
            }
            ultimaCarpeta = destino.getParentFile();
            barra.exito("Archivo guardado: " + destino.getAbsolutePath());
        } catch (IOException ex) {
            barra.error("No se pudo guardar el archivo. Si ya lo tienes abierto en otro programa, ciérralo e intenta de nuevo. ("
                    + ex.getMessage() + ")");
        }
    }
}
