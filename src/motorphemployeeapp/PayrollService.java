package motorphemployeeapp;

import java.io.*;
import java.util.ArrayList;

/**
 * PayrollService
 *
 * This class does all the payroll math: gross pay, SSS, PhilHealth,
 * Pag-IBIG, tax, total deductions, and net pay. It also builds the
 * text shown on screen, writes the payroll CSV file, and creates the
 * payroll summary.
 */
public class PayrollService {

    private PayrollService() {}

    // Simple holder for one payroll line (one cutoff period for one employee).
    // -------------------------------------------------------------------------
    public static class PayrollRecord {
        public String empNum;
        public String lastName;
        public String firstName;
        public String cutoffDate;   // e.g. "06/01/2024 - 06/15/2024"
        public double hoursWorked;
        public double grossSalary;
        public double sss;
        public double philHealth;
        public double pagIBIG;
        public double tax;
        public double totalDeductions;
        public double netSalary;
    }

    // GROSS SALARY
    // -------------------------------------------------------------------------
    // Gross pay for one cutoff = hours worked x hourly rate.
    public static double calculateGrossSalary(String empNum, double hoursWorked) {
        double hourlyRate = EmployeeService.getHourlyRate(empNum);
        return hoursWorked * hourlyRate;
    }

    // Monthly gross pay = the 1st cutoff gross + the 2nd cutoff gross.
    public static double calculateMonthlyGross(double firstGross, double secondGross) {
        return firstGross + secondGross;
    }

    // MANDATORY DEDUCTIONS
    // -------------------------------------------------------------------------
    // Looks up the SSS contribution for the given monthly gross pay,
    // using the standard SSS contribution bracket table.
    public static double computeSSS(double monthlyGross) {
        if (monthlyGross < 3250)  return 135.00;
        if (monthlyGross >= 24750) return 1125.00;
        int bracket = (int) ((monthlyGross - 3250) / 500);
        return 157.5 + (bracket * 22.5);
    }

    // PhilHealth premium is 3% of monthly gross (capped at 1800), split
    // evenly between employee and employer, so the employee pays half.
    public static double computePhilHealth(double monthlyGross) {
        double totalPremium = Math.min(monthlyGross * 0.03, 1800.00);
        return totalPremium / 2;
    }

    // Pag-IBIG is 1% of monthly gross for the 1000-1500 range, otherwise 2%,
    // capped at a maximum contribution of 100.
    public static double computePagIBIG(double monthlyGross) {
        double contribution;
        if (monthlyGross >= 1000 && monthlyGross <= 1500) {
            contribution = monthlyGross * 0.01;
        } else {
            contribution = monthlyGross * 0.02;
        }
        return Math.min(contribution, 100.00);
    }

    // Taxable income = monthly gross minus the SSS, PhilHealth and Pag-IBIG
    // contributions.
    public static double computeTaxableIncome(double monthlyGross,
                                              double sss,
                                              double philHealth,
                                              double pagIBIG) {
        return monthlyGross - (sss + philHealth + pagIBIG);
    }

    // Withholding tax, using the standard BIR tax brackets.
    public static double computeTax(double taxableIncome) {
        if (taxableIncome <= 20832)   return 0;
        else if (taxableIncome <= 33333)  return (taxableIncome - 20833) * 0.20;
        else if (taxableIncome <= 66667)  return 2500 + (taxableIncome - 33333) * 0.25;
        else if (taxableIncome <= 166667) return 10833 + (taxableIncome - 66667) * 0.30;
        else if (taxableIncome <= 666667) return 40833.33 + (taxableIncome - 166667) * 0.32;
        else                              return 200833.33 + (taxableIncome - 666667) * 0.35;
    }

    // Adds up all four deductions into one total.
    public static double computeDeductions(double sss, double philHealth, double pagIBIG, double tax) {
        return sss + philHealth + pagIBIG + tax;
    }

