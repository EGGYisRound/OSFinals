package scheduler.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.Scrollable;
import java.awt.Rectangle;

import scheduler.disk.DiskResult;

/**
 * Graph of the head's path: cylinders run left to right, time (steps) runs
 * top to bottom. Green = initial head position, blue = served request,
 * orange = disk end / return jump.
 */
final class PathPanel extends JPanel implements Scrollable {
    private static final int LEFT = 40, RIGHT = 50, TOP = 50, ROW = 28;

    private DiskResult result;

    PathPanel() {
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(700, 120));
    }

    void setResult(DiskResult r) {
        this.result = r;
        setPreferredSize(new Dimension(700, r == null ? 120 : TOP + (r.steps.size() + 1) * ROW + 20));
        revalidate();
        repaint();
    }

    @Override public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }
    @Override public int getScrollableUnitIncrement(Rectangle r, int o, int d) { return ROW; }
    @Override public int getScrollableBlockIncrement(Rectangle r, int o, int d) { return ROW * 5; }
    @Override public boolean getScrollableTracksViewportWidth() { return true; }
    @Override public boolean getScrollableTracksViewportHeight() { return false; }

    private int xOf(int cylinder) {
        int usable = Math.max(100, getWidth() - LEFT - RIGHT);
        return LEFT + (int) Math.round((double) cylinder * usable / Math.max(1, result.diskSize - 1));
    }

    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        if (result == null) {
            g.setColor(Color.GRAY);
            g.drawString("The head's path graph will appear here after you run the simulation.", LEFT, 40);
            return;
        }
        FontMetrics fm = g.getFontMetrics();

        // cylinder axis
        g.setColor(Color.DARK_GRAY);
        int axisY = TOP - 14;
        g.drawLine(xOf(0), axisY, xOf(result.diskSize - 1), axisY);
        int ticks = 8;
        for (int i = 0; i <= ticks; i++) {
            int c = (int) Math.round((double) i * (result.diskSize - 1) / ticks);
            int x = xOf(c);
            g.drawLine(x, axisY - 3, x, axisY + 3);
            String s = String.valueOf(c);
            g.drawString(s, x - fm.stringWidth(s) / 2, axisY - 8);
            g.setColor(new Color(0, 0, 0, 25));
            g.drawLine(x, axisY + 3, x, getHeight() - 10);
            g.setColor(Color.DARK_GRAY);
        }

        List<Integer> path = result.path();
        g.setStroke(new BasicStroke(2f));
        g.setColor(new Color(0x2F6DB5));
        for (int i = 0; i + 1 < path.size(); i++) {
            DiskResult.Step step = result.steps.get(i);
            g.setColor(step.kind == DiskResult.Kind.RETURN ? new Color(0xE08A2E) : new Color(0x2F6DB5));
            g.drawLine(xOf(path.get(i)), TOP + i * ROW, xOf(path.get(i + 1)), TOP + (i + 1) * ROW);
        }
        g.setStroke(new BasicStroke(1f));
        for (int i = 0; i < path.size(); i++) {
            int x = xOf(path.get(i));
            int y = TOP + i * ROW;
            Color c;
            if (i == 0) c = new Color(0x2E9B4F);
            else c = result.steps.get(i - 1).kind.serves() ? new Color(0x2F6DB5) : new Color(0xE08A2E);
            g.setColor(c);
            g.fillOval(x - 5, y - 5, 10, 10);
            g.setColor(Color.BLACK);
            String label = String.valueOf(path.get(i));
            int lx = x + 9;
            if (lx + fm.stringWidth(label) > getWidth() - 4) lx = x - 9 - fm.stringWidth(label);
            g.drawString(label, lx, y + fm.getAscent() / 2 - 1);
        }
    }
}
