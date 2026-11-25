package scheduler.disk;

import java.util.List;

/**
 * Owner: <member name>. LOOK.
 * Like SCAN, but the head turns around at the LAST PENDING REQUEST in the
 * current direction instead of travelling to the disk end.
 */
public final class LookDiskScheduler extends AbstractDiskScheduler {
    @Override public String getName() { return "LOOK"; }
    @Override public boolean usesDirection() { return true; }

    @Override
    protected DiskResult run(List<Integer> reqs, int head, int diskSize, Direction dir) {
        DiskResult.Trace trace = new DiskResult.Trace(head, diskSize);
        for (int r : forward(reqs, head, dir)) trace.serve(r);
        for (int r : backward(reqs, head, dir)) trace.serve(r);
        return trace.finish();
    }
}
