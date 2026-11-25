package scheduler.cpu;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Queue;

/**
 * Owner: <member name>. Round Robin (preemptive, time quantum).
 *
 * Convention (Section 4.3): if a new arrival and a preempted process enter the
 * ready queue at the same instant, the NEW ARRIVAL is queued first.
 * Processes that arrive at the same time are queued by lower PID.
 */
public final class RoundRobinScheduler implements CpuScheduler {
    @Override public String getName() { return "Round Robin (RR)"; }
    @Override public boolean isPreemptive() { return true; }
    @Override public boolean usesPriority() { return false; }
    @Override public boolean usesQuantum() { return true; }

    @Override
    public CpuResult schedule(List<ProcessInfo> processes, int quantum) {
        if (quantum < 1) throw new IllegalArgumentException("Time quantum must be at least 1.");
        List<ProcessInfo> byArrival = new ArrayList<>(processes);
        byArrival.sort(Comparator.<ProcessInfo>comparingInt(p -> p.arrival).thenComparingInt(p -> p.pid));

        int n = byArrival.size();
        int[] remaining = new int[n];
        for (int i = 0; i < n; i++) remaining[i] = byArrival.get(i).burst;

        List<CpuResult.Slice> slices = new ArrayList<>();
        Queue<Integer> ready = new ArrayDeque<>();
        int next = 0;  // index of next process not yet queued
        int time = 0, finished = 0;

        while (next < n && byArrival.get(next).arrival <= time) ready.add(next++);

        while (finished < n) {
            if (ready.isEmpty()) { // idle until the next arrival
                int arrival = byArrival.get(next).arrival;
                slices.add(new CpuResult.Slice(CpuResult.IDLE, time, arrival));
                time = arrival;
                while (next < n && byArrival.get(next).arrival <= time) ready.add(next++);
                continue;
            }
            int i = ready.poll();
            int run = Math.min(quantum, remaining[i]);
            slices.add(new CpuResult.Slice(byArrival.get(i).pid, time, time + run));
            time += run;
            remaining[i] -= run;
            // new arrivals (including those arriving exactly now) go BEFORE the preempted process
            while (next < n && byArrival.get(next).arrival <= time) ready.add(next++);
            if (remaining[i] > 0) ready.add(i);
            else finished++;
        }
        return CpuResult.build(processes, slices);
    }
}
