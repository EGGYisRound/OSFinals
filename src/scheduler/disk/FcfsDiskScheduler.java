package scheduler.disk;

import java.util.List;

/** Owner: <member name>. Disk FCFS: requests are served in arrival order. */
public final class FcfsDiskScheduler extends AbstractDiskScheduler {
    @Override public String getName() { return "FCFS"; }

    @Override
    protected DiskResult run(List<Integer> requests, int head, int diskSize, Direction dir) {
        DiskResult.Trace trace = new DiskResult.Trace(head, diskSize);
        for (int r : requests) trace.serve(r);
        return trace.finish();
    }
}
