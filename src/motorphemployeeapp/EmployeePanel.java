package motorphemployeeapp;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

/**
 * EmployeePanel
 *
 * Builds the Employee Lookup screen shown after an "employee" login.
 * Lets the logged-in employee type in their employee number to view
 * their own details or their own payslip.
 */
public class EmployeePanel {

    private EmployeePanel() {}

    // Component references
    private static JTextField txtEmpNum;
    private static JTextArea  areaResult;

    // PANEL BUILDER
    // -------------------------------------------------------------------------
    // Constructs and returns the fully assembled Employee Lookup JPanel.
    public static JPanel build() {
        JPanel outer = new JPanel(new BorderLayout(0, 0));
        outer.setBackground(AppConstants.CLR_BG);
        outer.setBorder(new EmptyBorder(28, 36, 28, 36));

        // Header row
        JPanel headerRow = new JPanel(new BorderLayout());
        headerRow.setBackground(AppConstants.CLR_BG);
        headerRow.add(
                UIComponents.sectionHeader("Employee Details",
                        "View your personal employee details"),
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
        gc.fill    = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1;
        gc.insets  = new Insets(6, 0, 6, 0);

        txtEmpNum = UIComponents.inputField();
        txtEmpNum.setToolTipText("Enter your numeric Employee ID (e.g. 10001)");

        JButton btnSearch  = UIComponents.primaryBtn("View Details");
        JButton btnPayslip = UIComponents.ghostBtn("View Payslip");
        JButton btnClear   = UIComponents.ghostBtn("Clear");

        // Event handling
        btnSearch.addActionListener(e -> handleLookup());
        txtEmpNum.addActionListener(e -> handleLookup()); // Enter key shortcut
        btnPayslip.addActionListener(e -> handleViewPayslip());
        btnClear.addActionListener(e -> {
            txtEmpNum.setText("");
            areaResult.setText("");
        });

        gc.gridx = 0; gc.gridy = 0; gc.gridwidth = 2;
        formCard.add(UIComponents.label("Employee Number"), gc);

        gc.gridy = 1;
        formCard.add(txtEmpNum, gc);

        // Row of three buttons: View Details | View Payslip | Clear
        JPanel btnRow = new JPanel(new GridLayout(1, 3, 8, 0));
        btnRow.setBackground(AppConstants.CLR_SURFACE);
        btnRow.add(btnSearch);
        btnRow.add(btnPayslip);
        btnRow.add(btnClear);

        gc.gridy = 2; gc.gridwidth = 2; gc.weightx = 1.0;
        gc.insets = new Insets(12, 0, 0, 0);
        formCard.add(btnRow, gc);

        // Result area
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
    // -------------------------------------------------------------------------
    // Checks the entered employee number and, if it's valid and found,
    // shows that employee's details.
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

            // Build and display the complete set of details from the
            // Employee Details CSV file (not just number, name, birthday).
            StringBuilder sb = new StringBuilder();
            sb.append("\nEMPLOYEE DETAILS\n\n");
            sb.append(String.format("  %-26s %s%n", "Employee No.:",            safe(emp, AppConstants.COL_EMP_NUM)));
            sb.append(String.format("  %-26s %s%n", "Last Name:",               safe(emp, AppConstants.COL_LAST_NAME)));
            sb.append(String.format("  %-26s %s%n", "First Name:",              safe(emp, AppConstants.COL_FIRST_NAME)));
            sb.append(String.format("  %-26s %s%n", "Birthday:",                safe(emp, AppConstants.COL_BIRTHDAY)));
            sb.append(String.format("  %-26s %s%n", "Address:",                 safe(emp, AppConstants.COL_ADDRESS)));
            sb.append(String.format("  %-26s %s%n", "Phone Number:",            safe(emp, AppConstants.COL_PHONE)));
            sb.append(String.format("  %-26s %s%n", "SSS #:",                   safe(emp, AppConstants.COL_SSS)));
            sb.append(String.format("  %-26s %s%n", "PhilHealth #:",            safe(emp, AppConstants.COL_PHILHEALTH)));
            sb.append(String.format("  %-26s %s%n", "TIN #:",                   safe(emp, AppConstants.COL_TIN)));
            sb.append(String.format("  %-26s %s%n", "Pag-IBIG #:",              safe(emp, AppConstants.COL_PAGIBIG)));
            sb.append(String.format("  %-26s %s%n", "Status:",                  safe(emp, AppConstants.COL_STATUS)));
            sb.append(String.format("  %-26s %s%n", "Position:",                safe(emp, AppConstants.COL_POSITION)));
            sb.append(String.format("  %-26s %s%n", "Immediate Supervisor:",    safe(emp, AppConstants.COL_SUPERVISOR)));
            sb.append(String.format("  %-26s PHP %s%n", "Basic Salary:",           safe(emp, AppConstants.COL_BASIC_SALARY)));
            sb.append(String.format("  %-26s PHP %s%n", "Rice Subsidy:",           safe(emp, AppConstants.COL_RICE_SUBSIDY)));
            sb.append(String.format("  %-26s PHP %s%n", "Phone Allowance:",        safe(emp, AppConstants.COL_PHONE_ALLOWANCE)));
            sb.append(String.format("  %-26s PHP %s%n", "Clothing Allowance:",     safe(emp, AppConstants.COL_CLOTHING_ALLOWANCE)));
            sb.append(String.format("  %-26s PHP %s%n", "Gross Semi-monthly Rate:", safe(emp, AppConstants.COL_GROSS_SEMI_MONTHLY)));
            sb.append(String.format("  %-26s PHP %s%n", "Hourly Rate:",            safe(emp, AppConstants.COL_HOURLY_RATE)));

            areaResult.setText(sb.toString());

        } catch (NumberFormatException ex) {
            UIComponents.showError(null, "Invalid Employee Number:\n" + ex.getMessage());
        } catch (IllegalArgumentException ex) {
            UIComponents.showError(null, ex.getMessage());
        }
    }

