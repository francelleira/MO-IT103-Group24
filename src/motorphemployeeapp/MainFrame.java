package motorphemployeeapp;

import javax.swing.*;
import java.awt.*;

/**
 * MainFrame
 *
 * Creates the application's single JFrame, sets up the CardLayout
 * container, and wires together the three screen panels produced by
 * LoginPanel, EmployeePanel, and PayrollPanel.
 */
public class MainFrame {

    // ─── Prevent instantiation ────────────────────────────────────────────────
    private MainFrame() {}

    // ─── Named keys for the CardLayout ───────────────────────────────────────
    public static final String CARD_LOGIN    = "login";
    public static final String CARD_EMPLOYEE = "employee";
    public static final String CARD_PAYROLL  = "payroll";

    // FRAME BUILDER
    /**
     * Constructs the JFrame, populates it with all screen panels,
     * and makes it visible.
     */
    public static void build() {
        JFrame frame = new JFrame("MotorPH Payroll Portal");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(820, 640);
        frame.setMinimumSize(new Dimension(760, 580));
        frame.setLocationRelativeTo(null); // centre on screen
        frame.getContentPane().setBackground(AppConstants.CLR_BG);

        // Shared CardLayout and its container
        CardLayout cardLayout = new CardLayout();
        JPanel     cardPanel  = new JPanel(cardLayout);
        cardPanel.setBackground(AppConstants.CLR_BG);

        // Build each screen and add it to the card container
        cardPanel.add(LoginPanel.build(cardLayout, cardPanel), CARD_LOGIN);
        cardPanel.add(EmployeePanel.build(),                   CARD_EMPLOYEE);
        cardPanel.add(PayrollPanel.build(),                    CARD_PAYROLL);

        frame.add(cardPanel);

        // Start on the login screen
        cardLayout.show(cardPanel, CARD_LOGIN);

        frame.setVisible(true);
    }
}
