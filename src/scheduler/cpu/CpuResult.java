package scheduler.cpu;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Result of a CPU scheduling simulation. All metrics are derived from the
 * Gantt chart in ONE place ({@link #build}), so every algorithm computes the
 * metrics identically (Section 4.3 formulas):
 * Turnaround = Completion - Arrival; Waiting = Turnaround - Burst;
 * Response = First CPU time - Arrival; Utilization = busy / total * 100;
 * Throughput = processes / total time.
 */
public final class CpuResult {
    public static final int IDLE = -1;

    /** One block of the Gantt chart. pid == IDLE for idle CPU. */
    public static final class Slice {
        public final int pid;
        public final int start;
        public final int end;

        public Slice(int pid, int start, int end) {
            this.pid = pid;
            this.start = start;
            this.end = end;
        }
    }

    /** Per-process row of the result table. */
    public static final class Row {
        public final int pid, arrival, burst, priority;
        public final int firstStart, completion, turnaround, waiting, response;

        Row(ProcessInfo p, int firstStart, int completion) {
            this.pid = p.pid;
            this.arrival = p.arrival;
            this.burst = p.burst;
            this.priority = p.priority;
            this.firstStart = firstStart;
            this.completion = completion;
            this.turnaround = completion - p.arrival;
            this.waiting = turnaround - p.burst;
            this.response = firstStart - p.arrival;
        }
    }

    public final List<Slice> gantt;
    public final List<Row> rows;
    public final int totalTime;
    public final int busyTime;
    public final int idleTime;
    public final double avgTurnaround, avgWaiting, avgResponse;
    public final double utilization; // percent
    public final double throughput;  // processes per time unit

    private CpuResult(List<Slice> gantt, List<Row> rows, int total, int busy) {
        this.gantt = Collections.unmodifiableList(gantt);
        this.rows = Collections.unmodifiableList(rows);
        this.totalTime = total;
        this.busyTime = busy;
        this.idleTime = total - busy;
        int n = rows.size();
        double t = 0, w = 0, r = 0;
        for (Row row : rows) {
            t += row.turnaround;
            w += row.waiting;
            r += row.response;
        }
        this.avgTurnaround = n == 0 ? 0 : t / n;
        this.avgWaiting = n == 0 ? 0 : w / n;
        this.avgResponse = n == 0 ? 0 : r / n;
        this.utilization = total == 0 ? 0 : 100.0 * busy / total;
        this.throughput = total == 0 ? 0 : (double) n / total;
    }

    /**
     * Builds a result from raw time slices (in chronological order, covering
     * time 0 up to the last completion). Adjacent slices of the same process
     * (or idle) are merged. Rows keep the order of {@code procs}.
     */
    static CpuResult build(List<ProcessInfo> procs, List<Slice> raw) {
        List<Slice> merged = new ArrayList<>();
        for (Slice s : raw) {
            if (s.end <= s.start) continue;
            if (!merged.isEmpty()) {
                Slice last = merged.get(merged.size() - 1);
                if (last.pid == s.pid && last.end == s.start) {
                    merged.set(merged.size() - 1, new Slice(s.pid, last.start, s.end));
                    continue;
                }
            }
            merged.add(s);
        }
        int total = merged.isEmpty() ? 0 : merged.get(merged.size() - 1).end;
        int busy = 0;
        for (Slice s : merged) if (s.pid != IDLE) busy += s.end - s.start;

        List<Row> rows = new ArrayList<>();
        for (ProcessInfo p : procs) {
            int first = -1, done = -1;
            for (Slice s : merged) {
                if (s.pid != p.pid) continue;
                if (first < 0) first = s.start;
                done = s.end;
            }
            rows.add(new Row(p, first, done));
        }
        return new CpuResult(merged, rows, total, busy);
    }
}
