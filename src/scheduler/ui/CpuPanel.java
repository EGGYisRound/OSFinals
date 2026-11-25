package scheduler.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.DefaultTableModel;

import scheduler.cpu.CpuResult;
import scheduler.cpu.CpuScheduler;
import scheduler.cpu.ProcessInfo;
import scheduler.util.Validation;
import scheduler.util.ValidationException;

/** Screen for one CPU scheduling algorithm: input table, Gantt chart, metrics. */
final class CpuPanel extends JPanel {
    private static final int DEFAULT_ROWS = 5;

    private final CpuScheduler algorithm;
    private final boolean priority;

    private final JSpinner countSpinner =
            new JSpinner(new SpinnerNumberModel(DEFAULT_ROWS, 1, Validation.MAX_PROCESSES, 1));
    private final JTextField quantumField = new JTextField("2", 5);
    private final DefaultTableModel inputModel;
    private final JTable inputTable;
    private final GanttPanel gantt = new GanttPanel();
    private final DefaultTableModel resultModel;
    private final JTextArea summary = new JTextArea(6, 30);
    private final JLabel status = new JLabel(" ");

    CpuPanel(CpuScheduler algorithm, Runnable onBack) {
        this.algorithm = algorithm;
        this.priority = algorithm.usesPriority();
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));

        // ---- title
        JLabel title = new JLabel("CPU Scheduling - " + algorithm.getName()
                + (algorithm.isPreemptive() ? "  (preemptive)" : "  (non-preemptive)"));
        title.setFont(title.getFont().deriveFont(Font.BOLD, 18f));

        // ---- input area
        List<String> inCols = new ArrayList<>(List.of("PID", "Arrival Time", "Burst Time"));
        if (priority) inCols.add("Priority (lower = higher)");
        inputModel = new DefaultTableModel(inCols.toArray(), 0);
        inputTable = new JTable(inputModel);
        inputTable.setRowHeight(24);
        resizeRows(DEFAULT_ROWS);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        controls.add(new JLabel("Number of processes:"));
        controls.add(countSpinner);
        JButton apply = new JButton("Apply");
        apply.addActionListener(e -> applyCount());
        controls.add(apply);
        if (algorithm.usesQuantum()) {
            controls.add(new JLabel("   Time quantum:"));
            controls.add(quantumField);
        }

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        JButton run = new JButton("Run Simulation");
        JButton sample = new JButton("Load Sample Data");
        JButton reset = new JButton("Reset / Clear");
        JButton back = new JButton("Back to Menu");
        run.addActionListener(e -> run());
        sample.addActionListener(e -> loadSample());
        reset.addActionListener(e -> reset());
        back.addActionListener(e -> onBack.run());
        buttons.add(run);
        buttons.add(sample);
        buttons.add(reset);
        buttons.add(back);

        JPanel inputTop = new JPanel(new BorderLayout());
        inputTop.add(controls, BorderLayout.NORTH);
        inputTop.add(buttons, BorderLayout.SOUTH);

        JPanel inputPanel = new JPanel(new BorderLayout(4, 4));
        inputPanel.setBorder(BorderFactory.createTitledBorder("Input"));
        inputPanel.add(inputTop, BorderLayout.NORTH);
        inputPanel.add(new JScrollPane(inputTable), BorderLayout.CENTER);

        // ---- output area
        List<String> outCols = new ArrayList<>(List.of("PID", "Arrival", "Burst"));
        if (priority) outCols.add("Priority");
        outCols.addAll(List.of("Completion", "Turnaround", "Waiting", "Response"));
        resultModel = new DefaultTableModel(outCols.toArray(), 0) {
            private static final long serialVersionUID = 1L;
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable resultTable = new JTable(resultModel);
        resultTable.setRowHeight(22);

        JScrollPane ganttScroll = new JScrollPane(gantt);
        ganttScroll.setBorder(BorderFactory.createTitledBorder("Gantt chart"));
        ganttScroll.setPreferredSize(new java.awt.Dimension(600, 150));

        summary.setEditable(false);
        summary.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        summary.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        JPanel tablePanel = new JPanel(new BorderLayout(6, 6));
        tablePanel.setBorder(BorderFactory.createTitledBorder("Results"));
        tablePanel.add(new JScrollPane(resultTable), BorderLayout.CENTER);
        JScrollPane summaryScroll = new JScrollPane(summary);
        summaryScroll.setPreferredSize(new java.awt.Dimension(400, 100));
        tablePanel.add(summaryScroll, BorderLayout.EAST);

        JPanel output = new JPanel(new BorderLayout(6, 6));
        output.add(ganttScroll, BorderLayout.NORTH);
        output.add(tablePanel, BorderLayout.CENTER);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, inputPanel, output);
        split.setResizeWeight(0.35);
        split.setBorder(null);

        status.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
        add(title, BorderLayout.NORTH);
        add(split, BorderLayout.CENTER);
        add(status, BorderLayout.SOUTH);
        clearOutput();
    }

    // ---------------------------------------------------------------- input
    private void stopEditing() {
        if (inputTable.isEditing()) inputTable.getCellEditor().stopCellEditing();
    }

    private void resizeRows(int count) {
        while (inputModel.getRowCount() > count) inputModel.removeRow(inputModel.getRowCount() - 1);
        while (inputModel.getRowCount() < count) {
            Object[] row = new Object[inputModel.getColumnCount()];
            row[0] = String.valueOf(inputModel.getRowCount() + 1);
            for (int i = 1; i < row.length; i++) row[i] = "";
            inputModel.addRow(row);
        }
    }

    private void applyCount() {
        stopEditing();
        try {
            int n = Validation.parseInt("Number of processes", String.valueOf(countSpinner.getValue()),
                    1, Validation.MAX_PROCESSES);
            resizeRows(n);
            setStatus("Table resized to " + n + " process(es).", false);
        } catch (ValidationException e) {
            error(e.getMessage());
        }
    }

    private List<ProcessInfo> readProcesses() throws ValidationException {
        stopEditing();
        List<ProcessInfo> list = new ArrayList<>();
        Set<Integer> pids = new HashSet<>();
        for (int r = 0; r < inputModel.getRowCount(); r++) {
            String where = "Row " + (r + 1) + " - ";
            int pid = Validation.parseInt(where + "PID", cell(r, 0), 1, 9999);
            if (!pids.add(pid)) throw new ValidationException(where + "PID " + pid + " is used more than once.");
            int arrival = Validation.parseInt(where + "Arrival Time", cell(r, 1), 0, Validation.MAX_TIME_VALUE);
            int burst = Validation.parseInt(where + "Burst Time", cell(r, 2), 1, Validation.MAX_TIME_VALUE);
            int prio = priority ? Validation.parseInt(where + "Priority", cell(r, 3), 0, Validation.MAX_PRIORITY) : 0;
            list.add(new ProcessInfo(pid, arrival, burst, prio));
        }
        if (list.isEmpty()) throw new ValidationException("Add at least one process.");
        return list;
    }

    private String cell(int row, int col) {
        Object v = inputModel.getValueAt(row, col);
        return v == null ? "" : v.toString();
    }

    // -------------------------------------------------------------- actions
    private void run() {
        try {
            List<ProcessInfo> procs = readProcesses();
            int quantum = algorithm.usesQuantum()
                    ? Validation.parseInt("Time quantum", quantumField.getText(), 1, Validation.MAX_TIME_VALUE)
                    : 0;
            show(algorithm.schedule(procs, quantum));
            setStatus("Simulation complete.", false);
        } catch (ValidationException e) {
            error(e.getMessage());
        } catch (RuntimeException e) {
            error("Unexpected problem: " + e.getMessage());
        }
    }

    private void show(CpuResult res) {
        gantt.setResult(res);
        resultModel.setRowCount(0);
        for (CpuResult.Row r : res.rows) {
            List<Object> row = new ArrayList<>(List.of(r.pid, r.arrival, r.burst));
            if (priority) row.add(r.priority);
            row.addAll(List.of(r.completion, r.turnaround, r.waiting, r.response));
            resultModel.addRow(row.toArray());
        }
        summary.setText(String.format(Locale.ROOT,
                "Average waiting time    : %.2f%n"
                + "Average turnaround time : %.2f%n"
                + "Average response time   : %.2f%n"
                + "CPU utilization         : %.2f %%%n"
                + "Throughput              : %.4f process/unit%n"
                + "Total time %d  (busy %d, idle %d)",
                res.avgWaiting, res.avgTurnaround, res.avgResponse, res.utilization, res.throughput,
                res.totalTime, res.busyTime, res.idleTime));
        summary.setCaretPosition(0);
    }

    private void loadSample() {
        stopEditing();
        int[][] data = Samples.CPU_PROCESSES;
        countSpinner.setValue(data.length);
        resizeRows(data.length);
        for (int r = 0; r < data.length; r++) {
            inputModel.setValueAt(String.valueOf(data[r][0]), r, 0);
            inputModel.setValueAt(String.valueOf(data[r][1]), r, 1);
            inputModel.setValueAt(String.valueOf(data[r][2]), r, 2);
            if (priority) inputModel.setValueAt(String.valueOf(data[r][3]), r, 3);
        }
        quantumField.setText(String.valueOf(Samples.CPU_QUANTUM));
        clearOutput();
        setStatus("Sample data loaded. Press Run Simulation.", false);
    }

    private void reset() {
        stopEditing();
        countSpinner.setValue(DEFAULT_ROWS);
        inputModel.setRowCount(0);
        resizeRows(DEFAULT_ROWS);
        quantumField.setText("2");
        clearOutput();
        setStatus("Cleared.", false);
    }

    private void clearOutput() {
        gantt.setResult(null);
        resultModel.setRowCount(0);
        summary.setText("");
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
