package motorphemployeeapp;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

/**
 * EmployeePanel
 *
 * Builds and owns the Employee Lookup screen that is shown after a
 * successful employee login. The screen allows a user to enter their
 * employee number and view their basic personal details.
 *
 * Responsibilities:
 *   - Render the employee lookup form
 *   - Handle the Look Up and Clear button actions
 *   - Validate the employee number input
 *   - Display employee details in the result area
 *   - Provide a Sign Out button that delegates to LoginPanel
 */
public class EmployeePanel {

    // ─── Prevent instantiation ────────────────────────────────────────────────
    private EmployeePanel() {}

    // ─── Component references ─────────────────────────────────────────────────
    private static JTextField txtEmpNum;
    private static JTextArea  areaResult;

    // PANEL BUILDER
    /**
     * Constructs and returns the fully assembled Employee Lookup JPanel.
     *
     * @return  Ready-to-display Employee panel.
     */
    public static JPanel build() {
        JPanel outer = new JPanel(new BorderLayout(0, 0));
        outer.setBackground(AppConstants.CLR_BG);
        outer.setBorder(new EmptyBorder(28, 36, 28, 36));

        // ── Header row ────────────────────────────────────────────────────────
        JPanel headerRow = new JPanel(new BorderLayout());
        headerRow.setBackground(AppConstants.CLR_BG);
        headerRow.add(
            UIComponents.sectionHeader("Employee Details",
                "View your personal employee details"),
            BorderLayout.WEST);

        JButton btnSignOut = UIComponents.ghostBtn("← Sign Out");
        btnSignOut.addActionListener(e -> LoginPanel.signOut());

        JPanel backWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        backWrap.setBackground(AppConstants.CLR_BG);
        backWrap.add(btnSignOut);
        headerRow.add(backWrap, BorderLayout.EAST);

        outer.add(headerRow, BorderLayout.NORTH);

        // ── Form card ─────────────────────────────────────────────────────────
        JPanel formCard = UIComponents.card(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill    = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1;
        gc.insets  = new Insets(6, 0, 6, 0);

        txtEmpNum = UIComponents.inputField();
        txtEmpNum.setToolTipText("Enter your numeric Employee ID (e.g. 10001)");

        JButton btnSearch = UIComponents.primaryBtn("View Details");
        JButton btnClear  = UIComponents.ghostBtn("Clear");

        // ── Event handling ────────────────────────────────────────────────────
        btnSearch.addActionListener(e -> handleLookup());
        txtEmpNum.addActionListener(e -> handleLookup()); // Enter key shortcut
        btnClear.addActionListener(e -> {
            txtEmpNum.setText("");
            areaResult.setText("");
        });

        gc.gridx = 0; gc.gridy = 0; gc.gridwidth = 2;
        formCard.add(UIComponents.label("Employee Number"), gc);

        gc.gridy = 1;
        formCard.add(txtEmpNum, gc);

        gc.gridy = 2; gc.gridwidth = 1; gc.weightx = 0.5;
        gc.insets = new Insets(12, 0, 0, 6);
        formCard.add(btnSearch, gc);

        gc.gridx = 1;
        gc.insets = new Insets(12, 6, 0, 0);
        formCard.add(btnClear, gc);

        // ── Result area ───────────────────────────────────────────────────────
        areaResult = UIComponents.resultArea();
        JScrollPane sp = UIComponents.scrollPane(areaResult);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, formCard, sp);
        split.setDividerLocation(160);
        split.setResizeWeight(0);
        split.setBorder(null);
        split.setBackground(AppConstants.CLR_BG);
        split.setDividerSize(6);

        outer.add(split, BorderLayout.CENTER);
        return outer;
    }

    // EVENT HANDLER
    /**
     * Validates the entered employee number and displays the matching
     * employee's details if found.
     *
     * Exception handling:
     *   - Empty field           → IllegalArgumentException
     *   - Non-numeric input     → NumberFormatException
     *   - Employee not found    → IllegalArgumentException
     * All exceptions show a modal error dialog via UIComponents.showError
     * so the program never crashes on invalid input.
     */
    private static void handleLookup() {
        try {
            String raw = txtEmpNum.getText().trim();

            // Validate: not empty
            if (raw.isEmpty()) {
                throw new IllegalArgumentException("Employee Number cannot be empty.");
            }

            // Validate: digits only
            if (!raw.matches("\\d+")) {
                throw new NumberFormatException(
                    "Employee Number must contain digits only (entered: \"" + raw + "\").");
            }

            // Validate: employee exists
            String[] emp = EmployeeService.findEmployee(raw);
            if (emp == null) {
                throw new IllegalArgumentException(
                    "No employee found with number: " + raw);
            }

            // Build and display result
            String name = emp[AppConstants.COL_FIRST_NAME].trim()
                        + " " + emp[AppConstants.COL_LAST_NAME].trim();

            StringBuilder sb = new StringBuilder();
            sb.append("EMPLOYEE DETAILS\n");
            sb.append(String.format("  %-18s %s%n", "Employee No.:", emp[AppConstants.COL_EMP_NUM].trim()));
            sb.append(String.format("  %-18s %s%n", "Full Name:",    name));
            sb.append(String.format("  %-18s %s%n", "Birthday:",     emp[AppConstants.COL_BIRTHDAY].trim()));

            areaResult.setText(sb.toString());

        } catch (NumberFormatException ex) {
            UIComponents.showError(null, "Invalid Employee Number:\n" + ex.getMessage());
        } catch (IllegalArgumentException ex) {
            UIComponents.showError(null, ex.getMessage());
        }
    }
}
