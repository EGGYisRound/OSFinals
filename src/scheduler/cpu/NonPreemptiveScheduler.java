package scheduler.cpu;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Template for non-preemptive CPU algorithms. Subclasses only supply the
 * selection order; the loop (idle handling, run-to-completion) lives here.
 * Tie-break (Section 4.3): when keys tie, earlier arrival first, then lower PID.
 */
abstract class NonPreemptiveScheduler implements CpuScheduler {

    /** Orders READY processes; the smallest one runs next. Must end with arrival, pid. */
    protected abstract Comparator<ProcessInfo> order();

    @Override public boolean isPreemptive() { return false; }
    @Override public boolean usesQuantum() { return false; }
    @Override public boolean usesPriority() { return false; }

    @Override
    public CpuResult schedule(List<ProcessInfo> processes, int quantum) {
        List<CpuResult.Slice> slices = new ArrayList<>();
        List<ProcessInfo> pending = new ArrayList<>(processes);
        Comparator<ProcessInfo> cmp = order();
        int time = 0;
        while (!pending.isEmpty()) {
            ProcessInfo next = null;
            int nextArrival = Integer.MAX_VALUE;
            for (ProcessInfo p : pending) {
                if (p.arrival <= time) {
                    if (next == null || cmp.compare(p, next) < 0) next = p;
                } else {
                    nextArrival = Math.min(nextArrival, p.arrival);
                }
            }
            if (next == null) { // CPU idle until the next arrival
                slices.add(new CpuResult.Slice(CpuResult.IDLE, time, nextArrival));
                time = nextArrival;
                continue;
            }
            slices.add(new CpuResult.Slice(next.pid, time, time + next.burst));
            time += next.burst;
            pending.remove(next);
        }
        return CpuResult.build(processes, slices);
    }

    /** Common tail of every comparator: arrival, then PID. */
    protected static int tieBreak(ProcessInfo a, ProcessInfo b) {
        if (a.arrival != b.arrival) return Integer.compare(a.arrival, b.arrival);
        return Integer.compare(a.pid, b.pid);
    }
}
