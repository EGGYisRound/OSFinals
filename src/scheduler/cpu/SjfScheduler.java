package scheduler.cpu;

import java.util.Comparator;

/** Owner: <member name>. Shortest Job First (non-preemptive). */
public final class SjfScheduler extends NonPreemptiveScheduler {
    @Override public String getName() { return "Shortest Job First (SJF)"; }

    @Override
    protected Comparator<ProcessInfo> order() {
        return (a, b) -> a.burst != b.burst ? Integer.compare(a.burst, b.burst) : tieBreak(a, b);
    }
}
