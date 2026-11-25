package scheduler.disk;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Result of a disk scheduling simulation. Head movement is the sum of the
 * absolute differences between successive head positions, starting from the
 * initial head position (Section 4.3). The return jump of C-SCAN / C-LOOK is
 * counted. Average seek distance = total head movement / number of requests.
 */
public final class DiskResult {

    /** Why the head moved in a step. */
    public enum Kind {
        SERVE("Serve request"),
        REACH_END("Travel to disk end"),
        RETURN("Return jump"),
        RETURN_SERVE("Return jump + serve request");

        private final String label;
        Kind(String label) { this.label = label; }
        public String label() { return label; }
        public boolean serves() { return this == SERVE || this == RETURN_SERVE; }
    }

    /** One head movement. */
    public static final class Step {
        public final int from, to, distance;
        public final Kind kind;

        Step(int from, int to, Kind kind) {
            this.from = from;
            this.to = to;
            this.distance = Math.abs(to - from);
            this.kind = kind;
        }
    }

    public final int initialHead;
    public final int diskSize;
    public final List<Step> steps;
    public final List<Integer> serviceOrder;
    public final int totalMovement;
    public final double averageSeek;

    private DiskResult(int head, int diskSize, List<Step> steps, List<Integer> order) {
        this.initialHead = head;
        this.diskSize = diskSize;
        this.steps = Collections.unmodifiableList(steps);
        this.serviceOrder = Collections.unmodifiableList(order);
        int total = 0;
        for (Step s : steps) total += s.distance;
        this.totalMovement = total;
        this.averageSeek = order.isEmpty() ? 0 : (double) total / order.size();
    }

    /** Positions visited by the head, starting with the initial position (for the path graph). */
    public List<Integer> path() {
        List<Integer> p = new ArrayList<>();
        p.add(initialHead);
        for (Step s : steps) p.add(s.to);
        return p;
    }

    /** Records head movements while an algorithm runs. */
    static final class Trace {
        private final int head, diskSize;
        private int current;
        private final List<Step> steps = new ArrayList<>();
        private final List<Integer> order = new ArrayList<>();

        Trace(int head, int diskSize) {
            this.head = head;
            this.diskSize = diskSize;
            this.current = head;
        }

        int position() { return current; }

        void serve(int cylinder) { move(cylinder, Kind.SERVE); }

        void reachEnd(int cylinder) { if (cylinder != current) move(cylinder, Kind.REACH_END); }

        void returnJump(int cylinder) { move(cylinder, Kind.RETURN); }

        void returnJumpAndServe(int cylinder) { move(cylinder, Kind.RETURN_SERVE); }

        private void move(int to, Kind kind) {
            steps.add(new Step(current, to, kind));
            if (kind.serves()) order.add(to);
            current = to;
        }

        DiskResult finish() { return new DiskResult(head, diskSize, steps, order); }
    }
}
