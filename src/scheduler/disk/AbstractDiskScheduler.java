package scheduler.disk;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Validates input once and offers helpers shared by the algorithms.
 * Cylinders run from 0 to N-1 (default N = 200).
 */
abstract class AbstractDiskScheduler implements DiskScheduler {

    @Override public boolean usesDirection() { return false; }

    @Override
    public final DiskResult schedule(List<Integer> requests, int head, int diskSize, Direction direction) {
        if (diskSize < 2) throw new IllegalArgumentException("Disk size must be at least 2 cylinders.");
        if (head < 0 || head >= diskSize)
            throw new IllegalArgumentException("Initial head must be between 0 and " + (diskSize - 1) + ".");
        if (requests == null || requests.isEmpty())
            throw new IllegalArgumentException("Enter at least one request.");
        for (int r : requests) {
            if (r < 0 || r >= diskSize)
                throw new IllegalArgumentException("Request " + r + " is outside 0.." + (diskSize - 1) + ".");
        }
        Direction dir = direction == null ? Direction.UP : direction;
        return run(new ArrayList<>(requests), head, diskSize, dir);
    }

    protected abstract DiskResult run(List<Integer> requests, int head, int diskSize, Direction dir);

    /**
     * Requests the head meets FIRST when moving in {@code dir}: those at or beyond
     * the head, nearest first. A request on the head's own cylinder is served first
     * at zero movement; duplicates are served consecutively.
     */
    protected static List<Integer> forward(List<Integer> reqs, int head, Direction dir) {
        List<Integer> out = new ArrayList<>();
        for (int r : reqs) if (dir == Direction.UP ? r >= head : r <= head) out.add(r);
        Collections.sort(out);
        if (dir == Direction.DOWN) Collections.reverse(out);
        return out;
    }

    /** Requests behind the head (served after the direction reverses), nearest to the head first. */
    protected static List<Integer> backward(List<Integer> reqs, int head, Direction dir) {
        List<Integer> out = new ArrayList<>();
        for (int r : reqs) if (dir == Direction.UP ? r < head : r > head) out.add(r);
        Collections.sort(out);
        if (dir == Direction.UP) Collections.reverse(out);
        return out;
    }

    protected static int endOf(Direction dir, int diskSize) {
        return dir == Direction.UP ? diskSize - 1 : 0;
    }

    protected static int oppositeEndOf(Direction dir, int diskSize) {
        return dir == Direction.UP ? 0 : diskSize - 1;
    }
}
