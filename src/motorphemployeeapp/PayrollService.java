package motorphemployeeapp;

/**
 * PayrollService
 *
 * Performs every monetary calculation needed to produce a payslip.
 * This class has no knowledge of the UI or of file I/O — it works
 * purely with numbers passed in and returns computed results.
 *
 * Responsibilities:
 *   - Gross salary computation
 *   - Monthly gross aggregation
 *   - SSS contribution lookup
 *   - PhilHealth premium calculation
 *   - Pag-IBIG contribution calculation
 *   - Taxable income derivation
 *   - Withholding tax computation
 *   - Total deductions aggregation
 *   - Net pay computation
 *   - Formatted payroll report text generation
 */
public class PayrollService {

    // ─── Prevent instantiation ────────────────────────────────────────────────
    private PayrollService() {}

    // GROSS SALARY
    /**
     * Computes the gross salary for one cutoff period.
     *
     * @param empNum       Employee number (used to look up the hourly rate).
     * @param hoursWorked  Total payable hours in the period.
     * @return             Gross salary = hoursWorked × hourlyRate.
     */
    public static double calculateGrossSalary(String empNum, double hoursWorked) {
        double hourlyRate = EmployeeService.getHourlyRate(empNum);
        return hoursWorked * hourlyRate;
    }

    /**
     * Adds the two cutoff gross salaries to form the monthly gross.
     *
     * @param firstGross   Gross salary from the 1st cutoff.
     * @param secondGross  Gross salary from the 2nd cutoff.
     * @return             Combined monthly gross salary.
     */
    public static double calculateMonthlyGross(double firstGross, double secondGross) {
        return firstGross + secondGross;
    }

    // MANDATORY DEDUCTIONS
    /**
     * Computes the employee's SSS contribution using the bracket table.
     *
     * The bracket table steps by PHP 500 of monthly salary, adding PHP 22.50
     * of contribution per bracket, starting at PHP 157.50 for the first bracket
     * (PHP 3,250–3,749).
     *
     * @param monthlyGross  Combined gross salary for the month.
     * @return              SSS contribution amount.
     */
    public static double computeSSS(double monthlyGross) {

        if (monthlyGross < 3250) {
            return 135.00; // minimum contribution
        }

        if (monthlyGross >= 24750) {
            return 1125.00; // maximum contribution cap
        }

        int bracket = (int) ((monthlyGross - 3250) / 500);
        return 157.5 + (bracket * 22.5);
    }

    /**
     * Computes the employee's share of the PhilHealth premium.
     *
     * Premium rate is 3 % of monthly gross, split equally between
     * employer and employee. Total premium is capped at PHP 1,800.
     *
     * @param monthlyGross  Combined gross salary for the month.
     * @return              Employee's PhilHealth share.
     */
    public static double computePhilHealth(double monthlyGross) {
        double totalPremium = Math.min(monthlyGross * 0.03, 1800.00);
        return totalPremium / 2; // employee pays half
    }

    /**
     * Computes the employee's Pag-IBIG (HDMF) contribution.
     *
     * Rate is 1 % for salaries PHP 1,000–1,500 and 2 % otherwise.
     * Contribution is capped at PHP 100.
     *
     * @param monthlyGross  Combined gross salary for the month.
     * @return              Pag-IBIG contribution amount.
     */
    public static double computePagIBIG(double monthlyGross) {
        double contribution;

        if (monthlyGross >= 1000 && monthlyGross <= 1500) {
            contribution = monthlyGross * 0.01;
        } else {
            contribution = monthlyGross * 0.02;
        }

        return Math.min(contribution, 100.00); // PHP 100 cap
    }

    // INCOME TAX
    /**
     * Derives taxable income by subtracting mandatory deductions from
     * the monthly gross salary.
     *
     * @param monthlyGross  Monthly gross salary.
     * @param sss           SSS contribution.
     * @param philHealth    PhilHealth employee share.
     * @param pagIBIG       Pag-IBIG contribution.
     * @return              Taxable income.
     */
    public static double computeTaxableIncome(double monthlyGross,
                                               double sss,
                                               double philHealth,
                                               double pagIBIG) {
        return monthlyGross - (sss + philHealth + pagIBIG);
    }

