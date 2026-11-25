package scheduler.cpu;

/** Owner: <member name>. Shortest Remaining Time First (preemptive). */
public final class SrtfScheduler extends PreemptiveScheduler {
    @Override public String getName() { return "Shortest Remaining Time First (SRTF)"; }

    @Override
    protected int key(ProcessInfo p, int remaining) {
        return remaining;
    }
}
