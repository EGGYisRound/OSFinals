package scheduler.cpu;

import java.util.List;

/**
 * Shared interface for every CPU scheduling algorithm (agreed at the Week 11
 * design checkpoint). Implementations contain NO user-interface code.
 */
public interface CpuScheduler {
    String getName();

    boolean isPreemptive();

    /** True if the algorithm reads {@link ProcessInfo#priority}. */
    boolean usesPriority();

    /** True if the algorithm needs a time quantum (Round Robin). */
    boolean usesQuantum();

    /**
     * Runs the simulation.
     *
     * @param processes processes to schedule (any order)
     * @param quantum   time quantum, ignored unless {@link #usesQuantum()}
     */
    CpuResult schedule(List<ProcessInfo> processes, int quantum);
}
