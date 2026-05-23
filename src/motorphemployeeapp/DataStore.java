package motorphemployeeapp;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * DataStore
 *
 * Owns the in-memory copies of the two CSV files and exposes
 * methods to (re)load them. All other service classes read
 * data through this class instead of touching the file system
 * directly, keeping I/O in one place.
 *
 * Responsibilities:
 *   - Load employee CSV into memory
 *   - Load attendance CSV into memory
 *   - Provide read-only access to both data sets
 */
public class DataStore {

    // ─── Prevent instantiation ────────────────────────────────────────────────
    private DataStore() {}

    // ─── In-memory data stores ────────────────────────────────────────────────
    private static ArrayList<String[]> employeeData   = new ArrayList<>();
    private static ArrayList<String[]> attendanceData = new ArrayList<>();

    // LOAD METHODS
    /**
     * Reads the employee CSV file and caches every data row.
     * The header line is skipped automatically.
     * Quoted fields that contain commas are handled by the regex split.
     */
    public static void loadEmployeeData() {
        employeeData.clear();

        try (BufferedReader br = new BufferedReader(new FileReader(AppConstants.EMPLOYEE_FILE))) {
            br.readLine(); // skip header
            String line;
            while ((line = br.readLine()) != null) {
                // Split that respects commas inside double-quoted fields
                String[] row = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
                employeeData.add(row);
            }
        } catch (IOException e) {
            System.out.println("Error reading employee file: " + e.getMessage());
        }
    }

    /**
     * Reads the attendance CSV file and caches every data row.
     * The header line is skipped automatically.
     */
    public static void loadAttendanceData() {
        attendanceData.clear();

        try (BufferedReader br = new BufferedReader(new FileReader(AppConstants.ATTENDANCE_FILE))) {
            br.readLine(); // skip header
            String line;
            while ((line = br.readLine()) != null) {
                attendanceData.add(line.split(","));
            }
        } catch (IOException e) {
            System.out.println("Error reading attendance file: " + e.getMessage());
        }
    }

    // DATA ACCESSORS (read-only views)
    /**
     * Returns the cached employee data rows.
     * Callers should treat this list as read-only.
     */
    public static ArrayList<String[]> getEmployeeData() {
        return employeeData;
    }

    /**
     * Returns the cached attendance data rows.
     * Callers should treat this list as read-only.
     */
    public static ArrayList<String[]> getAttendanceData() {
        return attendanceData;
    }
}
