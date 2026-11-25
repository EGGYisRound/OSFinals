package scheduler.disk;

/** Initial head direction for SCAN, C-SCAN, LOOK and C-LOOK. */
public enum Direction {
    UP("Toward higher cylinders"),
    DOWN("Toward lower cylinders");

    private final String label;

    Direction(String label) { this.label = label; }

    @Override public String toString() { return label; }
}
