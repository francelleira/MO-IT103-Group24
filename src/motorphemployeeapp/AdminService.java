package motorphemployeeapp;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * AdminService
 *
 * Owns every non-GUI concern of the Admin/HR screen:
 *   - Preparing employee data for display in the JTable.
 *   - Full field-level validation for the Add/Edit employee forms.
 *   - CRUD orchestration (delegates persistence to EmployeeService/DataStore).
 *   - Employee-number lookup used by the "Edit Record" flow.
 *   - The CSV file-watcher that triggers dynamic table refresh.
 *
 * AdminPanel (the view) never talks to DataStore or EmployeeService
 * directly for a write operation — it always goes through this class,
 * which keeps validation and data-handling rules in a single place.
 */
public class AdminService {

    private AdminService() {}

    // TABLE COLUMN DEFINITIONS
    // -------------------------------------------------------------------------
    public static final String[] TABLE_COLUMNS = {
            "Emp #", "Last Name", "First Name", "Birthday",
            "SSS #", "PhilHealth #", "TIN #", "Pag-IBIG #",
            "Status", "Position", "Hourly Rate"
    };

    public static final int[] DATA_COL_MAP = {
            AppConstants.COL_EMP_NUM,
            AppConstants.COL_LAST_NAME,
            AppConstants.COL_FIRST_NAME,
            AppConstants.COL_BIRTHDAY,
            AppConstants.COL_SSS,
            AppConstants.COL_PHILHEALTH,
            AppConstants.COL_TIN,
            AppConstants.COL_PAGIBIG,
            AppConstants.COL_STATUS,
            AppConstants.COL_POSITION,
            AppConstants.COL_HOURLY_RATE
    };

    private static final DateTimeFormatter BIRTHDAY_FORMAT = DateTimeFormatter.ofPattern("MM/dd/yyyy");

    // Accepted values for the Status field
    private static final String[] VALID_STATUSES = { "REGULAR", "PROBATIONARY" };

    // TABLE DATA
    // -------------------------------------------------------------------------
    /** Builds the display rows (one String[] per employee) used by the JTable model. */
    public static ArrayList<String[]> getDisplayRows() {
        ArrayList<String[]> data = DataStore.getEmployeeData();
        ArrayList<String[]> displayRows = new ArrayList<>();

        for (String[] row : data) {
            String[] displayRow = new String[TABLE_COLUMNS.length];
            for (int col = 0; col < TABLE_COLUMNS.length; col++) {
                int csvCol = DATA_COL_MAP[col];
                displayRow[col] = safeGet(row, csvCol);
            }
            displayRows.add(displayRow);
        }
        return displayRows;
    }

    /** True once at least one employee row has been loaded from the CSV file. */
    public static boolean isEmployeeDataLoaded() {
        return !DataStore.getEmployeeData().isEmpty();
    }

    public static String getNextEmployeeNumber() {
        return EmployeeService.getNextEmployeeNumber();
    }

    // LOOKUP FOR THE "EDIT RECORD" NUMBER-ENTRY FLOW
    // -------------------------------------------------------------------------
    /**
     * Validates a typed-in Employee Number and returns the matching row.
     * Used by the Edit Record number-entry panel, since the admin is no
     * longer required to select a row in the table first.
     *
     * @throws IllegalArgumentException if the number is missing, malformed,
     *                                   or does not match any employee.
     */
    public static String[] findEmployeeForEdit(String rawEmpNum) throws IllegalArgumentException {
        if (rawEmpNum == null || rawEmpNum.trim().isEmpty()) {
            throw new IllegalArgumentException("Employee Number cannot be empty.");
        }

        String empNum = rawEmpNum.trim();
        if (!empNum.matches("\\d+")) {
            throw new IllegalArgumentException(
                    "Employee Number must contain digits only (entered: \"" + empNum + "\").");
        }

        if (!isEmployeeDataLoaded()) {
            throw new IllegalArgumentException(
                    "No employee data is currently loaded. Please make sure the employee CSV file is loaded.");
        }

        String[] row = EmployeeService.findEmployee(empNum);
        if (row == null) {
            throw new IllegalArgumentException("No employee found with number " + empNum + ".");
        }
        return row;
    }

    // CRUD OPERATIONS (validate everything, then delegate persistence)
    // -------------------------------------------------------------------------
    public static void addEmployee(String[] row) throws IllegalArgumentException, IOException {
        validateEmployeeRow(row, true);
        EmployeeService.addEmployee(row);
    }

    public static void updateEmployee(String[] row) throws IllegalArgumentException, IOException {
        validateEmployeeRow(row, false);
        EmployeeService.updateEmployee(row);
    }

    public static void deleteEmployee(String empNum) throws IllegalArgumentException, IOException {
        EmployeeService.deleteEmployee(empNum);
    }

