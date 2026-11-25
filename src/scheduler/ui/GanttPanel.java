package scheduler.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JPanel;

import scheduler.cpu.CpuResult;

/** Draws the Gantt chart on a time scale, showing idle CPU time. */
final class GanttPanel extends JPanel {
    private static final int LEFT = 20, RIGHT = 30, TOP = 20, BAR_H = 44;
    private static final Color[] PALETTE = {
        new Color(0x8FB8DE), new Color(0xF4B183), new Color(0xA9D18E), new Color(0xFFD966),
        new Color(0xC9A7E0), new Color(0x9FD8D0), new Color(0xF2A7B5), new Color(0xB4C7E7),
        new Color(0xD6C08A), new Color(0xB8D8A0)
    };
    private static final Color IDLE_COLOR = new Color(0xDDDDDD);

    private CpuResult result;

    GanttPanel() {
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(600, 100));
    }

    void setResult(CpuResult r) {
        this.result = r;
        setPreferredSize(new Dimension(r == null ? 600 : LEFT + RIGHT + r.totalTime * unit(r), 100));
        revalidate();
        repaint();
    }

    private static int unit(CpuResult r) {
        return Math.max(10, Math.min(40, 1100 / Math.max(1, r.totalTime)));
    }

    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        if (result == null || result.gantt.isEmpty()) {
            g.setColor(Color.GRAY);
            g.drawString("Gantt chart will appear here after you run the simulation.", LEFT, TOP + 24);
            return;
        }
        int unit = unit(result);
        g.setFont(getFont().deriveFont(Font.BOLD, 12f));
        FontMetrics fm = g.getFontMetrics();

        // time axis with a minor tick for every time unit
        int axisY = TOP + BAR_H;
        g.setColor(Color.DARK_GRAY);
        g.drawLine(LEFT, axisY, LEFT + result.totalTime * unit, axisY);
        for (int t = 0; t <= result.totalTime; t++) {
            int x = LEFT + t * unit;
            g.drawLine(x, axisY, x, axisY + 3);
        }

        int lastLabelEnd = Integer.MIN_VALUE;
        for (CpuResult.Slice s : result.gantt) {
            int x = LEFT + s.start * unit;
            int w = (s.end - s.start) * unit;
            boolean idle = s.pid == CpuResult.IDLE;
            g.setColor(idle ? IDLE_COLOR : PALETTE[Math.floorMod(s.pid, PALETTE.length)]);
            g.fillRect(x, TOP, w, BAR_H);
            g.setColor(Color.DARK_GRAY);
            g.setStroke(new BasicStroke(idle ? 1f : 1.4f));
            g.drawRect(x, TOP, w, BAR_H);

            String label = idle ? "Idle" : "P" + s.pid;
            if (fm.stringWidth(label) + 4 > w) label = idle ? "" : String.valueOf(s.pid);
            if (fm.stringWidth(label) + 2 <= w) {
                g.setColor(idle ? Color.GRAY : Color.BLACK);
                g.drawString(label, x + (w - fm.stringWidth(label)) / 2, TOP + BAR_H / 2 + fm.getAscent() / 2 - 2);
            }
            lastLabelEnd = drawTime(g, fm, s.start, x, axisY, lastLabelEnd);
        }
        drawTime(g, fm, result.totalTime, LEFT + result.totalTime * unit, axisY, lastLabelEnd);
    }

    private int drawTime(Graphics2D g, FontMetrics fm, int time, int x, int axisY, int lastEnd) {
        String text = String.valueOf(time);
        int w = fm.stringWidth(text);
        int left = x - w / 2;
        if (left < lastEnd + 4) return lastEnd;
        g.setColor(Color.BLACK);
        g.drawString(text, left, axisY + 8 + fm.getAscent());
        return left + w;
    }
}
