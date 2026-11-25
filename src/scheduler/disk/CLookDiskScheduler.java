package scheduler.disk;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Owner: <member name>. Circular LOOK.
 * Serves requests in the initial direction up to the last pending request, then
 * jumps straight to the request at the far side (the return jump IS counted in
 * head movement and shown in the path) and continues in the same direction.
 */
public final class CLookDiskScheduler extends AbstractDiskScheduler {
    @Override public String getName() { return "C-LOOK"; }
    @Override public boolean usesDirection() { return true; }

    @Override
    protected DiskResult run(List<Integer> reqs, int head, int diskSize, Direction dir) {
        DiskResult.Trace trace = new DiskResult.Trace(head, diskSize);
        for (int r : forward(reqs, head, dir)) trace.serve(r);

        List<Integer> wrapped = new ArrayList<>(backward(reqs, head, dir));
        Collections.reverse(wrapped); // far-side request first, then onward in the same direction
        for (int i = 0; i < wrapped.size(); i++) {
            if (i == 0) trace.returnJumpAndServe(wrapped.get(i));
            else trace.serve(wrapped.get(i));
        }
        return trace.finish();
    }
}
