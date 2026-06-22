package motorphemployeeapp;

import java.awt.*;

/**
 * AppConstants
 *
 * Central repository for every constant used across the application.
 */
public class AppConstants {

    // Prevent instantiation
    private AppConstants() {}

    // FILE PATHS
    public static final String EMPLOYEE_FILE   = "MotorPH_Employee_Details.csv";
    public static final String ATTENDANCE_FILE = "Attendance_Record.csv";

    // EMPLOYEE CSV COLUMN INDICES
    public static final int COL_EMP_NUM            = 0;
    public static final int COL_LAST_NAME          = 1;
    public static final int COL_FIRST_NAME         = 2;
    public static final int COL_BIRTHDAY           = 3;
    public static final int COL_ADDRESS            = 4;
    public static final int COL_PHONE              = 5;
    public static final int COL_SSS                = 6;
    public static final int COL_PHILHEALTH         = 7;
    public static final int COL_TIN                = 8;
    public static final int COL_PAGIBIG            = 9;
    public static final int COL_STATUS             = 10;
    public static final int COL_POSITION           = 11;
    public static final int COL_SUPERVISOR         = 12;
    public static final int COL_BASIC_SALARY       = 13;
    public static final int COL_RICE_SUBSIDY       = 14;
    public static final int COL_PHONE_ALLOWANCE    = 15;
    public static final int COL_CLOTHING_ALLOWANCE = 16;
    public static final int COL_GROSS_SEMI_MONTHLY = 17;
    public static final int COL_HOURLY_RATE        = 18;

    // Total number of columns expected per employee row
    public static final int TOTAL_COLUMNS = 19;

    // CSV header line written when a new file is created or records appended
    public static final String EMPLOYEE_CSV_HEADER =
            "Employee #,Last Name,First Name,Birthday,Address,Phone Number," +
                    "SSS #,Philhealth #,TIN #,Pag-ibig #,Status,Position," +
                    "Immediate Supervisor,Basic Salary,Rice Subsidy,Phone Allowance," +
                    "Clothing Allowance,Gross Semi-monthly Rate,Hourly Rate";

    // ATTENDANCE CSV COLUMN INDICES
    public static final int ATT_EMP_NUM = 0;
    public static final int ATT_DATE    = 3;
    public static final int ATT_LOGIN   = 4;
    public static final int ATT_LOGOUT  = 5;

    // PAYROLL CUTOFF PERIODS  (June - December 2024)
    public static final String[] CUTOFF_START = {
            "06/01/2024", "06/16/2024",
            "07/01/2024", "07/16/2024",
            "08/01/2024", "08/16/2024",
            "09/01/2024", "09/16/2024",
            "10/01/2024", "10/16/2024",
            "11/01/2024", "11/16/2024",
            "12/01/2024", "12/16/2024"
    };

    public static final String[] CUTOFF_END = {
            "06/15/2024", "06/30/2024",
            "07/15/2024", "07/31/2024",
            "08/15/2024", "08/31/2024",
            "09/15/2024", "09/30/2024",
            "10/15/2024", "10/31/2024",
            "11/15/2024", "11/30/2024",
            "12/15/2024", "12/31/2024"
    };

    public static final String[] MONTH_NAMES = {
            "JUNE", "JULY", "AUGUST", "SEPTEMBER",
            "OCTOBER", "NOVEMBER", "DECEMBER"
    };

    // GUI COLOUR PALETTE
    public static final Color CLR_BG          = new Color(254, 255, 255);
    public static final Color CLR_SURFACE      = new Color(222, 242, 241);
    public static final Color CLR_SURFACE2     = new Color(254, 255, 255);
    public static final Color CLR_ACCENT       = new Color(43, 122, 120);
    public static final Color CLR_ACCENT_DARK  = new Color(58, 175, 169);
    public static final Color CLR_TEXT         = new Color(23, 37, 42);
    public static final Color CLR_MUTED        = new Color(43, 122, 120);
    public static final Color CLR_DANGER       = new Color(0xE05C5C);
    public static final Color CLR_SUCCESS      = new Color(0x4CAF7D);
    public static final Color CLR_BORDER       = new Color(43, 122, 120);

    // GUI FONTS
    public static final Font FONT_TITLE  = new Font("Helvetica",    Font.BOLD,  22);
    public static final Font FONT_LOGO   = new Font("Helvetica",    Font.BOLD,  28);
    public static final Font FONT_CARD_H = new Font("Helvetica",    Font.BOLD,  20);
    public static final Font FONT_LABEL  = new Font("Arial",   Font.BOLD,  12);
    public static final Font FONT_INPUT  = new Font("Arial",   Font.PLAIN, 13);
    public static final Font FONT_BUTTON = new Font("Arial",   Font.BOLD,  13);
    public static final Font FONT_SMALL  = new Font("Arial",   Font.PLAIN, 11);
    public static final Font FONT_MONO   = new Font("Courier New", Font.PLAIN, 12);

    // Card name for the new Admin/HR panel
    public static final String CARD_ADMIN = "admin";
}