    // VIEW PAYSLIP HANDLER
    // -------------------------------------------------------------------------
    // Lets the logged-in employee view their own payslip.
    private static void handleViewPayslip() {
        try {
            String raw = txtEmpNum.getText().trim();

            if (raw.isEmpty()) {
                throw new IllegalArgumentException(
                        "Employee Number cannot be empty. Please enter your Employee Number first.");
            }
            if (!raw.matches("\\d+")) {
                throw new NumberFormatException(
                        "Employee Number must contain digits only (entered: \"" + raw + "\").");
            }

            String[] emp = EmployeeService.findEmployee(raw);
            if (emp == null) {
                throw new IllegalArgumentException("No employee found with number: " + raw);
            }

            String input = JOptionPane.showInputDialog(null,
                    "Enter pay coverage month:\n(1 = June … 7 = December, 0 = all months)",
                    "View Payslip", JOptionPane.QUESTION_MESSAGE);

            if (input == null) return; // user cancelled

            input = input.trim();
            if (input.isEmpty()) {
                throw new IllegalArgumentException(
                        "Pay coverage is required (0 = all months, 1–7 for a specific month).");
            }

            int coverage;
            try {
                coverage = Integer.parseInt(input);
            } catch (NumberFormatException nfe) {
                throw new NumberFormatException("Pay coverage must be a whole number (0–7).");
            }
            if (coverage < 0 || coverage > 7) {
                throw new IllegalArgumentException(
                        "Pay coverage must be 0 (all months) through 7 (December).");
            }

            int monthIdx = (coverage == 0) ? -1 : coverage - 1;
            String payslip = PayrollService.buildPayrollText(raw, monthIdx);
            areaResult.setText(payslip);

        } catch (NumberFormatException ex) {
            UIComponents.showError(null, "Invalid Input:\n" + ex.getMessage());
        } catch (IllegalArgumentException ex) {
            UIComponents.showError(null, ex.getMessage());
        } catch (Exception ex) {
            UIComponents.showError(null, "Unexpected error:\n" + ex.getMessage());
        }
    }

    // HELPER
    // -------------------------------------------------------------------------
    // Strips surrounding quotes and whitespace from a CSV field for display.
    private static String safe(String[] row, int col) {
        if (col >= row.length || row[col] == null) return "";
        return row[col].replace("\"", "").trim();
    }
}
