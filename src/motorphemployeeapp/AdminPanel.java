package motorphemployeeapp;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;

/**
 * AdminPanel
 *
 * Builds the Admin/HR screen. It shows a table of all employees and
 * lets the admin add a new employee, edit an existing one, or delete
 * one. The screen switches between four views (table, add form, edit
 * lookup, edit form) using a CardLayout.
 */
public class AdminPanel {

    private AdminPanel() {}

    // Component references
    // -------------------------------------------------------------------------
    private static JTable            empTable;
    private static DefaultTableModel tableModel;

    // The main outer panel uses CardLayout to switch between table view and form views
    private static CardLayout mainCardLayout;
    private static JPanel     mainCardPanel;

    // Card names
    private static final String CARD_TABLE       = "TABLE";
    private static final String CARD_ADD_FORM    = "ADD_FORM";
    private static final String CARD_EDIT_LOOKUP = "EDIT_LOOKUP";
    private static final String CARD_EDIT_FORM   = "EDIT_FORM";

    // Header buttons
    private static JButton btnAddEmployee;
    private static JButton btnEditRecord;

    // Remembers the last row selected in the table, just so the Edit Record
    // number field can be pre-filled as a convenience.
    private static String selectedEmpNum;

    // Edit-lookup form field
    private static JTextField editLookupTxtEmpNum;

    // Add-form fields
    private static JTextField addTxtEmpNum;
    private static JTextField addTxtLastName;
    private static JTextField addTxtFirstName;
    private static JTextField addTxtBirthday;
    private static JTextField addTxtAddress;
    private static JTextField addTxtPhone;
    private static JTextField addTxtSSS;
    private static JTextField addTxtPhilHealth;
    private static JTextField addTxtTIN;
    private static JTextField addTxtPagIBIG;
    private static JTextField addTxtStatus;
    private static JTextField addTxtPosition;
    private static JTextField addTxtSupervisor;
    private static JTextField addTxtBasicSalary;
    private static JTextField addTxtRiceSubsidy;
    private static JTextField addTxtPhoneAllowance;
    private static JTextField addTxtClothingAllowance;
    private static JTextField addTxtGrossSemiMonthly;
    private static JTextField addTxtHourlyRate;

    // Edit-form fields
    private static JTextField editTxtEmpNum;
    private static JTextField editTxtLastName;
    private static JTextField editTxtFirstName;
    private static JTextField editTxtBirthday;
    private static JTextField editTxtAddress;
    private static JTextField editTxtPhone;
    private static JTextField editTxtSSS;
    private static JTextField editTxtPhilHealth;
    private static JTextField editTxtTIN;
    private static JTextField editTxtPagIBIG;
    private static JTextField editTxtStatus;
    private static JTextField editTxtPosition;
    private static JTextField editTxtSupervisor;
    private static JTextField editTxtBasicSalary;
    private static JTextField editTxtRiceSubsidy;
    private static JTextField editTxtPhoneAllowance;
    private static JTextField editTxtClothingAllowance;
    private static JTextField editTxtGrossSemiMonthly;
    private static JTextField editTxtHourlyRate;

    // PANEL BUILDER
    // -------------------------------------------------------------------------
    // Builds the whole Admin screen: the header row plus the card area
    // that switches between the table, add form, and edit forms.
    public static JPanel build() {
        // Top-level panel holds the header + a card area beneath
        JPanel outer = new JPanel(new BorderLayout(0, 0));
        outer.setBackground(AppConstants.CLR_BG);
        outer.setBorder(new EmptyBorder(20, 28, 20, 28));

        // Shared header row
        outer.add(buildHeaderRow(), BorderLayout.NORTH);

        // Main card area switches between table and form panels
        mainCardLayout = new CardLayout();
        mainCardPanel  = new JPanel(mainCardLayout);
        mainCardPanel.setBackground(AppConstants.CLR_BG);

        mainCardPanel.add(buildTableCard(),      CARD_TABLE);
        mainCardPanel.add(buildAddFormCard(),    CARD_ADD_FORM);
        mainCardPanel.add(buildEditLookupCard(), CARD_EDIT_LOOKUP);
        mainCardPanel.add(buildEditFormCard(),   CARD_EDIT_FORM);

        outer.add(mainCardPanel, BorderLayout.CENTER);

        // Load initial data
        refreshTable();

        // Start file-watcher for dynamic refresh
        AdminService.startFileWatcher(AdminPanel::refreshTable);

        return outer;
    }

