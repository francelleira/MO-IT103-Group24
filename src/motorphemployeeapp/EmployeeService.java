package motorphemployeeapp;

import java.util.ArrayList;

/**
 * EmployeeService
 *
 * Provides all employee-related query operations that work against
 * the data loaded by DataStore. No file I/O occurs here.
 */
public class EmployeeService {

    // ─── Prevent instantiation ────────────────────────────────────────────────
    private EmployeeService() {}

    // QUERY METHODS
    /**
     * Searches DataStore for an employee row whose employee-number
     * column matches the given empNum string exactly.
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
     */
    public static boolean employeeExists(String empNum) {
        return findEmployee(empNum) != null;
    }

    /**
     * Returns the hourly rate for the given employee.
     * Strips surrounding double-quote characters that may appear in
     * formatted CSV cells before parsing.
     */
    public static double getHourlyRate(String empNum) {
        String[] row = findEmployee(empNum);

        if (row == null) {
            return 0;
        }

        String rateStr = row[AppConstants.COL_HOURLY_RATE].replace("\"", "").trim();
        return Double.parseDouble(rateStr);
    }

    //Collects and returns every employee number present in DataStore.
    public static ArrayList<String> getAllEmployeeNumbers() {
        ArrayList<String[]> data    = DataStore.getEmployeeData();
        ArrayList<String>   empNums = new ArrayList<>();

        for (String[] row : data) {
            empNums.add(row[AppConstants.COL_EMP_NUM].trim());
        }

        return empNums;
    }
}
