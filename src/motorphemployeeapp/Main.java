package motorphemployeeapp;

import javax.swing.*;

/**
 * Main
 *
 * Application entry point. Its sole responsibility is to:
 *   1. Load the CSV data files into memory via DataStore.
 *   2. Schedule the GUI construction on the Event Dispatch Thread (EDT).
 *
 * All business logic lives in the service classes.
 * All GUI construction lives in the panel and frame classes.
 */
public class Main {

    // ─── Prevent instantiation ────────────────────────────────────────────────
    private Main() {}

    // PROGRAM ENTRY POINT
    public static void main(String[] args) throws Exception {

        // Load CSV data once at startup, before the GUI opens
        DataStore.loadEmployeeData();
        DataStore.loadAttendanceData();

        // Build and show the GUI on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            } catch (Exception ignored) {
                // Fall back to the default look and feel silently
            }
            MainFrame.build();
        });
    }
}
