package scheduler.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.List;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import scheduler.disk.Direction;
import scheduler.disk.DiskResult;
import scheduler.disk.DiskScheduler;
import scheduler.util.Validation;
import scheduler.util.ValidationException;

/** Screen for one disk scheduling algorithm: inputs, seek sequence, head movement, path graph. */
final class DiskPanel extends JPanel {
    private final DiskScheduler algorithm;

    private final JTextField requestsField = new JTextField(46);
    private final JTextField headField = new JTextField(6);
    private final JTextField sizeField = new JTextField(String.valueOf(Validation.DEFAULT_DISK_SIZE), 6);
    private final JComboBox<Direction> directionBox = new JComboBox<>(Direction.values());
    private final DefaultTableModel stepsModel;
    private final JTextArea summary = new JTextArea(6, 30);
    private final PathPanel path = new PathPanel();
    private final JLabel status = new JLabel(" ");

    DiskPanel(DiskScheduler algorithm, Runnable onBack) {
        this.algorithm = algorithm;
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));

        JLabel title = new JLabel("Disk Scheduling - " + algorithm.getName());
        title.setFont(title.getFont().deriveFont(Font.BOLD, 18f));

        // ---- inputs
        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        row1.add(new JLabel("Request queue (cylinders, separated by commas or spaces):"));
        row1.add(requestsField);
        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        row2.add(new JLabel("Initial head position:"));
        row2.add(headField);
        row2.add(new JLabel("   Disk size (cylinders 0 to N-1):"));
        row2.add(sizeField);
        row2.add(new JLabel("   Initial direction:"));
        row2.add(directionBox);
        directionBox.setEnabled(algorithm.usesDirection());
        if (!algorithm.usesDirection()) directionBox.setToolTipText("This algorithm does not use a direction.");

        JPanel row3 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        JButton run = new JButton("Run Simulation");
        JButton sample = new JButton("Load Sample Data");
        JButton reset = new JButton("Reset / Clear");
        JButton back = new JButton("Back to Menu");
        run.addActionListener(e -> run());
        sample.addActionListener(e -> loadSample());
        reset.addActionListener(e -> reset());
        back.addActionListener(e -> onBack.run());
        row3.add(run);
        row3.add(sample);
        row3.add(reset);
        row3.add(back);

        JPanel inputs = new JPanel();
        inputs.setLayout(new javax.swing.BoxLayout(inputs, javax.swing.BoxLayout.Y_AXIS));
        inputs.setBorder(BorderFactory.createTitledBorder("Input"));
        inputs.add(row1);
        inputs.add(row2);
        inputs.add(row3);

        // ---- outputs
        stepsModel = new DefaultTableModel(new Object[] {"Step", "From", "To", "Movement", "Action"}, 0) {
            private static final long serialVersionUID = 1L;
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable stepsTable = new JTable(stepsModel);
        stepsTable.setRowHeight(22);

        summary.setEditable(false);
        summary.setLineWrap(true);
        summary.setWrapStyleWord(true);
        summary.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        summary.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));

        JPanel results = new JPanel(new BorderLayout(6, 6));
        results.setBorder(BorderFactory.createTitledBorder("Results"));
        results.add(new JScrollPane(stepsTable), BorderLayout.CENTER);
        JScrollPane summaryScroll = new JScrollPane(summary);
        summaryScroll.setPreferredSize(new java.awt.Dimension(300, 140));
        results.add(summaryScroll, BorderLayout.SOUTH);

        JScrollPane pathScroll = new JScrollPane(path);
        pathScroll.setBorder(BorderFactory.createTitledBorder("Head path graph"));

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, results, pathScroll);
        split.setResizeWeight(0.4);
        split.setBorder(null);

        status.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
        add(title, BorderLayout.NORTH);
        JPanel center = new JPanel(new BorderLayout(6, 6));
        center.add(inputs, BorderLayout.NORTH);
        center.add(split, BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);
        add(status, BorderLayout.SOUTH);
    }

    private void run() {
        try {
            int size = Validation.parseInt("Disk size", sizeField.getText(), 2, Validation.MAX_DISK_SIZE);
            int head = Validation.parseInt("Initial head position", headField.getText(), 0, size - 1);
            List<Integer> requests = Validation.parseRequests(requestsField.getText(), size);
            Direction dir = (Direction) directionBox.getSelectedItem();
            show(algorithm.schedule(requests, head, size, dir));
            setStatus("Simulation complete.", false);
        } catch (ValidationException e) {
            error(e.getMessage());
        } catch (RuntimeException e) {
            error("Unexpected problem: " + e.getMessage());
        }
    }

    private void show(DiskResult res) {
        stepsModel.setRowCount(0);
        int i = 1;
        for (DiskResult.Step s : res.steps) {
            stepsModel.addRow(new Object[] {i++, s.from, s.to, s.distance, s.kind.label()});
        }
        StringBuilder order = new StringBuilder();
        for (int c : res.serviceOrder) order.append(order.length() == 0 ? "" : " -> ").append(c);
        summary.setText(String.format(Locale.ROOT,
                "Seek sequence (order of service):%n%s%n%n"
                + "Total head movement : %d cylinders%n"
                + "Average seek distance: %.2f  (%d / %d requests)",
                order, res.totalMovement, res.averageSeek, res.totalMovement, res.serviceOrder.size()));
        summary.setCaretPosition(0);
        path.setResult(res);
    }

    private void loadSample() {
        requestsField.setText(Samples.DISK_REQUESTS);
        headField.setText(String.valueOf(Samples.DISK_HEAD));
        sizeField.setText(String.valueOf(Samples.DISK_SIZE));
        directionBox.setSelectedItem(Samples.DISK_DIRECTION);
        clearOutput();
        setStatus("Sample data loaded. Press Run Simulation.", false);
    }

    private void reset() {
        requestsField.setText("");
        headField.setText("");
        sizeField.setText(String.valueOf(Validation.DEFAULT_DISK_SIZE));
        directionBox.setSelectedItem(Direction.UP);
        clearOutput();
        setStatus("Cleared.", false);
    }

    private void clearOutput() {
        stepsModel.setRowCount(0);
        summary.setText("");
        path.setResult(null);
    }

    private void error(String message) {
        setStatus(message, true);
        JOptionPane.showMessageDialog(this, message, "Invalid input", JOptionPane.WARNING_MESSAGE);
    }

    private void setStatus(String text, boolean isError) {
        status.setForeground(isError ? new Color(0xB00020) : new Color(0x1B5E20));
        status.setText(text);
    }
}
