package scheduler.disk;

import java.util.List;

/**
 * Owner: <member name>. Shortest Seek Time First.
 * Always serves the pending request nearest to the head; a distance tie goes to
 * the LOWER cylinder (Section 4.3). Duplicates are at distance 0, so they are
 * served consecutively.
 */
public final class SstfDiskScheduler extends AbstractDiskScheduler {
    @Override public String getName() { return "Shortest Seek Time First (SSTF)"; }

    @Override
    protected DiskResult run(List<Integer> pending, int head, int diskSize, Direction dir) {
        DiskResult.Trace trace = new DiskResult.Trace(head, diskSize);
        while (!pending.isEmpty()) {
            int bestIndex = 0;
            for (int i = 1; i < pending.size(); i++) {
                int d = Math.abs(pending.get(i) - trace.position());
                int bd = Math.abs(pending.get(bestIndex) - trace.position());
                if (d < bd || (d == bd && pending.get(i) < pending.get(bestIndex))) bestIndex = i;
            }
            trace.serve(pending.remove(bestIndex));
        }
        return trace.finish();
    }
}
