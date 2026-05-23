package motorphemployeeapp;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

/**
 * PayrollPanel
 *
 * Builds and owns the Payroll Processing screen shown after a
 * successful payroll-staff login. Captures Employee Number, Employee
 * Name, and Pay Coverage from the user, validates each field, then
 * delegates computation to PayrollService and displays the result.
 *
 * Responsibilities:
 *   - Render the three required input fields (Employee Number,
 *     Employee Name, Pay Coverage) plus a cutoff combo box
 *   - Handle "Process Payroll" (single employee) and "All Employees"
 *   - Validate all inputs with exception handling
 *   - Cross-validate name against the CSV record
 *   - Display formatted payroll output and status messages
 *   - Provide Sign Out navigation back to LoginPanel
 */
public class PayrollPanel {

    // ─── Prevent instantiation ────────────────────────────────────────────────
    private PayrollPanel() {}

    // ─── Component references ─────────────────────────────────────────────────
    private static JTextField    txtEmpNum;
    private static JTextField    txtEmpName;
    private static JTextField    txtPayCoverage;
    private static JComboBox<String> comboCutoff;
    private static JTextArea     areaResult;
    private static JLabel        lblStatus;

    // PANEL BUILDER
    /**
     * Constructs and returns the fully assembled Payroll Processing JPanel.
     *
     * @return  Ready-to-display Payroll panel.
     */
    public static JPanel build() {
        JPanel outer = new JPanel(new BorderLayout(0, 0));
        outer.setBackground(AppConstants.CLR_BG);
        outer.setBorder(new EmptyBorder(28, 36, 28, 36));

        // ── Header row ────────────────────────────────────────────────────────
        JPanel headerRow = new JPanel(new BorderLayout());
        headerRow.setBackground(AppConstants.CLR_BG);
        headerRow.add(
            UIComponents.sectionHeader("Payroll Processing",
                "Compute employee salaries and deductions"),
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
        gc.fill   = GridBagConstraints.HORIZONTAL;
        gc.insets = new Insets(5, 4, 5, 4);

        // Row 0 — labels: Employee Number | Employee Name
        gc.gridy = 0; gc.weightx = 0.4; gc.gridx = 0;
        formCard.add(UIComponents.label("Employee Number *"), gc);
        gc.gridx = 1; gc.weightx = 0.6;
        formCard.add(UIComponents.label("Employee Name *"), gc);

        // Row 1 — inputs: Employee Number | Employee Name
        gc.gridy = 1; gc.gridx = 0; gc.weightx = 0.4;
        txtEmpNum = UIComponents.inputField();
        txtEmpNum.setToolTipText("Enter numeric employee ID (e.g. 10001)");
        formCard.add(txtEmpNum, gc);

        gc.gridx = 1; gc.weightx = 0.6;
        txtEmpName = UIComponents.inputField();
        txtEmpName.setToolTipText("Full name as registered (e.g. Juan Dela Cruz)");
        formCard.add(txtEmpName, gc);

        // Row 2 — labels: Pay Coverage | Specific Cutoff
        gc.gridy = 2; gc.gridx = 0; gc.weightx = 0.4;
        formCard.add(UIComponents.label("Pay Coverage (month 1–7, 0 = all) *"), gc);
        gc.gridx = 1; gc.weightx = 0.6;
        formCard.add(UIComponents.label("Specific Cutoff Period"), gc);

        // Row 3 — inputs: Pay Coverage | Cutoff combo
        gc.gridy = 3; gc.gridx = 0; gc.weightx = 0.4;
        txtPayCoverage = UIComponents.inputField();
        txtPayCoverage.setToolTipText(
            "0 = all months (June–December)  |  1 = June … 7 = December");
        formCard.add(txtPayCoverage, gc);

        gc.gridx = 1; gc.weightx = 0.6;
        comboCutoff = new JComboBox<>(buildCutoffOptions());
        comboCutoff.setFont(AppConstants.FONT_INPUT);
        comboCutoff.setBackground(AppConstants.CLR_SURFACE2);
        comboCutoff.setForeground(AppConstants.CLR_TEXT);
        comboCutoff.setPreferredSize(new Dimension(0, 36));
        formCard.add(comboCutoff, gc);

        // Row 4 — hint text
        gc.gridy = 4; gc.gridx = 0; gc.gridwidth = 2; gc.weightx = 1;
        JLabel hint = new JLabel(
            "Coverage 0 = all months (June–Dec).  Coverage 1–7 selects a specific month.");
        hint.setFont(AppConstants.FONT_SMALL);
        hint.setForeground(AppConstants.CLR_MUTED);
        formCard.add(hint, gc);

        // Row 5 — action buttons
        JButton btnProcess = UIComponents.primaryBtn("Process Payroll");
        JButton btnAll     = UIComponents.ghostBtn("All Employees");
        JButton btnClear   = UIComponents.ghostBtn("Clear");

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        btnRow.setBackground(AppConstants.CLR_SURFACE);
        btnRow.add(btnProcess);
        btnRow.add(btnAll);
        btnRow.add(btnClear);

        gc.gridy = 5; gc.insets = new Insets(14, 0, 4, 0);
        formCard.add(btnRow, gc);

        // Row 6 — inline status label
        lblStatus = new JLabel(" ");
        lblStatus.setFont(AppConstants.FONT_SMALL);
        lblStatus.setForeground(AppConstants.CLR_MUTED);
        gc.gridy = 6; gc.insets = new Insets(2, 4, 2, 4);
        formCard.add(lblStatus, gc);

        // ── Event handling ────────────────────────────────────────────────────
        btnProcess.addActionListener(e -> handleProcessPayroll(false));
        btnAll.addActionListener(e -> handleProcessPayroll(true));
        btnClear.addActionListener(e -> clearForm());

        // ── Result area ───────────────────────────────────────────────────────
        areaResult = UIComponents.resultArea();
        JScrollPane sp = UIComponents.scrollPane(areaResult);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, formCard, sp);
        split.setDividerLocation(220);
        split.setResizeWeight(0);
        split.setBorder(null);
        split.setBackground(AppConstants.CLR_BG);
        split.setDividerSize(6);

        outer.add(split, BorderLayout.CENTER);
        return outer;
    }