    // FULL FIELD-LEVEL VALIDATION
    // -------------------------------------------------------------------------
    /**
     * Validates every field of an employee row and collects every problem
     * found instead of stopping at the first one, so the admin can see and
     * fix everything in a single pass.
     *
     * @param row    19-element employee field array.
     * @param isNew  true when adding a new employee (Employee # must be
     *               unique); false when editing (Employee # must already exist).
     * @throws IllegalArgumentException listing every validation failure found.
     */
    public static void validateEmployeeRow(String[] row, boolean isNew) throws IllegalArgumentException {
        if (row == null || row.length != AppConstants.TOTAL_COLUMNS) {
            throw new IllegalArgumentException("Employee record is incomplete or malformed.");
        }

        List<String> errors = new ArrayList<>();

        // Employee #
        String empNum = safeTrim(row[AppConstants.COL_EMP_NUM]);
        if (empNum.isEmpty()) {
            errors.add("Employee Number cannot be empty.");
        } else if (!empNum.matches("\\d+")) {
            errors.add("Employee Number must contain digits only (entered: \"" + empNum + "\").");
        } else if (isNew && EmployeeService.employeeExists(empNum)) {
            errors.add("Employee Number " + empNum + " already exists. Use a different number.");
        } else if (!isNew && !EmployeeService.employeeExists(empNum)) {
            errors.add("Employee Number " + empNum + " was not found. Cannot update.");
        }

        // Names
        checkName(row[AppConstants.COL_LAST_NAME],  "Last Name",  errors);
        checkName(row[AppConstants.COL_FIRST_NAME], "First Name", errors);

        // Birthday
        checkBirthday(row[AppConstants.COL_BIRTHDAY], errors);

        // Address
        if (safeTrim(row[AppConstants.COL_ADDRESS]).isEmpty()) {
            errors.add("Address cannot be empty.");
        }

        // Phone number
        checkPhone(row[AppConstants.COL_PHONE], errors);

        // Government-issued numbers
        checkPattern(row[AppConstants.COL_SSS], "SSS #", "^\\d{2}-\\d{7}-\\d$",
                "Format must be NN-NNNNNNN-N (e.g. 34-1234567-8).", errors);
        checkPattern(row[AppConstants.COL_PHILHEALTH], "PhilHealth #", "^\\d{12}$",
                "Must be exactly 12 digits.", errors);
        checkPattern(row[AppConstants.COL_TIN], "TIN #", "^\\d{3}-\\d{3}-\\d{3}-\\d{3}$",
                "Format must be NNN-NNN-NNN-NNN.", errors);
        checkPattern(row[AppConstants.COL_PAGIBIG], "Pag-IBIG #", "^\\d{12}$",
                "Must be exactly 12 digits.", errors);

        // Status
        String status = safeTrim(row[AppConstants.COL_STATUS]);
        if (status.isEmpty()) {
            errors.add("Status cannot be empty.");
        } else {
            boolean validStatus = false;
            for (String s : VALID_STATUSES) {
                if (s.equalsIgnoreCase(status)) { validStatus = true; break; }
            }
            if (!validStatus) {
                errors.add("Status must be either \"Regular\" or \"Probationary\" (entered: \"" + status + "\").");
            }
        }

        // Position / Supervisor
        if (safeTrim(row[AppConstants.COL_POSITION]).isEmpty()) {
            errors.add("Position cannot be empty.");
        }
        if (safeTrim(row[AppConstants.COL_SUPERVISOR]).isEmpty()) {
            errors.add("Immediate Supervisor cannot be empty (enter \"N/A\" if not applicable).");
        }

        // Monetary fields
        checkMoney(row[AppConstants.COL_BASIC_SALARY],       "Basic Salary",            true,  errors);
        checkMoney(row[AppConstants.COL_RICE_SUBSIDY],       "Rice Subsidy",            false, errors);
        checkMoney(row[AppConstants.COL_PHONE_ALLOWANCE],    "Phone Allowance",         false, errors);
        checkMoney(row[AppConstants.COL_CLOTHING_ALLOWANCE], "Clothing Allowance",      false, errors);
        checkMoney(row[AppConstants.COL_GROSS_SEMI_MONTHLY], "Gross Semi-monthly Rate", true,  errors);
        checkMoney(row[AppConstants.COL_HOURLY_RATE],        "Hourly Rate",             true,  errors);

        if (!errors.isEmpty()) {
            StringBuilder sb = new StringBuilder("Please correct the following before continuing:\n");
            for (String e : errors) {
                sb.append("  • ").append(e).append('\n');
            }
            throw new IllegalArgumentException(sb.toString());
        }
    }

