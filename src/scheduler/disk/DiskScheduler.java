package scheduler.disk;

import java.util.List;

/**
 * Shared interface for every disk scheduling algorithm (agreed at the Week 11
 * design checkpoint). Implementations contain NO user-interface code.
 */
public interface DiskScheduler {
    String getName();

    /** True if the initial direction matters (SCAN, C-SCAN, LOOK, C-LOOK). */
    boolean usesDirection();

    /**
     * @param requests  queue of cylinder numbers (duplicates allowed)
     * @param head      initial head position
     * @param diskSize  number of cylinders N (cylinders run 0..N-1)
     * @param direction initial direction (ignored unless {@link #usesDirection()})
     * @throws IllegalArgumentException if any input is out of range
     */
    DiskResult schedule(List<Integer> requests, int head, int diskSize, Direction direction);
}