    /**
     * Computes withholding tax using the BIR monthly tax table.
     *
     * Brackets (monthly taxable income → tax formula):
     *   ≤ 20,832            →  0
     *   20,833 – 33,333     →  20 % of excess over 20,833
     *   33,334 – 66,667     →  2,500  + 25 % of excess over 33,333
     *   66,668 – 166,667    →  10,833 + 30 % of excess over 66,667
     *   166,668 – 666,667   →  40,833.33 + 32 % of excess over 166,667
     *   > 666,667           →  200,833.33 + 35 % of excess over 666,667
     *
     * @param taxableIncome  Monthly taxable income.
     * @return               Withholding tax amount.
     */
    public static double computeTax(double taxableIncome) {

        if (taxableIncome <= 20832) {
            return 0;
        } else if (taxableIncome <= 33333) {
            return (taxableIncome - 20833) * 0.20;
        } else if (taxableIncome <= 66667) {
            return 2500 + (taxableIncome - 33333) * 0.25;
        } else if (taxableIncome <= 166667) {
            return 10833 + (taxableIncome - 66667) * 0.30;
        } else if (taxableIncome <= 666667) {
            return 40833.33 + (taxableIncome - 166667) * 0.32;
        } else {
            return 200833.33 + (taxableIncome - 666667) * 0.35;
        }
    }

    // TOTALS
    /**
     * Sums all mandatory deductions into one total.
     *
     * @param sss        SSS contribution.
     * @param philHealth PhilHealth employee share.
     * @param pagIBIG    Pag-IBIG contribution.
     * @param tax        Withholding tax.
     * @return           Total deductions.
     */
    public static double computeDeductions(double sss,
                                            double philHealth,
                                            double pagIBIG,
                                            double tax) {
        return sss + philHealth + pagIBIG + tax;
    }

    // FORMATTED REPORT
    /**
     * Builds a fully formatted payroll report string for one employee,
     * covering either a single month or all months (June–December).
     *
     * @param empNum        Employee number.
     * @param monthPairIdx  0-based month index (0 = June … 6 = December).
     *                      Pass -1 to include all months.
     * @return              Multi-line text report ready for display.
     * @throws Exception    If attendance date/time parsing fails.
     */
    public static String buildPayrollText(String empNum, int monthPairIdx) throws Exception {

        String[] emp = EmployeeService.findEmployee(empNum);
        if (emp == null) {
            return "Employee " + empNum + " not found.\n\n";
        }

        String name = emp[AppConstants.COL_FIRST_NAME].trim()
                    + " " + emp[AppConstants.COL_LAST_NAME].trim();

        StringBuilder sb = new StringBuilder();
        sb.append("╔══════════════════════════════════════════╗\n");
        sb.append(String.format("║  %-42s║%n", "PAYROLL REPORT"));
        sb.append(String.format("║  %-42s║%n", "Employee : " + name));
        sb.append(String.format("║  %-42s║%n", "Emp No.  : " + empNum));
        sb.append("╚══════════════════════════════════════════╝\n\n");

        // Determine the index range for the cutoff arrays
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

            sb.append(String.format("  ▸ %s%n", monthName));
            sb.append(String.format("  ┌─ 1st Cutoff  (%s – %s)%n", s1, e1));
            sb.append(String.format("  │  Hours Worked : %.2f hrs%n",    h1));
            sb.append(String.format("  │  Gross Salary : PHP %,.2f%n",   g1));
            sb.append(String.format("  │  Net Salary   : PHP %,.2f  (no deductions yet)%n", g1));
            sb.append(String.format("  ├─ 2nd Cutoff  (%s – %s)%n", s2, e2));
            sb.append(String.format("  │  Hours Worked : %.2f hrs%n",    h2));
            sb.append(String.format("  │  Gross Salary : PHP %,.2f%n",   g2));
            sb.append(String.format("  ├─ Monthly Gross: PHP %,.2f%n",   mg));
            sb.append("  │\n");
            sb.append("  │  Deductions (applied on 2nd cutoff):\n");
            sb.append(String.format("  │    SSS        : PHP %,.2f%n",   sss));
            sb.append(String.format("  │    PhilHealth : PHP %,.2f%n",   ph));
            sb.append(String.format("  │    Pag-IBIG   : PHP %,.2f%n",   pi));
            sb.append(String.format("  │    Tax        : PHP %,.2f  (taxable: PHP %,.2f)%n", tax, taxable));
            sb.append(String.format("  │    ─────────────────────────────%n"));
            sb.append(String.format("  │    Total Ded. : PHP %,.2f%n",   totalDed));
            sb.append(String.format("  └─ NET PAY (2nd): PHP %,.2f%n",   net2));
            sb.append("\n");
        }

        sb.append("─".repeat(46)).append("\n\n");
        return sb.toString();
    }
}
