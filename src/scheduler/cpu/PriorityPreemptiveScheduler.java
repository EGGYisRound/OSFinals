package scheduler.cpu;

/** Owner: <member name>. Priority scheduling, preemptive (lower number = higher priority, no aging). */
public final class PriorityPreemptiveScheduler extends PreemptiveScheduler {
    @Override public String getName() { return "Priority (Preemptive)"; }
    @Override public boolean usesPriority() { return true; }

    @Override
    protected int key(ProcessInfo p, int remaining) {
        return p.priority;
    }
}
