package scheduler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import scheduler.cpu.*;
import scheduler.disk.*;
import scheduler.util.Validation;
import scheduler.util.ValidationException;

/**
 * Plain-Java test runner (no external libraries). Expected values were solved
 * by hand. Run with: ./build.sh test
 */
public final class AlgorithmTests {
    private static int passed = 0, failed = 0;

    public static void main(String[] args) {
        cpuTests();
        diskTests();
        validationTests();
        System.out.println();
        System.out.println(passed + " passed, " + failed + " failed");
        if (failed > 0) System.exit(1);
    }

    // ------------------------------------------------------------------ CPU
    private static List<ProcessInfo> list(int[]... rows) {
        List<ProcessInfo> l = new ArrayList<>();
        for (int[] r : rows) l.add(new ProcessInfo(r[0], r[1], r[2], r.length > 3 ? r[3] : 0));
        return l;
    }

    private static String gantt(CpuResult r) {
        StringBuilder sb = new StringBuilder();
        for (CpuResult.Slice s : r.gantt)
            sb.append(s.pid == CpuResult.IDLE ? "I" : "P" + s.pid).append('[').append(s.start).append('-').append(s.end).append("] ");
        return sb.toString().trim();
    }

    private static void cpuTests() {
        // Textbook set: P1 0/8, P2 1/4, P3 2/9, P4 3/5
        List<ProcessInfo> book = list(new int[]{1, 0, 8}, new int[]{2, 1, 4}, new int[]{3, 2, 9}, new int[]{4, 3, 5});

        CpuResult fcfs = new FcfsScheduler().schedule(book, 0);
        eq("FCFS gantt", "P1[0-8] P2[8-12] P3[12-21] P4[21-26]", gantt(fcfs));
        near("FCFS avg waiting", 8.75, fcfs.avgWaiting); // (0+7+10+18)/4

        CpuResult sjf = new SjfScheduler().schedule(book, 0);
        eq("SJF gantt", "P1[0-8] P2[8-12] P4[12-17] P3[17-26]", gantt(sjf));
        near("SJF avg waiting", 7.75, sjf.avgWaiting);

        CpuResult srtf = new SrtfScheduler().schedule(book, 0);
        eq("SRTF gantt", "P1[0-1] P2[1-5] P4[5-10] P1[10-17] P3[17-26]", gantt(srtf));
        near("SRTF avg waiting", 6.5, srtf.avgWaiting);
        near("SRTF avg turnaround", 13.0, srtf.avgTurnaround);
        near("SRTF avg response", 4.25, srtf.avgResponse); // 0,0,15,2 = 17/4

        // Round Robin q=4: P1 24, P2 3, P3 3 all at 0
        List<ProcessInfo> rr = list(new int[]{1, 0, 24}, new int[]{2, 0, 3}, new int[]{3, 0, 3});
        CpuResult r1 = new RoundRobinScheduler().schedule(rr, 4);
        eq("RR gantt", "P1[0-4] P2[4-7] P3[7-10] P1[10-30]", gantt(r1));
        near("RR avg waiting", 17.0 / 3, r1.avgWaiting); // 6,4,7

        // RR convention: new arrival queued BEFORE the preempted process
        CpuResult r2 = new RoundRobinScheduler().schedule(list(new int[]{1, 0, 4}, new int[]{2, 2, 2}), 2);
        eq("RR same-instant tie", "P1[0-2] P2[2-4] P1[4-6]", gantt(r2));

        // Idle CPU
        CpuResult idle = new FcfsScheduler().schedule(list(new int[]{1, 3, 2}), 0);
        eq("Idle gantt", "I[0-3] P1[3-5]", gantt(idle));
        near("Idle utilization", 40.0, idle.utilization);
        near("Idle throughput", 0.2, idle.throughput);

        // Idle gap in the middle
        CpuResult gap = new SjfScheduler().schedule(list(new int[]{1, 0, 2}, new int[]{2, 5, 1}), 0);
        eq("Idle gap gantt", "P1[0-2] I[2-5] P2[5-6]", gantt(gap));

        // Priority (lower number = higher priority); ties -> earlier arrival -> lower PID
        List<ProcessInfo> pri = list(new int[]{1, 0, 10, 3}, new int[]{2, 0, 1, 1}, new int[]{3, 0, 2, 4},
                new int[]{4, 0, 1, 5}, new int[]{5, 0, 5, 2});
        CpuResult pnp = new PriorityNonPreemptiveScheduler().schedule(pri, 0);
        eq("Priority NP gantt", "P2[0-1] P5[1-6] P1[6-16] P3[16-18] P4[18-19]", gantt(pnp));
        near("Priority NP avg waiting", 8.2, pnp.avgWaiting); // 6,0,16,18,1

        CpuResult tie = new PriorityNonPreemptiveScheduler().schedule(
                list(new int[]{2, 0, 3, 1}, new int[]{1, 0, 3, 1}), 0);
        eq("Priority tie -> lower PID", "P1[0-3] P2[3-6]", gantt(tie));

        // Preemptive priority: strictly higher priority preempts, equal does not
        List<ProcessInfo> pp = list(new int[]{1, 0, 4, 2}, new int[]{2, 1, 3, 1}, new int[]{3, 2, 1, 1});
        CpuResult pre = new PriorityPreemptiveScheduler().schedule(pp, 0);
        eq("Priority P gantt", "P1[0-1] P2[1-4] P3[4-5] P1[5-8]", gantt(pre));

        // SRTF: equal remaining time does NOT preempt
        CpuResult eqR = new SrtfScheduler().schedule(list(new int[]{1, 0, 4}, new int[]{2, 1, 3}), 0);
        eq("SRTF no preempt on equal", "P1[0-4] P2[4-7]", gantt(eqR));

        // Single process
        CpuResult one = new RoundRobinScheduler().schedule(list(new int[]{1, 0, 7}), 3);
        eq("Single process RR", "P1[0-7]", gantt(one));
        near("Single process utilization", 100.0, one.utilization);

        // Support at least 10 processes
        List<ProcessInfo> ten = new ArrayList<>();
        for (int i = 1; i <= 12; i++) ten.add(new ProcessInfo(i, i - 1, 2, i % 3));
        for (CpuScheduler s : allCpu()) {
            CpuResult res = s.schedule(ten, 3);
            check(s.getName() + " handles 12 processes, busy=24", res.busyTime == 24 && res.rows.size() == 12);
        }
    }

