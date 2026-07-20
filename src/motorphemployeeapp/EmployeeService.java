package motorphemployeeapp;

import java.io.IOException;
import java.util.ArrayList;

/**
 * EmployeeService
 *
 * Handles everything related to individual employees: looking them up,
 * adding, updating, and deleting them.
 */
public class EmployeeService {

    private EmployeeService() {}

    // QUERY METHODS
    // -------------------------------------------------------------------------
    // Finds and returns one employee's row by employee number, or null
    // if no employee with that number exists.
    public static String[] findEmployee(String empNum) {
        ArrayList<String[]> data = DataStore.getEmployeeData();
        for (String[] row : data) {
            if (row[AppConstants.COL_EMP_NUM].trim().equals(empNum)) {
                return row;
            }
        }
        return null;
    }

    // True if an employee with this number exists.
    public static boolean employeeExists(String empNum) {
        return findEmployee(empNum) != null;
    }

    // Returns the hourly rate for one employee, or 0 if not found/invalid.
    public static double getHourlyRate(String empNum) {
        String[] row = findEmployee(empNum);
        if (row == null) return 0;
        String rateStr = row[AppConstants.COL_HOURLY_RATE].replace("\"", "").trim();
        try {
            return Double.parseDouble(rateStr);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    // Returns the employee number of every loaded employee.
    public static ArrayList<String> getAllEmployeeNumbers() {
        ArrayList<String[]> data    = DataStore.getEmployeeData();
        ArrayList<String>   empNums = new ArrayList<>();
        for (String[] row : data) {
            empNums.add(row[AppConstants.COL_EMP_NUM].trim());
        }
        return empNums;
    }

    // Returns the next sequential employee number: the highest existing
    // employee number, plus 1. Returns 10001 if there are no employees yet.
    public static String getNextEmployeeNumber() {
        ArrayList<String[]> data = DataStore.getEmployeeData();
        int maxNum = 10000; // starting base; next will be 10001
        for (String[] row : data) {
            try {
                int num = Integer.parseInt(row[AppConstants.COL_EMP_NUM].trim());
                if (num > maxNum) {
                    maxNum = num;
                }
            } catch (NumberFormatException e) {
                // skip non-numeric employee numbers
            }
        }
        return String.valueOf(maxNum + 1);
    }

    // ADD NEW EMPLOYEE
    // -------------------------------------------------------------------------
    // Validates a new employee row, then saves it to the CSV file.
    public static void addEmployee(String[] row) throws IllegalArgumentException, IOException {
        if (row.length != AppConstants.TOTAL_COLUMNS) {
            throw new IllegalArgumentException(
                    "Row must have " + AppConstants.TOTAL_COLUMNS + " fields.");
        }

        String empNum = row[AppConstants.COL_EMP_NUM].trim();

        if (empNum.isEmpty()) {
            throw new IllegalArgumentException("Employee Number cannot be empty.");
        }
        if (!empNum.matches("\\d+")) {
            throw new IllegalArgumentException(
                    "Employee Number must contain digits only (entered: \"" + empNum + "\").");
        }
        if (employeeExists(empNum)) {
            throw new IllegalArgumentException(
                    "Employee Number " + empNum + " already exists. Use a different number.");
        }
        if (row[AppConstants.COL_LAST_NAME].trim().isEmpty()) {
            throw new IllegalArgumentException("Last Name cannot be empty.");
        }
        if (row[AppConstants.COL_FIRST_NAME].trim().isEmpty()) {
            throw new IllegalArgumentException("First Name cannot be empty.");
        }

        // Validate numeric fields that must be parseable
        validateNumericField(row[AppConstants.COL_HOURLY_RATE],    "Hourly Rate");
        validateNumericField(row[AppConstants.COL_BASIC_SALARY],   "Basic Salary");

        DataStore.appendEmployeeRow(row);
    }

    // UPDATE EXISTING EMPLOYEE
    // -------------------------------------------------------------------------
    // Validates the changes, then replaces the matching row and saves the
    // whole file back to disk.
    public static void updateEmployee(String[] row) throws IllegalArgumentException, IOException {
        String empNum = row[AppConstants.COL_EMP_NUM].trim();

        if (!employeeExists(empNum)) {
            throw new IllegalArgumentException(
                    "Employee Number " + empNum + " not found. Cannot update.");
        }
        if (row[AppConstants.COL_LAST_NAME].trim().isEmpty()) {
            throw new IllegalArgumentException("Last Name cannot be empty.");
        }
        if (row[AppConstants.COL_FIRST_NAME].trim().isEmpty()) {
            throw new IllegalArgumentException("First Name cannot be empty.");
        }

        validateNumericField(row[AppConstants.COL_HOURLY_RATE],    "Hourly Rate");
        validateNumericField(row[AppConstants.COL_BASIC_SALARY],   "Basic Salary");

        // Deep-copy the row so the in-memory store is independent of the form's text fields
        String[] rowCopy = new String[row.length];
        System.arraycopy(row, 0, rowCopy, 0, row.length);

        ArrayList<String[]> data = DataStore.getEmployeeData();
        for (int i = 0; i < data.size(); i++) {
            if (data.get(i)[AppConstants.COL_EMP_NUM].trim().equals(empNum)) {
                data.set(i, rowCopy);
                break;
            }
        }

        DataStore.saveAllEmployeeData();
        DataStore.loadEmployeeData();
    }

    // DELETE EMPLOYEE
    // -------------------------------------------------------------------------
    // Removes the matching employee row and saves the whole file back to disk.
    public static void deleteEmployee(String empNum) throws IllegalArgumentException, IOException {
        if (!employeeExists(empNum)) {
            throw new IllegalArgumentException(
                    "Employee Number " + empNum + " not found. Cannot delete.");
        }

        ArrayList<String[]> data = DataStore.getEmployeeData();
        for (int i = 0; i < data.size(); i++) {
            if (data.get(i)[AppConstants.COL_EMP_NUM].trim().equals(empNum)) {
                data.remove(i);
                break;
            }
        }

        DataStore.saveAllEmployeeData();
        DataStore.loadEmployeeData();
    }

    // HELPER
    // -------------------------------------------------------------------------
    // Validates that the given field string is a non-negative number.
    private static void validateNumericField(String value, String fieldName)
            throws IllegalArgumentException {
        String cleaned = value.replace("\"", "").replace(",", "").trim();
        if (cleaned.isEmpty()) return; // optional numeric fields may be blank
        try {
            double d = Double.parseDouble(cleaned);
            if (d < 0) {
                throw new IllegalArgumentException(fieldName + " must be a positive number.");
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    fieldName + " must be a valid number (entered: \"" + value.trim() + "\").");
        }
    }
}