    // FIELD-LEVEL VALIDATION HELPERS
    // -------------------------------------------------------------------------
    private static void checkName(String value, String fieldName, List<String> errors) {
        String v = safeTrim(value);
        if (v.isEmpty()) {
            errors.add(fieldName + " cannot be empty.");
        } else if (!v.matches("[a-zA-ZÀ-ÖØ-öø-ÿ .,'\\-]+")) {
            errors.add(fieldName + " contains invalid characters (entered: \"" + v + "\").");
        }
    }

    private static void checkBirthday(String value, List<String> errors) {
        String v = safeTrim(value);
        if (v.isEmpty()) {
            errors.add("Birthday cannot be empty.");
            return;
        }
        try {
            LocalDate date = LocalDate.parse(v, BIRTHDAY_FORMAT);
            if (date.isAfter(LocalDate.now())) {
                errors.add("Birthday cannot be a future date.");
            } else if (date.isBefore(LocalDate.now().minusYears(100))) {
                errors.add("Birthday seems invalid (more than 100 years ago).");
            } else if (date.isAfter(LocalDate.now().minusYears(18))) {
                errors.add("Employee must be at least 18 years old.");
            }
        } catch (DateTimeParseException ex) {
            errors.add("Birthday must be in MM/DD/YYYY format (entered: \"" + v + "\").");
        }
    }

    private static void checkPhone(String value, List<String> errors) {
        String v = safeTrim(value);
        if (v.isEmpty()) {
            errors.add("Phone Number cannot be empty.");
        } else if (!v.matches("\\d{3,4}-\\d{3}-\\d{3,4}")) {
            errors.add("Phone Number format should be NNN-NNN-NNN (entered: \"" + v + "\").");
        }
    }

    private static void checkPattern(String value, String fieldName, String pattern,
                                     String hint, List<String> errors) {
        String v = safeTrim(value);
        if (v.isEmpty()) {
            errors.add(fieldName + " cannot be empty.");
        } else if (!v.matches(pattern)) {
            errors.add(fieldName + " is invalid. " + hint + " (entered: \"" + v + "\").");
        }
    }

    private static void checkMoney(String value, String fieldName, boolean required, List<String> errors) {
        String cleaned = safeTrim(value).replace(",", "");
        if (cleaned.isEmpty()) {
            if (required) errors.add(fieldName + " cannot be empty.");
            return;
        }
        try {
            double d = Double.parseDouble(cleaned);
            if (d < 0) {
                errors.add(fieldName + " cannot be negative.");
            }
        } catch (NumberFormatException e) {
            errors.add(fieldName + " must be a valid number (entered: \"" + safeTrim(value) + "\").");
        }
    }

    private static String safeTrim(String v) {
        return (v == null) ? "" : v.replace("\"", "").trim();
    }

    private static String safeGet(String[] row, int col) {
        if (col >= row.length) return "";
        return safeTrim(row[col]);
    }

    // FILE WATCHER (dynamic table refresh whenever the CSV changes on disk)
    // -------------------------------------------------------------------------
    private static Thread watchThread;

    /**
     * Starts a background watcher that reloads the employee CSV and invokes
     * {@code onChange} whenever the file is modified on disk.
     *
     * @param onChange  Callback run after the in-memory data is reloaded
     *                   (e.g. AdminPanel::refreshTable). Safe to call from
     *                   any thread; the callback is responsible for
     *                   dispatching to the EDT if it touches Swing components.
     */
    public static void startFileWatcher(Runnable onChange) {
        stopFileWatcher();
        watchThread = new Thread(() -> {
            try {
                Path dir  = Paths.get(AppConstants.EMPLOYEE_FILE).toAbsolutePath().getParent();
                Path file = Paths.get(AppConstants.EMPLOYEE_FILE).toAbsolutePath().getFileName();

                WatchService watcher = FileSystems.getDefault().newWatchService();
                dir.register(watcher, StandardWatchEventKinds.ENTRY_MODIFY);

                while (!Thread.currentThread().isInterrupted()) {
                    WatchKey key;
                    try {
                        key = watcher.take();
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                        break;
                    }

                    for (WatchEvent<?> event : key.pollEvents()) {
                        if (event.context() instanceof Path &&
                                file.equals(event.context())) {
                            // Small delay to ensure the write is complete
                            Thread.sleep(200);
                            DataStore.loadEmployeeData();
                            onChange.run();
                        }
                    }
                    key.reset();
                }
                watcher.close();
            } catch (Exception e) {
                // Watcher stopped — non-fatal
            }
        }, "EmployeeFileWatcher");
        watchThread.setDaemon(true);
        watchThread.start();
    }

    public static void stopFileWatcher() {
        if (watchThread != null && watchThread.isAlive()) {
            watchThread.interrupt();
        }
    }
}
