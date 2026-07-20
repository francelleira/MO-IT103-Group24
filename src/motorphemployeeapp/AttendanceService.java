package motorphemployeeapp;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

/**
 * AttendanceService
 *
 * Turns raw login/logout attendance records into payable hours worked,
 * using the data already loaded by DataStore.
 */
public class AttendanceService {

    private AttendanceService() {}

    // Date / time formatters
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("H:mm");

    // Business-hour boundaries
    private static final LocalTime WORK_START  = LocalTime.of(8,  0);
    private static final LocalTime WORK_END    = LocalTime.of(17, 0);
    private static final LocalTime GRACE_LIMIT = LocalTime.of(8, 10);

    // HOURS CALCULATION
    // Adds up the payable hours one employee worked between two dates.
    public static double calculateHours(String empNum, String startDate, String endDate)
            throws Exception {

        double total = 0;

        LocalDate start = LocalDate.parse(startDate, DATE_FORMAT);
        LocalDate end   = LocalDate.parse(endDate,   DATE_FORMAT);

        ArrayList<String[]> attendance = DataStore.getAttendanceData();

        for (String[] row : attendance) {
            String rowEmpNum = row[AppConstants.ATT_EMP_NUM].trim();

            // Skip rows that do not belong to this employee
            if (!rowEmpNum.equals(empNum)) {
                continue;
            }

            LocalDate workDate = LocalDate.parse(row[AppConstants.ATT_DATE].trim(), DATE_FORMAT);

            // Skip rows outside the cutoff window
            if (workDate.isBefore(start) || workDate.isAfter(end)) {
                continue;
            }

            LocalTime login  = LocalTime.parse(row[AppConstants.ATT_LOGIN ].trim(), TIME_FORMAT);
            LocalTime logout = LocalTime.parse(row[AppConstants.ATT_LOGOUT].trim(), TIME_FORMAT);

            total += calculateDailyHours(login, logout);
        }

        return total;
    }

    /**
     * Calculates payable hours for a single attendance record.
     *
     * Rules applied:
     *  1. Grace period: logins at or before 8:10 AM are treated as 8:00 AM.
     *  2. Overtime cap:  any logout after 5:00 PM is capped at 5:00 PM.
     *  3. Lunch break:   one hour is deducted from every full day.
     *  4. Floor at zero: invalid/incomplete records never return negative hours.
     */
    public static double calculateDailyHours(LocalTime login, LocalTime logout) {

        // Rule 1 — grace period
        if (!login.isAfter(GRACE_LIMIT)) {
            login = WORK_START;
        }

        // Rule 2 — overtime cap
        if (logout.isAfter(WORK_END)) {
            logout = WORK_END;
        }

        // Rule 3 — deduct lunch break
        double hours = Duration.between(login, logout).toMinutes() / 60.0 - 1.0;

        // Rule 4 — floor at zero
        return Math.max(hours, 0);
    }
}