    // FORMATTED REPORT
    // -------------------------------------------------------------------------
    // Builds the printable payroll report text for one employee, covering
    // either a single month (monthPairIdx 0-6) or all months (-1).
    public static String buildPayrollText(String empNum, int monthPairIdx) throws Exception {
        String[] emp = EmployeeService.findEmployee(empNum);
        if (emp == null) return "Employee " + empNum + " not found.\n\n";

        String name = emp[AppConstants.COL_FIRST_NAME].trim()
                + " " + emp[AppConstants.COL_LAST_NAME].trim();

        StringBuilder sb = new StringBuilder();
        sb.append("╔══════════════════════════════════════════╗\n");
        sb.append(String.format("║  %-40s║%n", "PAYROLL REPORT"));
        sb.append(String.format("║  %-40s║%n", "Employee : " + name));
        sb.append(String.format("║  %-40s║%n", "Emp No.  : " + empNum));
        sb.append(String.format("║  %-40s║%n", "Birthday : " + emp[AppConstants.COL_BIRTHDAY].trim()));
        sb.append("╚══════════════════════════════════════════╝\n\n");

        int startI = (monthPairIdx < 0) ? 0 : monthPairIdx * 2;
        int endI   = (monthPairIdx < 0)
                ? AppConstants.CUTOFF_START.length - 1
                : Math.min(monthPairIdx * 2 + 1, AppConstants.CUTOFF_START.length - 1);

        for (int i = startI; i <= endI; i += 2) {
            String s1 = AppConstants.CUTOFF_START[i];
            String e1 = AppConstants.CUTOFF_END[i];
            String s2 = AppConstants.CUTOFF_START[i + 1];
            String e2 = AppConstants.CUTOFF_END[i + 1];

            double h1 = AttendanceService.calculateHours(empNum, s1, e1);
            double h2 = AttendanceService.calculateHours(empNum, s2, e2);
            double g1 = calculateGrossSalary(empNum, h1);
            double g2 = calculateGrossSalary(empNum, h2);
            double mg = calculateMonthlyGross(g1, g2);

            double sss      = computeSSS(mg);
            double ph       = computePhilHealth(mg);
            double pi       = computePagIBIG(mg);
            double taxable  = computeTaxableIncome(mg, sss, ph, pi);
            double tax      = computeTax(taxable);
            double totalDed = computeDeductions(sss, ph, pi, tax);
            double net2     = g2 - totalDed;

            String monthName = AppConstants.MONTH_NAMES[i / 2];

            // 1st cutoff: no deductions
            sb.append(String.format("%s%n", monthName));
            sb.append(String.format("1st Cutoff  (%s – %s)%n", s1, e1));
            sb.append(String.format("Hours Worked : %.2f hrs%n",  h1));
            sb.append(String.format("Gross Salary : PHP %,.2f%n", g1));
            sb.append(String.format("Net Salary   : PHP %,.2f%n", g1));

            // 2nd cutoff: deductions applied
            sb.append(String.format("%n2nd Cutoff  (%s – %s)%n", s2, e2));
            sb.append(String.format("Hours Worked : %.2f hrs%n",  h2));
            sb.append(String.format("Gross Salary : PHP %,.2f%n", g2));
            sb.append("Deductions:\n");
            sb.append(String.format("  SSS        : PHP %,.2f%n", sss));
            sb.append(String.format("  PhilHealth : PHP %,.2f%n", ph));
            sb.append(String.format("  Pag-IBIG   : PHP %,.2f%n", pi));
            sb.append(String.format("  Tax        : PHP %,.2f%n", tax));
            sb.append(String.format("  ────────────────────────────-%n"));
            sb.append(String.format("  Total Ded. : PHP %,.2f%n", totalDed));
            sb.append(String.format("Net Salary   : PHP %,.2f%n", net2));
            sb.append("\n");
            sb.append("─".repeat(43)).append("\n\n");
        }

        return sb.toString();
    }