    private static List<CpuScheduler> allCpu() {
        return Arrays.<CpuScheduler>asList(new FcfsScheduler(), new SjfScheduler(), new PriorityNonPreemptiveScheduler(),
                new SrtfScheduler(), new PriorityPreemptiveScheduler(), new RoundRobinScheduler());
    }

    // ----------------------------------------------------------------- Disk
    private static final List<Integer> Q = Arrays.asList(98, 183, 37, 122, 14, 124, 65, 67);

    private static void diskTests() {
        DiskResult fcfs = new FcfsDiskScheduler().schedule(Q, 53, 200, Direction.UP);
        eq("Disk FCFS total", 640, fcfs.totalMovement);
        near("Disk FCFS avg seek", 80.0, fcfs.averageSeek);

        DiskResult sstf = new SstfDiskScheduler().schedule(Q, 53, 200, Direction.UP);
        eq("Disk SSTF order", "[65, 67, 37, 14, 98, 122, 124, 183]", sstf.serviceOrder.toString());
        eq("Disk SSTF total", 236, sstf.totalMovement);

        DiskResult scan = new ScanDiskScheduler().schedule(Q, 53, 200, Direction.UP);
        eq("Disk SCAN up order", "[65, 67, 98, 122, 124, 183, 37, 14]", scan.serviceOrder.toString());
        eq("Disk SCAN up path", "[53, 65, 67, 98, 122, 124, 183, 199, 37, 14]", scan.path().toString());
        eq("Disk SCAN up total", 331, scan.totalMovement);

        DiskResult scanDown = new ScanDiskScheduler().schedule(Q, 53, 200, Direction.DOWN);
        eq("Disk SCAN down total", 236, scanDown.totalMovement); // 53->0 (53) then 0->183 (183)

        DiskResult cscan = new CScanDiskScheduler().schedule(Q, 53, 200, Direction.UP);
        eq("Disk C-SCAN path", "[53, 65, 67, 98, 122, 124, 183, 199, 0, 14, 37]", cscan.path().toString());
        eq("Disk C-SCAN total", 382, cscan.totalMovement);

        DiskResult look = new LookDiskScheduler().schedule(Q, 53, 200, Direction.UP);
        eq("Disk LOOK total", 299, look.totalMovement);

        DiskResult clook = new CLookDiskScheduler().schedule(Q, 53, 200, Direction.UP);
        eq("Disk C-LOOK path", "[53, 65, 67, 98, 122, 124, 183, 14, 37]", clook.path().toString());
        eq("Disk C-LOOK total", 322, clook.totalMovement);

        // SSTF tie -> lower cylinder (head 50, requests 40 and 60)
        DiskResult tie = new SstfDiskScheduler().schedule(Arrays.asList(60, 40), 50, 200, Direction.UP);
        eq("Disk SSTF tie lower first", "[40, 60]", tie.serviceOrder.toString());

        // Duplicates served consecutively with zero movement
        DiskResult dup = new FcfsDiskScheduler().schedule(Arrays.asList(10, 10, 10), 10, 200, Direction.UP);
        eq("Disk duplicates total", 0, dup.totalMovement);
        DiskResult dup2 = new ScanDiskScheduler().schedule(Arrays.asList(20, 20, 5), 10, 50, Direction.UP);
        eq("Disk SCAN duplicates order", "[20, 20, 5]", dup2.serviceOrder.toString());

        // Head at a boundary
        DiskResult atEnd = new ScanDiskScheduler().schedule(Arrays.asList(10, 20), 199, 200, Direction.UP);
        eq("Disk SCAN head at end", "[199, 20, 10]", atEnd.path().toString());
        DiskResult atZero = new CLookDiskScheduler().schedule(Arrays.asList(10, 20), 0, 200, Direction.DOWN);
        eq("Disk C-LOOK head at 0 down", "[0, 20, 10]", atZero.path().toString());

        // Single request
        DiskResult single = new LookDiskScheduler().schedule(Arrays.asList(30), 10, 200, Direction.UP);
        eq("Disk single request", 20, single.totalMovement);

        // Support at least 20 requests
        List<Integer> many = new ArrayList<>();
        for (int i = 0; i < 25; i++) many.add((i * 37) % 200);
        for (DiskScheduler d : Arrays.<DiskScheduler>asList(new FcfsDiskScheduler(), new SstfDiskScheduler(), new ScanDiskScheduler(),
                new CScanDiskScheduler(), new LookDiskScheduler(), new CLookDiskScheduler())) {
            DiskResult res = d.schedule(many, 100, 200, Direction.UP);
            check(d.getName() + " serves all 25 requests", res.serviceOrder.size() == 25);
        }

        // Bad input
        try {
            new FcfsDiskScheduler().schedule(Arrays.asList(250), 10, 200, Direction.UP);
            check("Disk out-of-range request rejected", false);
        } catch (IllegalArgumentException e) {
            check("Disk out-of-range request rejected", true);
        }
    }

