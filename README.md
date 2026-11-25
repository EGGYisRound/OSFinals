# CPU and Disk Scheduling Simulator (Java)

ICS26012 – Operating Systems · 1st Semester AY 2026–2027 · Alternative Assessment

A stand-alone Java Swing application that simulates CPU and disk scheduling algorithms. Every result
(Gantt chart, seek sequence, metrics) is **computed by the algorithm** – nothing is hard-coded.
Standard library only; no database, no internet.

- **Repository:** `<paste your GitHub repository link here>`
- **Group:** `<Group No> – <Section>`
- **Java version:** JDK/JRE 11 or newer (developed and tested on Java 21)

## Algorithms

| Category | Implemented (pick your group's four in the menu) |
|---|---|
| CPU – non-preemptive | FCFS, SJF, Priority |
| CPU – preemptive | SRTF, Priority, Round Robin |
| Disk | FCFS, SSTF, SCAN, C-SCAN, LOOK, C-LOOK |

The main menu lists only your group's **two CPU + two disk** assigned algorithms (see
[Configuring the assigned algorithms](#configuring-the-assigned-algorithms)).

## How to run

```bash
java -jar dist/ICS26012-Scheduling-Simulator.jar
```

Double-clicking the JAR also works on most systems with a JRE installed.

## How to build

Linux / macOS: `./build.sh` · Windows: `build.bat`  → produces `dist/ICS26012-Scheduling-Simulator.jar`.

Run the automated tests (hand-solved expected values): `./build.sh test`

## Configuring the assigned algorithms

Edit the defaults at the top of `src/scheduler/AppConfig.java`, **or** copy `config.properties.example`
to `config.properties` next to the JAR and edit it (group, members, `cpu=`, `disk=` keys).
The About screen shows the group and members.

## Features

- Main menu (CPU Scheduling / Disk Scheduling groups), **About**, **Exit**
- CPU: ≥ 10 processes (up to 50), PID / arrival / burst / priority, time quantum for RR;
  Gantt chart on a time scale **with idle time**; per-process completion, turnaround, waiting, response;
  averages; CPU utilization; throughput
- Disk: ≥ 20 requests (up to 500), initial head, disk size (default 200), initial direction;
  seek sequence, per-step movement, total head movement, average seek distance, head-path graph
- Input validation with clear messages (empty, non-numeric, negative, decimal, out of range, duplicate PID)
- **Load Sample Data** and **Reset / Clear** on every module

## Conventions followed (Section 4.3)

- Time starts at 0 in whole units; idle CPU is shown in the Gantt chart; context switch = 0
- CPU ties: earlier arrival, then lower PID. Lower priority number = higher priority, no aging
- SRTF preempts only if remaining time is strictly less; preemptive Priority only if strictly higher
- Round Robin: a new arrival and a preempted process entering at the same instant → new arrival first
- Turnaround = Completion − Arrival · Waiting = Turnaround − Burst · Response = First CPU − Arrival ·
  Utilization = busy ÷ total × 100 · Throughput = processes ÷ total time
- Cylinders 0 … N−1; head movement = sum of absolute differences from the initial head position
- SSTF ties → lower cylinder; duplicate requests served consecutively (zero movement)
- SCAN / C-SCAN travel to the physical end; LOOK / C-LOOK turn at the last pending request.
  The return jump of C-SCAN / C-LOOK is counted in head movement and shown in the path.
  A request on the head's own cylinder is served first (distance 0).
- Gantt blocks of the same process that run back-to-back are merged into one block.

## Project structure

```
src/scheduler/
  App.java, AppConfig.java      entry point, group info + assigned algorithms
  cpu/                          CpuScheduler interface + one class per algorithm (no UI code)
  disk/                         DiskScheduler interface + one class per algorithm (no UI code)
  util/                         input validation
  ui/                           Swing screens (menu, CPU panel, disk panel, Gantt, path graph)
test/scheduler/AlgorithmTests.java   automated tests
```

Algorithm logic is fully separate from the interface, so each module can be tested on its own.
Each algorithm class has a header comment `Owner: <member name>` – **replace it with the owner's name**.

## Team workflow (GitHub)

Each member commits their own algorithm class from their own account so the commit history shows
individual contributions. Suggested: one branch per member, merged into `main` by the Leader.
