package motorphemployeeapp;

import java.awt.*;

/**
 * AppConstants
 *
 * Central repository for every constant used across the application.
 * Grouping constants here means changing a value in one place
 * automatically propagates everywhere it is referenced.
 *
 * Responsibilities:
 *   - CSV file paths
 *   - CSV column indices (employee & attendance)
 *   - Payroll cutoff date arrays
 *   - GUI color palette
 *   - GUI fonts
 */
public class AppConstants {

    // ─── Prevent instantiation ────────────────────────────────────────────────
    private AppConstants() {}

    // FILE PATHS
    public static final String EMPLOYEE_FILE   = "MotorPH_Employee_Details.csv";
    public static final String ATTENDANCE_FILE = "Attendance_Record.csv";

    // EMPLOYEE CSV COLUMN INDICES
    public static final int COL_EMP_NUM     = 0;
    public static final int COL_LAST_NAME   = 1;
    public static final int COL_FIRST_NAME  = 2;
    public static final int COL_BIRTHDAY    = 3;
    public static final int COL_HOURLY_RATE = 18;

    // ATTENDANCE CSV COLUMN INDICES
    public static final int ATT_EMP_NUM = 0;
    public static final int ATT_DATE    = 3;
    public static final int ATT_LOGIN   = 4;
    public static final int ATT_LOGOUT  = 5;

    // PAYROLL CUTOFF PERIODS  (June – December 2024)
    // Each month has two cutoff periods: 1st–15th and 16th–end-of-month.
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

    /** Human-readable month names parallel to CUTOFF_START/END pairs. */
    public static final String[] MONTH_NAMES = {
        "June", "July", "August", "September",
        "October", "November", "December"
    };

    // GUI COLOUR PALETTE  (deep navy + amber accent)
    public static final Color CLR_BG          = new Color(0x0D1B2A); // deep navy background
    public static final Color CLR_SURFACE      = new Color(0x1A2D42); // card / panel surface
    public static final Color CLR_SURFACE2     = new Color(0x233347); // input field background
    public static final Color CLR_ACCENT       = new Color(0xF4A623); // amber accent
    public static final Color CLR_ACCENT_DARK  = new Color(0xC6841A); // pressed amber
    public static final Color CLR_TEXT         = new Color(0xEBF0F7); // near-white text
    public static final Color CLR_MUTED        = new Color(0x7A92A8); // secondary / label text
    public static final Color CLR_DANGER       = new Color(0xE05C5C); // validation error red
    public static final Color CLR_SUCCESS      = new Color(0x4CAF7D); // success green
    public static final Color CLR_BORDER       = new Color(0x2E4560); // panel / input border

    // GUI FONTS
    public static final Font FONT_TITLE  = new Font("Georgia",    Font.BOLD,  22);
    public static final Font FONT_LOGO   = new Font("Georgia",    Font.BOLD,  28);
    public static final Font FONT_CARD_H = new Font("Georgia",    Font.BOLD,  20);
    public static final Font FONT_LABEL  = new Font("Segoe UI",   Font.BOLD,  12);
    public static final Font FONT_INPUT  = new Font("Segoe UI",   Font.PLAIN, 13);
    public static final Font FONT_BUTTON = new Font("Segoe UI",   Font.BOLD,  13);
    public static final Font FONT_SMALL  = new Font("Segoe UI",   Font.PLAIN, 11);
    public static final Font FONT_MONO   = new Font("Courier New", Font.PLAIN, 12);
}