    // HEADER ROW
    // -------------------------------------------------------------------------
    // Builds the top row with the title on the left and the Add Employee /
    // Edit Record / Sign Out buttons on the right.
    private static JPanel buildHeaderRow() {
        JPanel headerRow = new JPanel(new BorderLayout());
        headerRow.setBackground(AppConstants.CLR_BG);
        headerRow.setBorder(new EmptyBorder(0, 0, 12, 0));

        headerRow.add(
                UIComponents.sectionHeader("Employee Records",
                        "View, add, edit, and delete employee records"),
                BorderLayout.WEST);

        // Right side: Add Employee | Edit Record | Sign Out
        btnAddEmployee = UIComponents.primaryBtn("Add Employee");
        btnEditRecord  = UIComponents.ghostBtn("Edit Record");

        JButton btnSignOut = UIComponents.ghostBtn("Sign Out");
        btnSignOut.addActionListener(e -> {
            AdminService.stopFileWatcher();
            LoginPanel.signOut();
        });

        btnAddEmployee.addActionListener(e -> showAddForm());
        btnEditRecord.addActionListener(e -> showEditLookupForm());

        JPanel btnWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnWrap.setBackground(AppConstants.CLR_BG);
        btnWrap.add(btnAddEmployee);
        btnWrap.add(btnEditRecord);
        btnWrap.add(btnSignOut);

        headerRow.add(btnWrap, BorderLayout.EAST);
        return headerRow;
    }

