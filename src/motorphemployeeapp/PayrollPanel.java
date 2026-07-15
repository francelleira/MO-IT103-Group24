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
 * successful payroll_staff login.
 */
public class PayrollPanel {

    private PayrollPanel() {}

    private static final String PAYROLL_CSV = "Payroll_Report.csv";

    private static JTextField txtEmpNum;
    private static JTextField txtEmpName;
    private static JTextField txtPayCoverage;
    private static JTextArea  areaResult;
    private static JLabel     lblStatus;

    public static JPanel build() {
        JPanel outer = new JPanel(new BorderLayout(0, 0));
        outer.setBackground(AppConstants.CLR_BG);
        outer.setBorder(new EmptyBorder(28, 36, 28, 36));

        // Header row
        JPanel headerRow = new JPanel(new BorderLayout());
        headerRow.setBackground(AppConstants.CLR_BG);
        headerRow.add(
                UIComponents.sectionHeader("Payroll Processing",
                        "Compute employee salaries and deductions"),
                BorderLayout.WEST);

        JButton btnSignOut = UIComponents.ghostBtn("Sign Out");
        btnSignOut.addActionListener(e -> LoginPanel.signOut());

        JPanel backWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        backWrap.setBackground(AppConstants.CLR_BG);
        backWrap.add(btnSignOut);
        headerRow.add(backWrap, BorderLayout.EAST);
        outer.add(headerRow, BorderLayout.NORTH);

        // Form card
        JPanel formCard = UIComponents.card(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill   = GridBagConstraints.HORIZONTAL;
        gc.insets = new Insets(5, 4, 5, 4);

        // Row 1: Employee Number | Employee Name
        JPanel topFields = new JPanel(new GridLayout(2, 2, 8, 4));
        topFields.setBackground(AppConstants.CLR_SURFACE);
        topFields.add(UIComponents.label("Employee Number *"));
        topFields.add(UIComponents.label("Employee Name *"));

        txtEmpNum = UIComponents.inputField();
        txtEmpNum.setToolTipText("Enter numeric employee ID (e.g. 10001)");
        topFields.add(txtEmpNum);

        txtEmpName = UIComponents.inputField();
        txtEmpName.setToolTipText("Full name as registered");
        topFields.add(txtEmpName);

        gc.gridy = 0; gc.gridx = 0; gc.gridwidth = 2; gc.weightx = 1.0;
        formCard.add(topFields, gc);

        gc.gridwidth = 1; gc.weightx = 1.0;

        // Row 2: Pay Coverage label
        gc.gridy = 1; gc.gridx = 0; gc.gridwidth = 2;
        formCard.add(UIComponents.label("Pay Coverage (month 1–7, 0 = all) *"), gc);

        // Row 3: Pay Coverage input
        gc.gridy = 2;
        txtPayCoverage = UIComponents.inputField();
        txtPayCoverage.setToolTipText(
                "0 = all months (June–December)  |  1 = June … 7 = December");
        formCard.add(txtPayCoverage, gc);

        // Row 4: hint
        gc.gridy = 4; gc.gridx = 0; gc.gridwidth = 2; gc.weightx = 1;
        JLabel hint = new JLabel(
                "0 = all months (June–December)  |  1 = June … 7 = December");
        hint.setFont(AppConstants.FONT_SMALL);
        hint.setForeground(AppConstants.CLR_MUTED);
        formCard.add(hint, gc);

        // Row 5: buttons
        JButton btnProcess = UIComponents.primaryBtn("Process Payroll");
        JButton btnAll     = UIComponents.ghostBtn("All Employees");
        JButton btnSummary = UIComponents.ghostBtn("Generate Summary");
        JButton btnClear   = UIComponents.ghostBtn("Clear");

        JPanel btnRow = new JPanel(new GridLayout(1, 4, 8, 0));
        btnRow.setBackground(AppConstants.CLR_SURFACE);
        btnRow.add(btnProcess);
        btnRow.add(btnAll);
        btnRow.add(btnSummary);
        btnRow.add(btnClear);

        gc.gridy = 5; gc.gridx = 0; gc.gridwidth = 2; gc.weightx = 1.0;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.insets = new Insets(14, 0, 4, 0);
        formCard.add(btnRow, gc);

        gc.gridwidth = 1;

        // Row 6: inline status
        lblStatus = new JLabel(" ");
        lblStatus.setFont(AppConstants.FONT_SMALL);
        lblStatus.setForeground(AppConstants.CLR_MUTED);
        gc.gridy = 6; gc.insets = new Insets(2, 4, 2, 4);
        formCard.add(lblStatus, gc);

        // Wire events
        btnProcess.addActionListener(e -> handleProcessPayroll(false));
        btnAll.addActionListener(e -> handleProcessPayroll(true));
        btnSummary.addActionListener(e -> handleGenerateSummary());
        btnClear.addActionListener(e -> clearForm());

        // Result area
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
    // -------------------------------------------------------------------------
    private static void handleProcessPayroll(boolean allEmployees) {
        areaResult.setText("");
        setStatus("Processing…", AppConstants.CLR_MUTED);

        try {
            if (allEmployees) {
                // Process all employees, all months
                StringBuilder sb = new StringBuilder();
                ArrayList<String> allNums = EmployeeService.getAllEmployeeNumbers();
                for (String num : allNums) {
                    sb.append(PayrollService.buildPayrollText(num, -1));
                }
                areaResult.setText(sb.toString());

                // Write CSV
                PayrollService.writePayrollCsv(null, -1, PAYROLL_CSV);
                setStatus("✔  Processed " + allNums.size() + " employees. CSV saved to "
                        + PAYROLL_CSV, AppConstants.CLR_SUCCESS);
                JOptionPane.showMessageDialog(null,
                        "Payroll processed for all employees.\nCSV file saved: " + PAYROLL_CSV,
                        "Payroll Complete", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            // Validate Employee Number
            String rawNum = txtEmpNum.getText().trim();
            if (rawNum.isEmpty()) throw new IllegalArgumentException("Employee Number is required.");
            if (!rawNum.matches("\\d+")) throw new NumberFormatException(
                    "Employee Number must contain digits only (entered: \"" + rawNum + "\").");

            // Validate Employee Name
            String rawName = txtEmpName.getText().trim();
            if (rawName.isEmpty()) throw new IllegalArgumentException("Employee Name is required.");
            if (rawName.length() < 2) throw new IllegalArgumentException("Employee Name is too short.");
            if (!rawName.matches("[a-zA-ZÀ-ÖØ-öø-ÿ .,'\\-]+"))
                throw new IllegalArgumentException("Employee Name contains invalid characters.");

            // Validate Pay Coverage
            String rawCov = txtPayCoverage.getText().trim();
            if (rawCov.isEmpty()) throw new IllegalArgumentException(
                    "Pay Coverage is required (0 = all months, 1–7 for a specific month).");
            int coverage;
            try {
                coverage = Integer.parseInt(rawCov);
            } catch (NumberFormatException nfe) {
                throw new NumberFormatException("Pay Coverage must be a whole number (0–7).");
            }
            if (coverage < 0 || coverage > 7) throw new IllegalArgumentException(
                    "Pay Coverage must be 0 (all months) through 7 (December).");

            // Employee must exist
            if (!EmployeeService.employeeExists(rawNum)) throw new IllegalArgumentException(
                    "No employee found with number " + rawNum + ".");

            // Optional name-match warning
            String[] empRow = EmployeeService.findEmployee(rawNum);
            String recorded = (empRow[AppConstants.COL_FIRST_NAME].trim()
                    + " " + empRow[AppConstants.COL_LAST_NAME].trim()).toLowerCase();
            boolean nameMatch = recorded.contains(rawName.toLowerCase())
                    || rawName.toLowerCase().contains(recorded.split(" ")[0]);

            if (!nameMatch) {
                int choice = JOptionPane.showConfirmDialog(null,
                        "The name entered doesn't match the record for employee "
                                + rawNum + ".\nRecorded name: "
                                + empRow[AppConstants.COL_FIRST_NAME].trim() + " "
                                + empRow[AppConstants.COL_LAST_NAME].trim()
                                + "\n\nContinue anyway?",
                        "Name Mismatch", JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE);
                if (choice != JOptionPane.YES_OPTION) {
                    setStatus("Cancelled.", AppConstants.CLR_MUTED);
                    return;
                }
            }

            int monthIdx = (coverage == 0) ? -1 : coverage - 1;
            areaResult.setText(PayrollService.buildPayrollText(rawNum, monthIdx));

            // Write CSV
            PayrollService.writePayrollCsv(rawNum, monthIdx, PAYROLL_CSV);
            setStatus("✔  Payroll computed successfully. CSV saved to " + PAYROLL_CSV,
                    AppConstants.CLR_SUCCESS);
            JOptionPane.showMessageDialog(null,
                    "Payroll computed successfully.\nCSV file saved: " + PAYROLL_CSV,
                    "Payroll Complete", JOptionPane.INFORMATION_MESSAGE);

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

    // PAYROLL SUMMARY HANDLER
    // -------------------------------------------------------------------------
    /**
     * Computes and displays a summary of all employees' payroll data:
     * total employee count, total gross pay, total deductions, and
     * average net pay. Validation ensures the computation only runs
     * when employee data has actually been loaded.
     */
    private static void handleGenerateSummary() {
        setStatus("Generating summary…", AppConstants.CLR_MUTED);

        try {
            if (DataStore.getEmployeeData().isEmpty()) {
                throw new IllegalStateException(
                        "No employee data is currently loaded. Please make sure the "
                                + "employee CSV file has been loaded before generating a summary.");
            }

            PayrollService.PayrollSummary summary = PayrollService.generateSummary(-1);
            String summaryText = PayrollService.buildSummaryText(summary);

            areaResult.setText(summaryText);
            setStatus("✔  Summary generated for " + summary.employeeCount + " employees.",
                    AppConstants.CLR_SUCCESS);

            JOptionPane.showMessageDialog(null, summaryText,
                    "Payroll Summary", JOptionPane.INFORMATION_MESSAGE);

        } catch (IllegalStateException ex) {
            UIComponents.showError(null, ex.getMessage());
            setStatus("✘  " + ex.getMessage(), AppConstants.CLR_DANGER);
        } catch (Exception ex) {
            UIComponents.showError(null, "Unexpected error:\n" + ex.getMessage());
            setStatus("✘  Unexpected error.", AppConstants.CLR_DANGER);
        }
    }

    private static void clearForm() {
        txtEmpNum.setText("");
        txtEmpName.setText("");
        txtPayCoverage.setText("");
        areaResult.setText("");
        setStatus(" ", AppConstants.CLR_MUTED);
    }

    private static void setStatus(String text, Color colour) {
        lblStatus.setText(text);
        lblStatus.setForeground(colour);
    }
}
