package scheduler.disk;

import java.util.List;

/**
 * Owner: <member name>. SCAN (elevator).
 * The head sweeps in the initial direction, serving requests on the way, and
 * always travels to the PHYSICAL END of the disk (Section 4.3). It then
 * reverses and serves the remaining requests until the last one.
 */
public final class ScanDiskScheduler extends AbstractDiskScheduler {
    @Override public String getName() { return "SCAN"; }
    @Override public boolean usesDirection() { return true; }

    @Override
    protected DiskResult run(List<Integer> reqs, int head, int diskSize, Direction dir) {
        DiskResult.Trace trace = new DiskResult.Trace(head, diskSize);
        for (int r : forward(reqs, head, dir)) trace.serve(r);
        trace.reachEnd(endOf(dir, diskSize));
        for (int r : backward(reqs, head, dir)) trace.serve(r);
        return trace.finish();
    }
}
