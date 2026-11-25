package scheduler.disk;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Owner: <member name>. Circular SCAN.
 * Serves requests in the initial direction up to the PHYSICAL END, then jumps
 * back to the opposite end (the return jump IS counted in head movement and
 * shown in the path) and continues in the same direction.
 */
public final class CScanDiskScheduler extends AbstractDiskScheduler {
    @Override public String getName() { return "C-SCAN"; }
    @Override public boolean usesDirection() { return true; }

    @Override
    protected DiskResult run(List<Integer> reqs, int head, int diskSize, Direction dir) {
        DiskResult.Trace trace = new DiskResult.Trace(head, diskSize);
        for (int r : forward(reqs, head, dir)) trace.serve(r);
        trace.reachEnd(endOf(dir, diskSize));

        List<Integer> wrapped = new ArrayList<>(backward(reqs, head, dir));
        if (!wrapped.isEmpty()) {
            trace.returnJump(oppositeEndOf(dir, diskSize));
            Collections.reverse(wrapped); // continue in the same direction, from the far end
            for (int r : wrapped) trace.serve(r);
        }
        return trace.finish();
    }
}
