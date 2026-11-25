package scheduler.ui;

import scheduler.disk.Direction;

/**
 * Data behind the "Load Sample Data" buttons. Replace these with the
 * facilitator's practice problem sets if you want them one click away.
 */
final class Samples {
    private Samples() { }

    /** {pid, arrival, burst, priority}; includes an idle gap before P6. */
    static final int[][] CPU_PROCESSES = {
        {1, 0, 8, 3},
        {2, 1, 4, 1},
        {3, 2, 9, 4},
        {4, 3, 5, 2},
        {5, 4, 2, 2},
        {6, 30, 3, 1},
    };
    static final int CPU_QUANTUM = 3;

    static final String DISK_REQUESTS = "98, 183, 37, 122, 14, 124, 65, 67";
    static final int DISK_HEAD = 53;
    static final int DISK_SIZE = 200;
    static final Direction DISK_DIRECTION = Direction.UP;
}
