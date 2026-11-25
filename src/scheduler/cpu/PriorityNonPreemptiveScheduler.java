package scheduler.cpu;

import java.util.Comparator;

/** Owner: <member name>. Priority scheduling, non-preemptive (lower number = higher priority, no aging). */
public final class PriorityNonPreemptiveScheduler extends NonPreemptiveScheduler {
    @Override public String getName() { return "Priority (Non-preemptive)"; }
    @Override public boolean usesPriority() { return true; }

    @Override
    protected Comparator<ProcessInfo> order() {
        return (a, b) -> a.priority != b.priority ? Integer.compare(a.priority, b.priority) : tieBreak(a, b);
    }
}
