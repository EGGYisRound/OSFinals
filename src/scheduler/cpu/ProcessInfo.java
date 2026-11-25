package scheduler.cpu;

/** Immutable input description of one process. */
public final class ProcessInfo {
    public final int pid;
    public final int arrival;
    public final int burst;
    public final int priority; // lower number = higher priority

    public ProcessInfo(int pid, int arrival, int burst, int priority) {
        this.pid = pid;
        this.arrival = arrival;
        this.burst = burst;
        this.priority = priority;
    }
}