    // CSV EXPORT
    // -------------------------------------------------------------------------
    // Computes payroll for one employee (or all employees) and saves the
    // results as a CSV file.
    public static void writePayrollCsv(String empNum, int monthPairIdx, String outputPath)
            throws Exception {

        ArrayList<String> targets = new ArrayList<>();
        if (empNum == null || empNum.isEmpty()) {
            targets.addAll(EmployeeService.getAllEmployeeNumbers());
        } else {
            targets.add(empNum);
        }

        // Determine cutoff range
        int startI = (monthPairIdx < 0) ? 0 : monthPairIdx * 2;
        int endI   = (monthPairIdx < 0)
                ? AppConstants.CUTOFF_START.length - 1
                : Math.min(monthPairIdx * 2 + 1, AppConstants.CUTOFF_START.length - 1);

        // Build all payroll records first
        ArrayList<PayrollRecord> records = new ArrayList<>();

        for (String num : targets) {
            String[] emp = EmployeeService.findEmployee(num);
            if (emp == null) continue;

            String lastName  = emp[AppConstants.COL_LAST_NAME].trim();
            String firstName = emp[AppConstants.COL_FIRST_NAME].trim();

            for (int i = startI; i <= endI; i += 2) {
                String s1 = AppConstants.CUTOFF_START[i];
                String e1 = AppConstants.CUTOFF_END[i];
                String s2 = AppConstants.CUTOFF_START[i + 1];
                String e2 = AppConstants.CUTOFF_END[i + 1];

                double h1 = AttendanceService.calculateHours(num, s1, e1);
                double h2 = AttendanceService.calculateHours(num, s2, e2);
                double g1 = calculateGrossSalary(num, h1);
                double g2 = calculateGrossSalary(num, h2);
                double mg = calculateMonthlyGross(g1, g2);

                double sss     = computeSSS(mg);
                double ph      = computePhilHealth(mg);
                double pi      = computePagIBIG(mg);
                double taxable = computeTaxableIncome(mg, sss, ph, pi);
                double tax     = computeTax(taxable);
                double totalDed = computeDeductions(sss, ph, pi, tax);
                double net2    = g2 - totalDed;

                // 1st cutoff record — no deductions
                PayrollRecord r1 = new PayrollRecord();
                r1.empNum         = num;
                r1.lastName       = lastName;
                r1.firstName      = firstName;
                r1.cutoffDate     = s1 + " - " + e1;
                r1.hoursWorked    = h1;
                r1.grossSalary    = g1;
                r1.sss            = 0;
                r1.philHealth     = 0;
                r1.pagIBIG        = 0;
                r1.tax            = 0;
                r1.totalDeductions = 0;
                r1.netSalary      = g1;
                records.add(r1);

                // 2nd cutoff record — full deductions
                PayrollRecord r2 = new PayrollRecord();
                r2.empNum         = num;
                r2.lastName       = lastName;
                r2.firstName      = firstName;
                r2.cutoffDate     = s2 + " - " + e2;
                r2.hoursWorked    = h2;
                r2.grossSalary    = g2;
                r2.sss            = sss;
                r2.philHealth     = ph;
                r2.pagIBIG        = pi;
                r2.tax            = tax;
                r2.totalDeductions = totalDed;
                r2.netSalary      = net2;
                records.add(r2);
            }
        }

        // Write to CSV (overwrite the file completely each time)
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(outputPath, false))) {
            // Header
            bw.write("Employee #,Last Name,First Name,Cutoff Date,"
                    + "Hours Worked,Gross Salary,SSS,PhilHealth,Pag-IBIG,Tax,"
                    + "Total Deductions,Net Salary");
            bw.newLine();

            for (PayrollRecord r : records) {
                bw.write(
                        r.empNum + ","
                                + csvField(r.lastName)  + ","
                                + csvField(r.firstName) + ","
                                + csvField(r.cutoffDate) + ","
                                + String.format("%.2f", r.hoursWorked)    + ","
                                + String.format("%.2f", r.grossSalary)    + ","
                                + String.format("%.2f", r.sss)            + ","
                                + String.format("%.2f", r.philHealth)     + ","
                                + String.format("%.2f", r.pagIBIG)        + ","
                                + String.format("%.2f", r.tax)            + ","
                                + String.format("%.2f", r.totalDeductions) + ","
                                + String.format("%.2f", r.netSalary)
                );
                bw.newLine();
            }
        }
    }

    // Wraps a field in double-quotes if it contains a comma.
    private static String csvField(String value) {
        if (value == null) return "";
        if (value.contains(",")) return "\"" + value + "\"";
        return value;
    }

    // PAYROLL SUMMARY
    // -------------------------------------------------------------------------
    // Simple holder for the totals shown on the Payroll Summary screen.
    public static class PayrollSummary {
        public int    employeeCount;
        public double totalGrossPay;
        public double totalDeductions;
        public double averageMonthlyNetPay;
    }

    //Adds up payroll totals for every employee currently loaded, for the
    // given month range, and returns them as a PayrollSummary.
    public static PayrollSummary generateSummary(int monthPairIdx) throws Exception {
        ArrayList<String> allNums = EmployeeService.getAllEmployeeNumbers();

        if (allNums.isEmpty()) {
            throw new IllegalStateException(
                    "No employee data is currently loaded. Please make sure the "
                            + "employee CSV file has been loaded before generating a summary.");
        }

        int startI = (monthPairIdx < 0) ? 0 : monthPairIdx * 2;
        int endI   = (monthPairIdx < 0)
                ? AppConstants.CUTOFF_START.length - 1
                : Math.min(monthPairIdx * 2 + 1, AppConstants.CUTOFF_START.length - 1);

        double totalGross      = 0;
        double totalDeductions = 0;
        double totalNet        = 0;

        for (String num : allNums) {
            for (int i = startI; i <= endI; i += 2) {
                String s1 = AppConstants.CUTOFF_START[i];
                String e1 = AppConstants.CUTOFF_END[i];
                String s2 = AppConstants.CUTOFF_START[i + 1];
                String e2 = AppConstants.CUTOFF_END[i + 1];

                double h1 = AttendanceService.calculateHours(num, s1, e1);
                double h2 = AttendanceService.calculateHours(num, s2, e2);
                double g1 = calculateGrossSalary(num, h1);
                double g2 = calculateGrossSalary(num, h2);
                double mg = calculateMonthlyGross(g1, g2);

                double sss      = computeSSS(mg);
                double ph       = computePhilHealth(mg);
                double pi       = computePagIBIG(mg);
                double taxable  = computeTaxableIncome(mg, sss, ph, pi);
                double tax      = computeTax(taxable);
                double totalDed = computeDeductions(sss, ph, pi, tax);
                double net2     = g2 - totalDed;

                // 1st cutoff has no deductions; 2nd cutoff carries full deductions
                totalGross      += (g1 + g2);
                totalDeductions += totalDed;
                totalNet        += (g1 + net2);
            }
        }

        // Number of months covered by this summary
        int monthsCovered = (endI - startI) / 2 + 1;

        // Total employee-months = number of employees x number of months.
        int totalEmployeeMonths = allNums.size() * monthsCovered;

        PayrollSummary summary = new PayrollSummary();
        summary.employeeCount        = allNums.size();
        summary.totalGrossPay        = totalGross;
        summary.totalDeductions      = totalDeductions;
        summary.averageMonthlyNetPay = totalNet / totalEmployeeMonths;
        return summary;
    }

    // Turns a PayrollSummary into the text block shown on screen.
    public static String buildSummaryText(PayrollSummary s) {
        StringBuilder sb = new StringBuilder();
        sb.append("╔══════════════════════════════════════════╗\n");
        sb.append(String.format("║  %-40s║%n", "PAYROLL SUMMARY"));
        sb.append("╚══════════════════════════════════════════╝\n\n");
        sb.append(String.format("%-22s : %d%n",       "Total Employees",       s.employeeCount));
        sb.append(String.format("%-22s : PHP %,.2f%n", "Total Gross Pay",       s.totalGrossPay));
        sb.append(String.format("%-22s : PHP %,.2f%n", "Total Deductions",      s.totalDeductions));
        sb.append(String.format("%-22s : PHP %,.2f%n", "Avg. Monthly Net Pay",  s.averageMonthlyNetPay));
        return sb.toString();
    }
}
