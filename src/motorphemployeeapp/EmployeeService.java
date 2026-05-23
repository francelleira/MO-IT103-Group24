package motorphemployeeapp;

import java.util.ArrayList;

/**
 * EmployeeService
 *
 * Provides all employee-related query operations that work against
 * the data loaded by DataStore. No file I/O occurs here.
 *
 * Responsibilities:
 *   - Find a single employee row by employee number
 *   - Check whether an employee exists
 *   - Retrieve an employee's hourly rate
 *   - Retrieve the full list of employee numbers
 */
public class EmployeeService {

    // ─── Prevent instantiation ────────────────────────────────────────────────
    private EmployeeService() {}

    // QUERY METHODS
    /**
     * Searches DataStore for an employee row whose employee-number
     * column matches the given empNum string exactly.
     *
     * @param empNum  The employee number to search for.
     * @return        The matching row array, or null if not found.
     */
    public static String[] findEmployee(String empNum) {
        ArrayList<String[]> data = DataStore.getEmployeeData();

        for (String[] row : data) {
            if (row[AppConstants.COL_EMP_NUM].trim().equals(empNum)) {
                return row;
            }
        }

        return null;
    }

    /**
     * Convenience check: returns true when the employee number
     * exists in the loaded data.
     *
     * @param empNum  The employee number to check.
     * @return        true if the employee exists, false otherwise.
     */
    public static boolean employeeExists(String empNum) {
        return findEmployee(empNum) != null;
    }

    /**
     * Returns the hourly rate for the given employee.
     * Strips surrounding double-quote characters that may appear in
     * formatted CSV cells before parsing.
     *
     * @param empNum  The employee number.
     * @return        The hourly rate as a double, or 0 if not found.
     */
    public static double getHourlyRate(String empNum) {
        String[] row = findEmployee(empNum);

        if (row == null) {
            return 0;
        }

        String rateStr = row[AppConstants.COL_HOURLY_RATE].replace("\"", "").trim();
        return Double.parseDouble(rateStr);
    }

    /**
     * Collects and returns every employee number present in DataStore.
     *
     * @return  An ArrayList of employee number strings.
     */
    public static ArrayList<String> getAllEmployeeNumbers() {
        ArrayList<String[]> data    = DataStore.getEmployeeData();
        ArrayList<String>   empNums = new ArrayList<>();

        for (String[] row : data) {
            empNums.add(row[AppConstants.COL_EMP_NUM].trim());
        }

        return empNums;
    }
}
