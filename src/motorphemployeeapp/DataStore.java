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
 * Keeps the employee and attendance data in memory after reading it
 * from the two CSV files. Every other class reads and writes data
 * through this class instead of touching the files directly, so all
 * the file I/O stays in one place.
 */
public class DataStore {

    private DataStore() {}

    // In-memory data stores
    private static ArrayList<String[]> employeeData   = new ArrayList<>();
    private static ArrayList<String[]> attendanceData = new ArrayList<>();

    // LOAD METHODS
    // -------------------------------------------------------------------------
    // Reads the employee CSV file from disk and stores every row in memory.
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

    // Reads the attendance CSV file from disk and stores every row in memory.
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
    // Returns the employee rows currently held in memory.
    public static ArrayList<String[]> getEmployeeData() {
        return employeeData;
    }

    // Returns the attendance rows currently held in memory.
    public static ArrayList<String[]> getAttendanceData() {
        return attendanceData;
    }

    // WRITE METHODS
    // -------------------------------------------------------------------------
    // Adds one new employee row to the end of the CSV file, then reloads
    // the in-memory data so the table shows the new record right away.
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
    // Rewrites the whole employee CSV file using the current in-memory
    // list, replacing everything that was there before.
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
    // Converts a String array into a single CSV line.
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
