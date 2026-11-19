package vista.componentes;

import java.awt.Dimension;
import java.awt.LayoutManager;
import java.awt.Rectangle;
import javax.swing.JPanel;
import javax.swing.Scrollable;

public class PanelAjustado extends JPanel implements Scrollable {

    public PanelAjustado(LayoutManager layout) {
        super(layout);
        setOpaque(false);
    }

    @Override
    public Dimension getPreferredScrollableViewportSize() {
        return getPreferredSize();
    }

    @Override
    public int getScrollableUnitIncrement(Rectangle visible, int orientation, int direction) {
        return 24;
    }

    @Override
    public int getScrollableBlockIncrement(Rectangle visible, int orientation, int direction) {
        return Math.max(24, visible.height - 40);
    }

    @Override
    public boolean getScrollableTracksViewportWidth() {
        return true;
    }

    @Override
    public boolean getScrollableTracksViewportHeight() {
        return false;
    }
}
