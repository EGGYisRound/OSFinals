package scheduler;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import scheduler.cpu.*;
import scheduler.disk.*;

/**
 * Group details and the four assigned algorithms.
 *
 * EDIT THE DEFAULTS BELOW (or put a config.properties file next to the JAR) so the
 * main menu lists exactly your group's assigned algorithms.
 *
 * config.properties keys (all optional):
 *   group=Group 3 - Section A
 *   members=Juan Dela Cruz|Maria Santos|...        (separate members with |)
 *   cpu=SJF,RR                    (CPU keys: FCFS, SJF, PRIORITY_NP, SRTF, PRIORITY_P, RR)
 *   disk=SSTF,C-LOOK              (disk keys: FCFS, SSTF, SCAN, CSCAN, LOOK, CLOOK)
 */
public final class AppConfig {
    public static final String COURSE = "ICS26012 - Operating Systems";
    public static final String TITLE = "CPU and Disk Scheduling Simulator";

    // ---- defaults: CHANGE THESE to your group's information ----
    private static final String DEFAULT_GROUP = "Group <No> - Section <Section>";
    private static final String[] DEFAULT_MEMBERS = {
        "Member 1 - <algorithm> - Leader",
        "Member 2 - <algorithm> - Documentation Lead",
        "Member 3 - <algorithm> - Video Lead",
        "Member 4 - <algorithm> - Testing Lead"
    };
    private static final String DEFAULT_CPU = "FCFS,RR";      // one non-preemptive + one preemptive
    private static final String DEFAULT_DISK = "SSTF,SCAN";// two disk algorithms
    // ------------------------------------------------------------

    private static final Map<String, CpuScheduler> CPU_ALGORITHMS = new LinkedHashMap<>();
    private static final Map<String, DiskScheduler> DISK_ALGORITHMS = new LinkedHashMap<>();

    static {
        CPU_ALGORITHMS.put("FCFS", new FcfsScheduler());
        CPU_ALGORITHMS.put("SJF", new SjfScheduler());
        CPU_ALGORITHMS.put("PRIORITY_NP", new PriorityNonPreemptiveScheduler());
        CPU_ALGORITHMS.put("SRTF", new SrtfScheduler());
        CPU_ALGORITHMS.put("PRIORITY_P", new PriorityPreemptiveScheduler());
        CPU_ALGORITHMS.put("RR", new RoundRobinScheduler());

        DISK_ALGORITHMS.put("FCFS", new FcfsDiskScheduler());
        DISK_ALGORITHMS.put("SSTF", new SstfDiskScheduler());
        DISK_ALGORITHMS.put("SCAN", new ScanDiskScheduler());
        DISK_ALGORITHMS.put("CSCAN", new CScanDiskScheduler());
        DISK_ALGORITHMS.put("LOOK", new LookDiskScheduler());
        DISK_ALGORITHMS.put("CLOOK", new CLookDiskScheduler());
    }

    private final String group;
    private final List<String> members;
    private final List<CpuScheduler> cpu;
    private final List<DiskScheduler> disk;

    private AppConfig(String group, List<String> members, List<CpuScheduler> cpu, List<DiskScheduler> disk) {
        this.group = group;
        this.members = members;
        this.cpu = cpu;
        this.disk = disk;
    }

    public String group() { return group; }
    public List<String> members() { return members; }
    public List<CpuScheduler> cpuAlgorithms() { return cpu; }
    public List<DiskScheduler> diskAlgorithms() { return disk; }

    /** Loads config.properties from the working directory if present, else the defaults. */
    public static AppConfig load() {
        Properties p = new Properties();
        File f = new File("config.properties");
        if (f.isFile()) {
            try (InputStream in = new FileInputStream(f)) {
                p.load(in);
            } catch (IOException ignored) {
                // use defaults
            }
        }
        String group = p.getProperty("group", DEFAULT_GROUP).trim();
        List<String> members = new ArrayList<>();
        if (p.getProperty("members") != null) {
            for (String m : p.getProperty("members").split("\\|")) if (!m.trim().isEmpty()) members.add(m.trim());
        } else {
            members.addAll(java.util.Arrays.asList(DEFAULT_MEMBERS));
        }
        List<CpuScheduler> cpu = pick(CPU_ALGORITHMS, p.getProperty("cpu", DEFAULT_CPU));
        List<DiskScheduler> disk = pick(DISK_ALGORITHMS, p.getProperty("disk", DEFAULT_DISK));
        if (cpu.isEmpty()) cpu = pick(CPU_ALGORITHMS, DEFAULT_CPU);
        if (disk.isEmpty()) disk = pick(DISK_ALGORITHMS, DEFAULT_DISK);
        return new AppConfig(group, members, cpu, disk);
    }

    private static <T> List<T> pick(Map<String, T> all, String csv) {
        List<T> out = new ArrayList<>();
        for (String key : csv.split(",")) {
            String k = key.trim().toUpperCase().replace("-", "").replace(" ", "");
            if (k.equals("PRIORITYNP")) k = "PRIORITY_NP";
            if (k.equals("PRIORITYP")) k = "PRIORITY_P";
            T item = all.get(k);
            if (item != null && !out.contains(item)) out.add(item);
        }
        return out;
    }
}
