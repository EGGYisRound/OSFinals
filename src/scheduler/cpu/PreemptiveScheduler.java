package scheduler.cpu;

import java.util.ArrayList;
import java.util.List;

/**
 * Template for preemptive CPU algorithms driven by a numeric key
 * (remaining time for SRTF, priority number for Priority).
 *
 * The simulation advances one time unit at a time. The running process is
 * replaced ONLY if another ready process has a STRICTLY better (smaller) key
 * (Section 4.3: "preempts only if strictly less / strictly higher").
 * When the running process finishes, the next one is chosen by key, then
 * earlier arrival, then lower PID.
 */
abstract class PreemptiveScheduler implements CpuScheduler {

    /** Smaller key = more urgent. */
    protected abstract int key(ProcessInfo p, int remaining);

    @Override public boolean isPreemptive() { return true; }
    @Override public boolean usesQuantum() { return false; }
    @Override public boolean usesPriority() { return false; }

    @Override
    public CpuResult schedule(List<ProcessInfo> processes, int quantum) {
        int n = processes.size();
        int[] remaining = new int[n];
        for (int i = 0; i < n; i++) remaining[i] = processes.get(i).burst;

        List<CpuResult.Slice> slices = new ArrayList<>();
        int time = 0, finished = 0, running = -1;

        while (finished < n) {
            int best = -1;
            int nextArrival = Integer.MAX_VALUE;
            for (int i = 0; i < n; i++) {
                if (remaining[i] == 0) continue;
                ProcessInfo p = processes.get(i);
                if (p.arrival > time) {
                    nextArrival = Math.min(nextArrival, p.arrival);
                } else if (best < 0 || better(i, best, processes, remaining)) {
                    best = i;
                }
            }
            if (best < 0) { // idle
                slices.add(new CpuResult.Slice(CpuResult.IDLE, time, nextArrival));
                time = nextArrival;
                running = -1;
                continue;
            }
            // keep the current process unless the candidate is strictly better
            if (running >= 0 && remaining[running] > 0 && best != running
                    && key(processes.get(best), remaining[best])
                    >= key(processes.get(running), remaining[running])) {
                best = running;
            }
            ProcessInfo p = processes.get(best);
            slices.add(new CpuResult.Slice(p.pid, time, time + 1));
            time++;
            remaining[best]--;
            if (remaining[best] == 0) {
                finished++;
                running = -1;
            } else {
                running = best;
            }
        }
        return CpuResult.build(processes, slices);
    }

    private boolean better(int i, int j, List<ProcessInfo> ps, int[] rem) {
        ProcessInfo a = ps.get(i), b = ps.get(j);
        int ka = key(a, rem[i]), kb = key(b, rem[j]);
        if (ka != kb) return ka < kb;
        if (a.arrival != b.arrival) return a.arrival < b.arrival;
        return a.pid < b.pid;
    }
}