    // ----------------------------------------------------------- Validation
    private static void validationTests() {
        expectInvalid("empty", () -> Validation.parseInt("X", "  ", 0, 5));
        expectInvalid("letters", () -> Validation.parseInt("X", "abc", 0, 5));
        expectInvalid("decimal", () -> Validation.parseInt("X", "2.5", 0, 5));
        expectInvalid("negative", () -> Validation.parseInt("X", "-1", 0, 5));
        expectInvalid("huge", () -> Validation.parseInt("X", "99999999999999999999", 0, 5));
        expectInvalid("empty queue", () -> Validation.parseRequests("", 200));
        expectInvalid("queue out of range", () -> Validation.parseRequests("1 2 300", 200));
        expectInvalid("queue non numeric", () -> Validation.parseRequests("1, x, 3", 200));
        try {
            eq("queue parse", "[98, 183, 37]", Validation.parseRequests("98, 183;37", 200).toString());
        } catch (ValidationException e) {
            check("queue parse", false);
        }
    }

    private interface Thrower { void run() throws ValidationException; }

    private static void expectInvalid(String name, Thrower t) {
        try {
            t.run();
            check("Validation rejects " + name, false);
        } catch (ValidationException e) {
            check("Validation rejects " + name, true);
        }
    }

    // -------------------------------------------------------------- Helpers
    private static void eq(String name, Object expected, Object actual) {
        boolean ok = String.valueOf(expected).equals(String.valueOf(actual));
        if (!ok) System.out.println("   expected: " + expected + "\n   actual:   " + actual);
        check(name, ok);
    }

    private static void near(String name, double expected, double actual) {
        boolean ok = Math.abs(expected - actual) < 1e-9;
        if (!ok) System.out.println("   expected: " + expected + "\n   actual:   " + actual);
        check(name, ok);
    }

    private static void check(String name, boolean ok) {
        if (ok) passed++; else failed++;
        System.out.println((ok ? "PASS  " : "FAIL  ") + name);
    }
}
