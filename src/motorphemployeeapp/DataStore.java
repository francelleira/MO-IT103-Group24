package motorphemployeeapp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

/**
 * DataStore
 *
 * Owns the in-memory copies of the two CSV files and exposes
 * methods to (re)load them. All other service classes read
 * data through this class instead of touching the file system
 * directly, keeping I/O in one place.
 */
public class DataStore {

    // Prevent instantiation
    private DataStore() {}

    // In-memory data stores
    private static ArrayList<String[]> employeeData   = new ArrayList<>();
    private static ArrayList<String[]> attendanceData = new ArrayList<>();

    // LOAD METHODS
    // -------------------------------------------------------------------------
    // Reads the employee CSV file and caches every data row.
    public static void loadEmployeeData() {
        employeeData.clear();

        try (BufferedReader br = new BufferedReader(new FileReader(AppConstants.EMPLOYEE_FILE))) {
            br.readLine(); // skip header line
            String line;
            while ((line = br.readLine()) != null) {
                // Split that respects commas inside double-quoted fields
                String[] row = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
                employeeData.add(row);
            }
        } catch (IOException e) {
            System.out.println("Error reading employee file: " + e.getMessage());
        }
    }

    // Reads the attendance CSV file and caches every data row.
    public static void loadAttendanceData() {
        attendanceData.clear();

        try (BufferedReader br = new BufferedReader(new FileReader(AppConstants.ATTENDANCE_FILE))) {
            br.readLine(); // skip header line
            String line;
            while ((line = br.readLine()) != null) {
                attendanceData.add(line.split(","));
            }
        } catch (IOException e) {
            System.out.println("Error reading attendance file: " + e.getMessage());
        }
    }

    // DATA ACCESSORS (read-only views)
    // -------------------------------------------------------------------------
    // Returns the cached employee data rows.
    public static ArrayList<String[]> getEmployeeData() {
        return employeeData;
    }

    // Returns the cached attendance data rows.
    public static ArrayList<String[]> getAttendanceData() {
        return attendanceData;
    }

    // WRITE METHODS
    // -------------------------------------------------------------------------
    /**
     * Appends a single new employee row to the CSV file,
     * then reloads the in-memory store so the table refreshes.
     *
     * The row array must have exactly AppConstants.TOTAL_COLUMNS elements.
     * Fields that contain commas are wrapped in double-quotes automatically.
     *
     * @param row  String array with all 19 employee fields in order.
     * @throws IOException if the file cannot be written.
     */
    public static void appendEmployeeRow(String[] row) throws IOException {
        // Build a single CSV line from the array
        String csvLine = buildCsvLine(row);

        // Append mode: true keeps existing content
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(AppConstants.EMPLOYEE_FILE, true))) {
            bw.newLine();           // start on a fresh line
            bw.write(csvLine);
        }

        // Reload so the in-memory list reflects the new record
        loadEmployeeData();
    }

    // OVERWRITE METHOD
    // -------------------------------------------------------------------------
    /**
     * Writes the entire current in-memory employee list back to the CSV file,
     * replacing its previous contents.  Used after an update or delete operation.
     *
     * Writes the header line first, then every row in employeeData.
     *
     * @throws IOException if the file cannot be written.
     */
    public static void saveAllEmployeeData() throws IOException {
        // false = overwrite (not append)
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(AppConstants.EMPLOYEE_FILE, false))) {
            bw.write(AppConstants.EMPLOYEE_CSV_HEADER);
            for (String[] row : employeeData) {
                bw.newLine();
                bw.write(buildCsvLine(row));
            }
        }
    }

    // HELPER
    // -------------------------------------------------------------------------
    /**
     * Converts a String array into a single CSV line.
     * Any field that contains a comma is wrapped in double-quotes.
     *
     * @param row  String array of field values.
     * @return     Comma-separated line ready for file output.
     */
    public static String buildCsvLine(String[] row) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < row.length; i++) {
            String field = (row[i] == null) ? "" : row[i].trim();

            // Remove existing surrounding quotes
            if (field.startsWith("\"") && field.endsWith("\"")) {
                field = field.substring(1, field.length() - 1);
            }

            // Escape quotes inside the field
            field = field.replace("\"", "\"\"");

            // Quote fields containing commas, quotes, or newlines
            if (field.contains(",") || field.contains("\"") || field.contains("\n")) {
                field = "\"" + field + "\"";
            }

            sb.append(field);

            if (i < row.length - 1) {
                sb.append(",");
            }
        }

        return sb.toString();
    }
}
