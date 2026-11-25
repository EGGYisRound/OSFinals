package scheduler.cpu;

import java.util.Comparator;

/** Owner: <member name>. First-Come First-Served (non-preemptive). */
public final class FcfsScheduler extends NonPreemptiveScheduler {
    @Override public String getName() { return "First-Come First-Served (FCFS)"; }

    @Override
    protected Comparator<ProcessInfo> order() {
        return NonPreemptiveScheduler::tieBreak; // arrival, then PID
    }
}