    // EVENT HANDLERS
    /**
     * Processes payroll for one employee or all employees.
     *
     * When allEmployees is true the individual input fields are ignored
     * and every employee in the data store is processed.
     *
     * Exception handling layers:
     *   NumberFormatException   – non-numeric employee number or coverage value
     *   IllegalArgumentException – empty fields, out-of-range coverage, unknown employee
     *   Exception               – unexpected runtime errors (parse failures, etc.)
     * Each layer shows a modal error dialog and updates the inline status label.
     *
     * @param allEmployees  true to process all employees; false for a single one.
     */
    private static void handleProcessPayroll(boolean allEmployees) {
        areaResult.setText("");
        setStatus("Processing…", AppConstants.CLR_MUTED);

        try {
            // ── All-employees shortcut (no field validation needed) ────────────
            if (allEmployees) {
                StringBuilder sb = new StringBuilder();
                ArrayList<String> allNums = EmployeeService.getAllEmployeeNumbers();
                for (String num : allNums) {
                    sb.append(PayrollService.buildPayrollText(num, -1));
                }
                areaResult.setText(sb.toString());
                setStatus("✔  Processed " + allNums.size() + " employees.",
                          AppConstants.CLR_SUCCESS);
                return;
            }

            // ── Validate: Employee Number ─────────────────────────────────────
            String rawNum = txtEmpNum.getText().trim();
            if (rawNum.isEmpty()) {
                throw new IllegalArgumentException("Employee Number is required.");
            }
            if (!rawNum.matches("\\d+")) {
                throw new NumberFormatException(
                    "Employee Number must contain digits only (entered: \""
                    + rawNum + "\").");
            }

            // ── Validate: Employee Name ───────────────────────────────────────
            String rawName = txtEmpName.getText().trim();
            if (rawName.isEmpty()) {
                throw new IllegalArgumentException("Employee Name is required.");
            }
            if (rawName.length() < 2) {
                throw new IllegalArgumentException("Employee Name is too short.");
            }
            if (!rawName.matches("[a-zA-ZÀ-ÖØ-öø-ÿ .,'\\-]+")) {
                throw new IllegalArgumentException(
                    "Employee Name contains invalid characters.");
            }

            // ── Validate: Pay Coverage ────────────────────────────────────────
            String rawCov = txtPayCoverage.getText().trim();
            if (rawCov.isEmpty()) {
                throw new IllegalArgumentException(
                    "Pay Coverage is required (0 = all months, 1–7 for a specific month).");
            }
            int coverage;
            try {
                coverage = Integer.parseInt(rawCov);
            } catch (NumberFormatException nfe) {
                throw new NumberFormatException("Pay Coverage must be a whole number (0–7).");
            }
            if (coverage < 0 || coverage > 7) {
                throw new IllegalArgumentException(
                    "Pay Coverage must be 0 (all months) through 7 (December).");
            }

            // ── Cross-validate: employee must exist ───────────────────────────
            if (!EmployeeService.employeeExists(rawNum)) {
                throw new IllegalArgumentException(
                    "No employee found with number " + rawNum + ".");
            }

            // ── Optional name-match warning ───────────────────────────────────
            String[] empRow = EmployeeService.findEmployee(rawNum);
            String recorded = (empRow[AppConstants.COL_FIRST_NAME].trim()
                             + " " + empRow[AppConstants.COL_LAST_NAME].trim())
                             .toLowerCase();
            boolean nameMatch = recorded.contains(rawName.toLowerCase())
                             || rawName.toLowerCase().contains(recorded.split(" ")[0]);

            if (!nameMatch) {
                int choice = JOptionPane.showConfirmDialog(null,
                    "The name entered doesn't match the record for employee "
                    + rawNum + ".\n"
                    + "Recorded name: " + empRow[AppConstants.COL_FIRST_NAME].trim()
                    + " " + empRow[AppConstants.COL_LAST_NAME].trim() + "\n\n"
                    + "Continue anyway?",
                    "Name Mismatch",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);

                if (choice != JOptionPane.YES_OPTION) {
                    setStatus("Cancelled.", AppConstants.CLR_MUTED);
                    return;
                }
            }

            // ── Compute and display ───────────────────────────────────────────
            // coverage 0 → monthIdx -1 (all); coverage 1–7 → monthIdx 0–6
            int monthIdx = (coverage == 0) ? -1 : coverage - 1;
            areaResult.setText(PayrollService.buildPayrollText(rawNum, monthIdx));
            setStatus("✔  Payroll computed successfully.", AppConstants.CLR_SUCCESS);

        } catch (NumberFormatException ex) {
            UIComponents.showError(null, "Number Format Error:\n" + ex.getMessage());
            setStatus("✘  Number format error.", AppConstants.CLR_DANGER);

        } catch (IllegalArgumentException ex) {
            UIComponents.showError(null, "Validation Error:\n" + ex.getMessage());
            setStatus("✘  " + ex.getMessage(), AppConstants.CLR_DANGER);

        } catch (Exception ex) {
            UIComponents.showError(null, "Unexpected error:\n" + ex.getMessage());
            setStatus("✘  Unexpected error.", AppConstants.CLR_DANGER);
        }
    }

    /** Resets all form fields and the result area. */
    private static void clearForm() {
        txtEmpNum.setText("");
        txtEmpName.setText("");
        txtPayCoverage.setText("");
        areaResult.setText("");
        setStatus(" ", AppConstants.CLR_MUTED);
    }

    /** Updates the inline status label with the given text and colour. */
    private static void setStatus(String text, Color colour) {
        lblStatus.setText(text);
        lblStatus.setForeground(colour);
    }

    // HELPER
    /**
     * Builds the options array for the cutoff combo box, with a
     * "All periods" placeholder as the first entry.
     *
     * @return  String array of human-readable cutoff labels.
     */
    private static String[] buildCutoffOptions() {
        String[] opts = new String[AppConstants.CUTOFF_START.length + 1];
        opts[0] = "— All periods —";
        for (int i = 0; i < AppConstants.CUTOFF_START.length; i++) {
            opts[i + 1] = AppConstants.CUTOFF_START[i]
                        + "  →  "
                        + AppConstants.CUTOFF_END[i];
        }
        return opts;
    }
}
