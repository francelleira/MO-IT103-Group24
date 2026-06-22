package motorphemployeeapp;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

/**
 * LoginPanel
 *
 * Builds and owns the Login screen. When credentials are verified it
 * asks MainFrame to switch to the appropriate screen via the supplied
 * CardLayout and container reference.
 */
public class LoginPanel {

    private LoginPanel() {}

    private static JTextField     txtUsername;
    private static JPasswordField txtPassword;
    private static JLabel         lblError;

    private static CardLayout cardLayout;
    private static JPanel     cardPanel;

    public static JPanel build(CardLayout layout, JPanel cards) {
        cardLayout = layout;
        cardPanel  = cards;

        JPanel outer = new JPanel(new GridBagLayout());
        outer.setBackground(AppConstants.CLR_BG);

        JPanel brand = new JPanel(new FlowLayout(FlowLayout.CENTER));
        brand.setBackground(AppConstants.CLR_BG);

        JLabel logoLbl = new JLabel("MotorPH Payroll Portal");
        logoLbl.setFont(AppConstants.FONT_LOGO);
        logoLbl.setForeground(AppConstants.CLR_ACCENT);
        brand.add(logoLbl);

        JLabel tagline = new JLabel("Payroll Management System");
        tagline.setFont(AppConstants.FONT_SMALL);
        tagline.setForeground(AppConstants.CLR_MUTED);
        tagline.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel loginCard = UIComponents.card(new GridBagLayout());
        loginCard.setPreferredSize(new Dimension(380, 320));

        GridBagConstraints gc = new GridBagConstraints();
        gc.insets  = new Insets(6, 0, 6, 0);
        gc.fill    = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1;

        JLabel heading = new JLabel("Sign In");
        heading.setFont(AppConstants.FONT_CARD_H);
        heading.setForeground(AppConstants.CLR_TEXT);

        txtUsername = UIComponents.inputField();
        txtPassword = UIComponents.passwordField();

        lblError = new JLabel(" ");
        lblError.setFont(AppConstants.FONT_SMALL);
        lblError.setForeground(AppConstants.CLR_DANGER);
        lblError.setHorizontalAlignment(SwingConstants.CENTER);

        JButton btnLogin = UIComponents.primaryBtn("Sign In");
        btnLogin.setPreferredSize(new Dimension(320, 42));

        ActionListener loginAction = e -> handleLogin();
        btnLogin.addActionListener(loginAction);
        txtPassword.addActionListener(loginAction);

        gc.gridx = 0; gc.gridy = 0; loginCard.add(heading,                      gc);
        gc.gridy = 1;               loginCard.add(UIComponents.label("Username"), gc);
        gc.gridy = 2;               loginCard.add(txtUsername,                   gc);
        gc.gridy = 3;               loginCard.add(UIComponents.label("Password"), gc);
        gc.gridy = 4;               loginCard.add(txtPassword,                   gc);
        gc.gridy = 5;               loginCard.add(lblError,                      gc);
        gc.gridy = 6;
        gc.insets = new Insets(10, 0, 0, 0);
        loginCard.add(btnLogin, gc);

        JPanel centre = new JPanel();
        centre.setLayout(new BoxLayout(centre, BoxLayout.Y_AXIS));
        centre.setBackground(AppConstants.CLR_BG);

        brand.setAlignmentX(Component.CENTER_ALIGNMENT);
        tagline.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginCard.setAlignmentX(Component.CENTER_ALIGNMENT);

        centre.add(brand);
        centre.add(Box.createVerticalStrut(4));
        centre.add(tagline);
        centre.add(Box.createVerticalStrut(24));
        centre.add(loginCard);

        outer.add(centre);
        return outer;
    }

    private static void handleLogin() {
        String user = txtUsername.getText().trim();
        String pass = new String(txtPassword.getPassword()).trim();

        try {
            if (user.isEmpty() || pass.isEmpty()) {
                throw new IllegalArgumentException("Username and password cannot be empty.");
            }

            if (user.equals("employee") && pass.equals("12345")) {
                lblError.setText(" ");
                cardLayout.show(cardPanel, MainFrame.CARD_EMPLOYEE);

            } else if (user.equals("payroll_staff") && pass.equals("12345")) {
                lblError.setText(" ");
                cardLayout.show(cardPanel, MainFrame.CARD_PAYROLL);

            } else if (user.equals("admin") && pass.equals("12345")) {
                lblError.setText(" ");
                cardLayout.show(cardPanel, MainFrame.CARD_ADMIN);

            } else {
                throw new SecurityException("Incorrect username or password.");
            }

        } catch (IllegalArgumentException | SecurityException ex) {
            lblError.setText(ex.getMessage());
            txtPassword.setText("");
        }
    }

    public static void signOut() {
        if (txtUsername != null) txtUsername.setText("");
        if (txtPassword != null) txtPassword.setText("");
        if (lblError    != null) lblError.setText(" ");
        if (cardLayout  != null) cardLayout.show(cardPanel, MainFrame.CARD_LOGIN);
    }
}
