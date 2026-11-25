package scheduler.ui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;

import scheduler.AppConfig;
import scheduler.cpu.CpuScheduler;
import scheduler.disk.DiskScheduler;

/** Main window: main menu (assigned algorithms grouped by type), About, and one screen per algorithm. */
public final class MainWindow extends JFrame {
    private static final String MENU = "menu";
    private static final String ABOUT = "about";

    private final CardLayout cards = new CardLayout();
    private final JPanel root = new JPanel(cards);

    public MainWindow() {
        super(AppConfig.TITLE + " - " + AppConfig.COURSE);
        AppConfig config = AppConfig.load();

        root.add(buildMenu(config), MENU);
        root.add(buildAbout(config), ABOUT);
        for (CpuScheduler a : config.cpuAlgorithms()) root.add(new CpuPanel(a, this::showMenu), "cpu:" + a.getName());
        for (DiskScheduler a : config.diskAlgorithms()) root.add(new DiskPanel(a, this::showMenu), "disk:" + a.getName());

        setContentPane(root);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(900, 620));
        setSize(1150, 760);
        setLocationRelativeTo(null);
        showMenu();
    }

    private void showMenu() { cards.show(root, MENU); }

    private JPanel buildMenu(AppConfig config) {
        JPanel page = new JPanel(new BorderLayout());
        page.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        JLabel title = new JLabel(AppConfig.TITLE, SwingConstants.CENTER);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 28f));
        JLabel sub = new JLabel(AppConfig.COURSE + "  |  " + config.group(), SwingConstants.CENTER);
        sub.setFont(sub.getFont().deriveFont(14f));
        JPanel head = new JPanel(new GridLayout(2, 1, 0, 6));
        head.add(title);
        head.add(sub);
        head.setBorder(BorderFactory.createEmptyBorder(0, 0, 24, 0));

        JPanel cpu = group("CPU Scheduling");
        for (CpuScheduler a : config.cpuAlgorithms())
            cpu.add(menuButton(a.getName(), "cpu:" + a.getName()));
        JPanel disk = group("Disk Scheduling");
        for (DiskScheduler a : config.diskAlgorithms())
            disk.add(menuButton(a.getName(), "disk:" + a.getName()));

        JPanel groups = new JPanel(new GridLayout(1, 2, 30, 0));
        groups.add(cpu);
        groups.add(disk);

        JButton about = new JButton("About");
        about.addActionListener(e -> cards.show(root, ABOUT));
        JButton exit = new JButton("Exit");
        exit.addActionListener(e -> System.exit(0));
        JPanel bottom = new JPanel(new GridLayout(1, 2, 20, 0));
        bottom.setBorder(BorderFactory.createEmptyBorder(24, 200, 0, 200));
        bottom.add(about);
        bottom.add(exit);

        page.add(head, BorderLayout.NORTH);
        page.add(groups, BorderLayout.CENTER);
        page.add(bottom, BorderLayout.SOUTH);
        return page;
    }

    private JPanel group(String title) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(title),
                BorderFactory.createEmptyBorder(14, 20, 14, 20)));
        return p;
    }

    private JPanel menuButton(String text, String card) {
        JButton b = new JButton(text);
        b.setFont(b.getFont().deriveFont(15f));
        b.setPreferredSize(new Dimension(300, 52));
        b.addActionListener(e -> cards.show(root, card));
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.add(b);
        wrap.setBorder(BorderFactory.createEmptyBorder(6, 0, 6, 0));
        wrap.setMaximumSize(new Dimension(Integer.MAX_VALUE, 66));
        return wrap;
    }

    private JPanel buildAbout(AppConfig config) {
        JPanel page = new JPanel();
        page.setLayout(new BoxLayout(page, BoxLayout.Y_AXIS));
        page.setBorder(BorderFactory.createEmptyBorder(40, 60, 40, 60));

        JLabel title = new JLabel("About");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 26f));
        page.add(title);
        page.add(Box.createVerticalStrut(14));
        page.add(line(AppConfig.TITLE, true));
        page.add(line(AppConfig.COURSE + " - Alternative Assessment (1st Sem, AY 2026-2027)", false));
        page.add(Box.createVerticalStrut(14));
        page.add(line(config.group(), true));
        page.add(line("Members:", false));
        for (String m : config.members()) page.add(line("   • " + m, false));
        page.add(Box.createVerticalStrut(14));
        page.add(line("Conventions: time starts at 0; context switch = 0; ties -> earlier arrival, then lower PID;", false));
        page.add(line("lower priority number = higher priority; disk cylinders 0..N-1 (default N = 200).", false));
        page.add(Box.createVerticalStrut(24));
        JButton back = new JButton("Back to Menu");
        back.addActionListener(e -> showMenu());
        page.add(back);
        return page;
    }

    private JLabel line(String text, boolean bold) {
        JLabel l = new JLabel(text);
        l.setFont(l.getFont().deriveFont(bold ? Font.BOLD : Font.PLAIN, 15f));
        l.setAlignmentX(0f);
        l.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));
        return l;
    }
}
