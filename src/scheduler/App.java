package scheduler;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import scheduler.ui.MainWindow;

/** Program entry point. */
public final class App {
    private App() { }

    public static void main(String[] args) {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ignored) {
            // fall back to the default look and feel
        }
        SwingUtilities.invokeLater(() -> new MainWindow().setVisible(true));
    }
}