    // TABLE CARD
    // -------------------------------------------------------------------------
    // Builds the scrollable table that lists every employee record.
    private static JPanel buildTableCard() {
        JPanel tableCard = new JPanel(new BorderLayout());
        tableCard.setBackground(AppConstants.CLR_BG);

        tableModel = new DefaultTableModel(AdminService.TABLE_COLUMNS, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        empTable = new JTable(tableModel);
        empTable.setFont(AppConstants.FONT_INPUT);
        empTable.setForeground(AppConstants.CLR_TEXT);
        empTable.setBackground(AppConstants.CLR_BG);
        empTable.setGridColor(AppConstants.CLR_BORDER);
        empTable.setRowHeight(24);
        empTable.setSelectionBackground(AppConstants.CLR_SURFACE);
        empTable.setSelectionForeground(AppConstants.CLR_TEXT);
        empTable.getTableHeader().setFont(AppConstants.FONT_LABEL);
        empTable.getTableHeader().setBackground(AppConstants.CLR_SURFACE);
        empTable.getTableHeader().setForeground(AppConstants.CLR_TEXT);
        empTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        empTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        JScrollPane tableSP = UIComponents.scrollPane(empTable);
        tableCard.add(tableSP, BorderLayout.CENTER);

        // Remember the selected employee number purely to pre-fill the
        // Edit Record number-entry field as a convenience.
        empTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = empTable.getSelectedRow();
                selectedEmpNum = (row >= 0) ? (String) tableModel.getValueAt(row, 0) : null;
            }
        });

        return tableCard;
    }

    // ADD EMPLOYEE FORM CARD
    // -------------------------------------------------------------------------
    // Builds the form used to add a brand-new employee record.
    private static JPanel buildAddFormCard() {
        JPanel formCard = new JPanel(new BorderLayout(0, 0));
        formCard.setBackground(AppConstants.CLR_BG);

        // Form title
        JLabel title = new JLabel("Add New Employee");
        title.setFont(AppConstants.FONT_CARD_H);
        title.setForeground(AppConstants.CLR_TEXT);
        title.setBorder(new EmptyBorder(0, 0, 10, 0));
        formCard.add(title, BorderLayout.NORTH);

        // Scrollable field grid
        JPanel fields = new JPanel(new GridLayout(0, 4, 8, 6));
        fields.setBackground(AppConstants.CLR_SURFACE);
        fields.setBorder(new EmptyBorder(14, 14, 14, 14));

        addTxtEmpNum           = addLabeledField(fields, "Employee # (auto)");
        addTxtLastName         = addLabeledField(fields, "Last Name *");
        addTxtFirstName        = addLabeledField(fields, "First Name *");
        addTxtBirthday         = addLabeledField(fields, "Birthday (MM/DD/YYYY) *");
        addTxtSSS              = addLabeledField(fields, "SSS # *");
        addTxtPhilHealth       = addLabeledField(fields, "PhilHealth # *");
        addTxtTIN              = addLabeledField(fields, "TIN # *");
        addTxtPagIBIG          = addLabeledField(fields, "Pag-IBIG # *");
        addTxtStatus           = addLabeledField(fields, "Status *");
        addTxtPosition         = addLabeledField(fields, "Position *");
        addTxtSupervisor       = addLabeledField(fields, "Supervisor *");
        addTxtPhone            = addLabeledField(fields, "Phone Number *");
        addTxtAddress          = addLabeledField(fields, "Address *");
        addTxtBasicSalary      = addLabeledField(fields, "Basic Salary *");
        addTxtRiceSubsidy      = addLabeledField(fields, "Rice Subsidy");
        addTxtPhoneAllowance   = addLabeledField(fields, "Phone Allowance");
        addTxtClothingAllowance = addLabeledField(fields, "Clothing Allowance");
        addTxtGrossSemiMonthly = addLabeledField(fields, "Gross Semi-monthly *");
        addTxtHourlyRate       = addLabeledField(fields, "Hourly Rate *");
        // Filler cells to keep grid even (19 fields x 2 cols = 38, next multiple of 4 = 40, need 2 more)
        fields.add(new JLabel(""));
        fields.add(new JLabel(""));

        // Auto-fill employee number (read-only -- user cannot change it)
        addTxtEmpNum.setEditable(false);
        addTxtEmpNum.setBackground(AppConstants.CLR_SURFACE);

        JScrollPane fieldsSP = new JScrollPane(fields);
        fieldsSP.setBorder(null);
        fieldsSP.setBackground(AppConstants.CLR_BG);

        formCard.add(fieldsSP, BorderLayout.CENTER);

        // Bottom button row
        JButton btnAdd  = UIComponents.primaryBtn("Add Employee");
        JButton btnBack = UIComponents.ghostBtn("Back");

        btnAdd.addActionListener(e -> handleAddEmployee());
        btnBack.addActionListener(e -> showTableView());

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        btnRow.setBackground(AppConstants.CLR_BG);
        btnRow.add(btnAdd);
        btnRow.add(btnBack);
        formCard.add(btnRow, BorderLayout.SOUTH);

        return formCard;
    }

    // EDIT-RECORD NUMBER-ENTRY LOOKUP CARD
    // -------------------------------------------------------------------------
    // Small panel shown when Edit Record is clicked.
    private static JPanel buildEditLookupCard() {
        JPanel outer = new JPanel(new GridBagLayout());
        outer.setBackground(AppConstants.CLR_BG);

        JPanel card = UIComponents.card(new GridBagLayout());
        card.setPreferredSize(new Dimension(420, 230));

        GridBagConstraints gc = new GridBagConstraints();
        gc.insets  = new Insets(6, 0, 6, 0);
        gc.fill    = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1;

        JLabel heading = new JLabel("Edit Employee Record");
        heading.setFont(AppConstants.FONT_CARD_H);
        heading.setForeground(AppConstants.CLR_TEXT);

        JLabel instructions = new JLabel("Enter the Employee Number to edit:");
        instructions.setFont(AppConstants.FONT_SMALL);
        instructions.setForeground(AppConstants.CLR_MUTED);

        editLookupTxtEmpNum = UIComponents.inputField();
        editLookupTxtEmpNum.setToolTipText("Enter numeric employee ID (e.g. 10001)");

        JButton btnContinue = UIComponents.primaryBtn("Continue");
        JButton btnBack     = UIComponents.ghostBtn("Back");

        btnContinue.addActionListener(e -> handleEditLookupContinue());
        editLookupTxtEmpNum.addActionListener(e -> handleEditLookupContinue()); // Enter key shortcut
        btnBack.addActionListener(e -> showTableView());

        JPanel btnRow = new JPanel(new GridLayout(1, 2, 8, 0));
        btnRow.setBackground(AppConstants.CLR_SURFACE);
        btnRow.add(btnContinue);
        btnRow.add(btnBack);

        gc.gridx = 0; gc.gridy = 0; card.add(heading, gc);
        gc.gridy = 1; card.add(instructions, gc);
        gc.gridy = 2; card.add(editLookupTxtEmpNum, gc);
        gc.gridy = 3; gc.insets = new Insets(14, 0, 0, 0);
        card.add(btnRow, gc);

        outer.add(card);
        return outer;
    }

    // EDIT EMPLOYEE FORM CARD
    // -------------------------------------------------------------------------
    // Builds the form used to edit, save, or delete an existing employee.
    private static JPanel buildEditFormCard() {
        JPanel formCard = new JPanel(new BorderLayout(0, 0));
        formCard.setBackground(AppConstants.CLR_BG);

        JLabel title = new JLabel("Edit Employee Record");
        title.setFont(AppConstants.FONT_CARD_H);
        title.setForeground(AppConstants.CLR_TEXT);
        title.setBorder(new EmptyBorder(0, 0, 10, 0));
        formCard.add(title, BorderLayout.NORTH);

        JPanel fields = new JPanel(new GridLayout(0, 4, 8, 6));
        fields.setBackground(AppConstants.CLR_SURFACE);
        fields.setBorder(new EmptyBorder(14, 14, 14, 14));

        editTxtEmpNum           = addLabeledField(fields, "Employee #");
        editTxtLastName         = addLabeledField(fields, "Last Name *");
        editTxtFirstName        = addLabeledField(fields, "First Name *");
        editTxtBirthday         = addLabeledField(fields, "Birthday (MM/DD/YYYY) *");
        editTxtSSS              = addLabeledField(fields, "SSS # *");
        editTxtPhilHealth       = addLabeledField(fields, "PhilHealth # *");
        editTxtTIN              = addLabeledField(fields, "TIN # *");
        editTxtPagIBIG          = addLabeledField(fields, "Pag-IBIG # *");
        editTxtStatus           = addLabeledField(fields, "Status *");
        editTxtPosition         = addLabeledField(fields, "Position *");
        editTxtSupervisor       = addLabeledField(fields, "Supervisor *");
        editTxtPhone            = addLabeledField(fields, "Phone Number *");
        editTxtAddress          = addLabeledField(fields, "Address *");
        editTxtBasicSalary      = addLabeledField(fields, "Basic Salary *");
        editTxtRiceSubsidy      = addLabeledField(fields, "Rice Subsidy");
        editTxtPhoneAllowance   = addLabeledField(fields, "Phone Allowance");
        editTxtClothingAllowance = addLabeledField(fields, "Clothing Allowance");
        editTxtGrossSemiMonthly = addLabeledField(fields, "Gross Semi-monthly *");
        editTxtHourlyRate       = addLabeledField(fields, "Hourly Rate *");
        fields.add(new JLabel(""));
        fields.add(new JLabel(""));

        // Employee # cannot be changed during edit
        editTxtEmpNum.setEditable(false);
        editTxtEmpNum.setBackground(AppConstants.CLR_SURFACE);

        JScrollPane fieldsSP = new JScrollPane(fields);
        fieldsSP.setBorder(null);
        fieldsSP.setBackground(AppConstants.CLR_BG);
        formCard.add(fieldsSP, BorderLayout.CENTER);

        // Bottom button row: Save Changes | Delete Record | Clear Form | Back
        JButton btnSave   = UIComponents.primaryBtn("Save Changes");
        JButton btnDelete = UIComponents.ghostBtn("Delete Record");
        JButton btnClear  = UIComponents.ghostBtn("Clear Form");
        JButton btnBack   = UIComponents.ghostBtn("Back");

        btnSave.addActionListener(e -> handleSaveChanges());
        btnDelete.addActionListener(e -> handleDeleteEmployee());
        btnClear.addActionListener(e -> clearEditForm());
        btnBack.addActionListener(e -> showTableView());

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        btnRow.setBackground(AppConstants.CLR_BG);
        btnRow.add(btnSave);
        btnRow.add(btnDelete);
        btnRow.add(btnClear);
        btnRow.add(btnBack);
        formCard.add(btnRow, BorderLayout.SOUTH);

        return formCard;
    }

    // NAVIGATION HELPERS
    // -------------------------------------------------------------------------

    // Switch back to the table view and bring back the header buttons.
    private static void showTableView() {
        btnAddEmployee.setVisible(true);
        btnEditRecord.setVisible(true);
        mainCardLayout.show(mainCardPanel, CARD_TABLE);
    }

     // Switch to the Add Employee form, pre-filling the auto-generated
     // employee number.
    private static void showAddForm() {
        clearAddForm();
        addTxtEmpNum.setText(AdminService.getNextEmployeeNumber());
        btnAddEmployee.setVisible(false);
        btnEditRecord.setVisible(false);
        mainCardLayout.show(mainCardPanel, CARD_ADD_FORM);
    }

    //Switch to the Edit Record number-entry lookup panel.
    private static void showEditLookupForm() {
        editLookupTxtEmpNum.setText(selectedEmpNum != null ? selectedEmpNum : "");
        btnAddEmployee.setVisible(false);
        btnEditRecord.setVisible(false);
        mainCardLayout.show(mainCardPanel, CARD_EDIT_LOOKUP);
    }

    // Checks the employee number typed into the lookup panel.
    private static void handleEditLookupContinue() {
        try {
            String[] row = AdminService.findEmployeeForEdit(editLookupTxtEmpNum.getText());
            populateEditForm(row);
            mainCardLayout.show(mainCardPanel, CARD_EDIT_FORM);
        } catch (IllegalArgumentException ex) {
            UIComponents.showError(null, ex.getMessage());
        }
    }

    // ADD EMPLOYEE HANDLER
    // -------------------------------------------------------------------------
    // Runs when "Add Employee" is clicked on the add form. Reads the form,
    // saves the new employee, then resets the form for the next entry.
    private static void handleAddEmployee() {
        String[] row = buildRowFromAddForm();

        try {
            AdminService.addEmployee(row);
            refreshTable();
            clearAddForm();
            // Generate next number for the next potential add
            addTxtEmpNum.setText(AdminService.getNextEmployeeNumber());
            JOptionPane.showMessageDialog(null,
                    "Employee " + row[AppConstants.COL_EMP_NUM] + " added successfully.",
                    "Success", JOptionPane.INFORMATION_MESSAGE);

        } catch (IllegalArgumentException ex) {
            UIComponents.showError(null, "Validation Error:\n" + ex.getMessage());
        } catch (IOException ex) {
            UIComponents.showError(null, "File Error:\n" + ex.getMessage());
        }
    }

    // SAVE CHANGES HANDLER
    // -------------------------------------------------------------------------
    // Runs when "Save Changes" is clicked. Reads the edit form, saves the
    // updated employee, refreshes the table, and returns to the table view.
    private static void handleSaveChanges() {
        String[] row = buildRowFromEditForm();

        try {
            AdminService.updateEmployee(row);
            refreshTable();
            JOptionPane.showMessageDialog(null,
                    "Employee " + row[AppConstants.COL_EMP_NUM] + " updated successfully.",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
            showTableView();

        } catch (IllegalArgumentException ex) {
            UIComponents.showError(null, "Validation Error:\n" + ex.getMessage());
        } catch (IOException ex) {
            UIComponents.showError(null, "File Error:\n" + ex.getMessage());
        }
    }

    // DELETE EMPLOYEE HANDLER
    // -------------------------------------------------------------------------
    // Runs when "Delete Record" is clicked. Asks the admin to confirm,
    // then deletes the employee and returns to the table view.
    private static void handleDeleteEmployee() {
        String empNum = editTxtEmpNum.getText().trim();
        if (empNum.isEmpty()) {
            UIComponents.showError(null, "No employee loaded into the edit form.");
            return;
        }

        String name = editTxtFirstName.getText().trim() + " " + editTxtLastName.getText().trim();

        int choice = JOptionPane.showConfirmDialog(null,
                "Are you sure you want to delete employee:\n"
                        + "  No: " + empNum + "  Name: " + name + "\n\n"
                        + "This action cannot be undone.",
                "Confirm Deletion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (choice != JOptionPane.YES_OPTION) return;

        try {
            AdminService.deleteEmployee(empNum);
            refreshTable();
            showTableView();
            JOptionPane.showMessageDialog(null,
                    "Employee " + empNum + " deleted successfully.",
                    "Deleted", JOptionPane.INFORMATION_MESSAGE);

        } catch (IllegalArgumentException ex) {
            UIComponents.showError(null, "Delete Error:\n" + ex.getMessage());
        } catch (IOException ex) {
            UIComponents.showError(null, "File Error:\n" + ex.getMessage());
        }
    }

    // TABLE REFRESH
    // -------------------------------------------------------------------------
    // Clears and repopulates the JTable from AdminService's prepared data.
    public static void refreshTable() {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(AdminPanel::refreshTable);
            return;
        }

        // Remember the currently selected employee number so we can restore it
        String currentSelection = null;
        int selectedRow = empTable != null ? empTable.getSelectedRow() : -1;
        if (selectedRow >= 0 && tableModel != null) {
            Object val = tableModel.getValueAt(selectedRow, 0);
            if (val != null) currentSelection = val.toString();
        }

        tableModel.setRowCount(0);

        ArrayList<String[]> displayRows = AdminService.getDisplayRows();
        for (String[] displayRow : displayRows) {
            tableModel.addRow(displayRow);
        }

        // Restore selection if the employee still exists
        if (currentSelection != null) {
            for (int r = 0; r < tableModel.getRowCount(); r++) {
                if (currentSelection.equals(tableModel.getValueAt(r, 0))) {
                    empTable.setRowSelectionInterval(r, r);
                    break;
                }
            }
        }
    }

    // FORM POPULATION HELPERS
    // -------------------------------------------------------------------------
    // Fills every field in the edit form with the given employee's data.
    private static void populateEditForm(String[] row) {
        editTxtEmpNum.setText(           safeGet(row, AppConstants.COL_EMP_NUM));
        editTxtLastName.setText(         safeGet(row, AppConstants.COL_LAST_NAME));
        editTxtFirstName.setText(        safeGet(row, AppConstants.COL_FIRST_NAME));
        editTxtBirthday.setText(         safeGet(row, AppConstants.COL_BIRTHDAY));
        editTxtAddress.setText(          safeGet(row, AppConstants.COL_ADDRESS));
        editTxtPhone.setText(            safeGet(row, AppConstants.COL_PHONE));
        editTxtSSS.setText(              safeGet(row, AppConstants.COL_SSS));
        editTxtPhilHealth.setText(       safeGet(row, AppConstants.COL_PHILHEALTH));
        editTxtTIN.setText(              safeGet(row, AppConstants.COL_TIN));
        editTxtPagIBIG.setText(          safeGet(row, AppConstants.COL_PAGIBIG));
        editTxtStatus.setText(           safeGet(row, AppConstants.COL_STATUS));
        editTxtPosition.setText(         safeGet(row, AppConstants.COL_POSITION));
        editTxtSupervisor.setText(       safeGet(row, AppConstants.COL_SUPERVISOR));
        editTxtBasicSalary.setText(      safeGet(row, AppConstants.COL_BASIC_SALARY));
        editTxtRiceSubsidy.setText(      safeGet(row, AppConstants.COL_RICE_SUBSIDY));
        editTxtPhoneAllowance.setText(   safeGet(row, AppConstants.COL_PHONE_ALLOWANCE));
        editTxtClothingAllowance.setText(safeGet(row, AppConstants.COL_CLOTHING_ALLOWANCE));
        editTxtGrossSemiMonthly.setText( safeGet(row, AppConstants.COL_GROSS_SEMI_MONTHLY));
        editTxtHourlyRate.setText(       safeGet(row, AppConstants.COL_HOURLY_RATE));
    }

    // Empties every field on the add form.
    private static void clearAddForm() {
        addTxtEmpNum.setText("");
        addTxtLastName.setText("");
        addTxtFirstName.setText("");
        addTxtBirthday.setText("");
        addTxtAddress.setText("");
        addTxtPhone.setText("");
        addTxtSSS.setText("");
        addTxtPhilHealth.setText("");
        addTxtTIN.setText("");
        addTxtPagIBIG.setText("");
        addTxtStatus.setText("");
        addTxtPosition.setText("");
        addTxtSupervisor.setText("");
        addTxtBasicSalary.setText("");
        addTxtRiceSubsidy.setText("");
        addTxtPhoneAllowance.setText("");
        addTxtClothingAllowance.setText("");
        addTxtGrossSemiMonthly.setText("");
        addTxtHourlyRate.setText("");
    }

    // Empties every editable field on the edit form, but keeps the
    // employee number since that field cannot be changed.
    private static void clearEditForm() {
        editTxtLastName.setText("");
        editTxtFirstName.setText("");
        editTxtBirthday.setText("");
        editTxtAddress.setText("");
        editTxtPhone.setText("");
        editTxtSSS.setText("");
        editTxtPhilHealth.setText("");
        editTxtTIN.setText("");
        editTxtPagIBIG.setText("");
        editTxtStatus.setText("");
        editTxtPosition.setText("");
        editTxtSupervisor.setText("");
        editTxtBasicSalary.setText("");
        editTxtRiceSubsidy.setText("");
        editTxtPhoneAllowance.setText("");
        editTxtClothingAllowance.setText("");
        editTxtGrossSemiMonthly.setText("");
        editTxtHourlyRate.setText("");
    }

    // ROW BUILDERS
    // -------------------------------------------------------------------------
    // Reads every field on the add form into a single employee row array.
    private static String[] buildRowFromAddForm() {
        String[] row = new String[AppConstants.TOTAL_COLUMNS];
        row[AppConstants.COL_EMP_NUM]            = addTxtEmpNum.getText().trim();
        row[AppConstants.COL_LAST_NAME]          = addTxtLastName.getText().trim();
        row[AppConstants.COL_FIRST_NAME]         = addTxtFirstName.getText().trim();
        row[AppConstants.COL_BIRTHDAY]           = addTxtBirthday.getText().trim();
        row[AppConstants.COL_ADDRESS]            = addTxtAddress.getText().trim();
        row[AppConstants.COL_PHONE]              = addTxtPhone.getText().trim();
        row[AppConstants.COL_SSS]                = addTxtSSS.getText().trim();
        row[AppConstants.COL_PHILHEALTH]         = addTxtPhilHealth.getText().trim();
        row[AppConstants.COL_TIN]                = addTxtTIN.getText().trim();
        row[AppConstants.COL_PAGIBIG]            = addTxtPagIBIG.getText().trim();
        row[AppConstants.COL_STATUS]             = addTxtStatus.getText().trim();
        row[AppConstants.COL_POSITION]           = addTxtPosition.getText().trim();
        row[AppConstants.COL_SUPERVISOR]         = addTxtSupervisor.getText().trim();
        row[AppConstants.COL_BASIC_SALARY]       = addTxtBasicSalary.getText().trim();
        row[AppConstants.COL_RICE_SUBSIDY]       = addTxtRiceSubsidy.getText().trim();
        row[AppConstants.COL_PHONE_ALLOWANCE]    = addTxtPhoneAllowance.getText().trim();
        row[AppConstants.COL_CLOTHING_ALLOWANCE] = addTxtClothingAllowance.getText().trim();
        row[AppConstants.COL_GROSS_SEMI_MONTHLY] = addTxtGrossSemiMonthly.getText().trim();
        row[AppConstants.COL_HOURLY_RATE]        = addTxtHourlyRate.getText().trim();
        return row;
    }

    // Reads every field on the edit form into a single employee row array.
    private static String[] buildRowFromEditForm() {
        String[] row = new String[AppConstants.TOTAL_COLUMNS];
        row[AppConstants.COL_EMP_NUM]            = editTxtEmpNum.getText().trim();
        row[AppConstants.COL_LAST_NAME]          = editTxtLastName.getText().trim();
        row[AppConstants.COL_FIRST_NAME]         = editTxtFirstName.getText().trim();
        row[AppConstants.COL_BIRTHDAY]           = editTxtBirthday.getText().trim();
        row[AppConstants.COL_ADDRESS]            = editTxtAddress.getText().trim();
        row[AppConstants.COL_PHONE]              = editTxtPhone.getText().trim();
        row[AppConstants.COL_SSS]                = editTxtSSS.getText().trim();
        row[AppConstants.COL_PHILHEALTH]         = editTxtPhilHealth.getText().trim();
        row[AppConstants.COL_TIN]                = editTxtTIN.getText().trim();
        row[AppConstants.COL_PAGIBIG]            = editTxtPagIBIG.getText().trim();
        row[AppConstants.COL_STATUS]             = editTxtStatus.getText().trim();
        row[AppConstants.COL_POSITION]           = editTxtPosition.getText().trim();
        row[AppConstants.COL_SUPERVISOR]         = editTxtSupervisor.getText().trim();
        row[AppConstants.COL_BASIC_SALARY]       = editTxtBasicSalary.getText().trim();
        row[AppConstants.COL_RICE_SUBSIDY]       = editTxtRiceSubsidy.getText().trim();
        row[AppConstants.COL_PHONE_ALLOWANCE]    = editTxtPhoneAllowance.getText().trim();
        row[AppConstants.COL_CLOTHING_ALLOWANCE] = editTxtClothingAllowance.getText().trim();
        row[AppConstants.COL_GROSS_SEMI_MONTHLY] = editTxtGrossSemiMonthly.getText().trim();
        row[AppConstants.COL_HOURLY_RATE]        = editTxtHourlyRate.getText().trim();
        return row;
    }

    // UTILITY HELPERS
    // -------------------------------------------------------------------------
    // Adds a label + input field pair to a form panel and returns the field.
    private static JTextField addLabeledField(JPanel parent, String labelText) {
        parent.add(UIComponents.label(labelText));
        JTextField tf = UIComponents.inputField();
        parent.add(tf);
        return tf;
    }

    // Reads one CSV cell safely, stripping quotes/whitespace, returning ""
    // instead of crashing if the column doesn't exist.
    private static String safeGet(String[] row, int col) {
        if (col >= row.length) return "";
        return row[col].replace("\"", "").trim();
    }
}
