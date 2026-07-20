package motorphemployeeapp;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

/**
 * UIComponents
 *
 * A set of methods that build ready-to-use,
 * consistently styled Swing components (labels, fields, buttons, cards,
 * etc.) using the colors and fonts from AppConstants.
 */
public class UIComponents {

    private UIComponents() {}

    // LABEL
    // -------------------------------------------------------------------------
    // Creates a muted, bold field label.
    public static JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setFont(AppConstants.FONT_LABEL);
        l.setForeground(AppConstants.CLR_MUTED);
        return l;
    }

    // INPUT FIELDS
    // -------------------------------------------------------------------------
    // Creates a styled single-line text input field.
    public static JTextField inputField() {
        JTextField f = new JTextField();
        f.setFont(AppConstants.FONT_INPUT);
        f.setBackground(AppConstants.CLR_SURFACE2);
        f.setForeground(AppConstants.CLR_TEXT);
        f.setCaretColor(AppConstants.CLR_ACCENT);
        f.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(AppConstants.CLR_BORDER, 1, true),
                new EmptyBorder(6, 10, 6, 10)));
        f.setPreferredSize(new Dimension(0, 36));
        return f;
    }

    // Creates a styled password input field.
    public static JPasswordField passwordField() {
        JPasswordField f = new JPasswordField();
        f.setFont(AppConstants.FONT_INPUT);
        f.setBackground(AppConstants.CLR_SURFACE2);
        f.setForeground(AppConstants.CLR_TEXT);
        f.setCaretColor(AppConstants.CLR_ACCENT);
        f.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(AppConstants.CLR_BORDER, 1, true),
                new EmptyBorder(6, 10, 6, 10)));
        f.setPreferredSize(new Dimension(0, 36));
        return f;
    }

    // BUTTONS
    // -------------------------------------------------------------------------
    // Creates an action button with a rounded
    // custom-painted background that responds to hover and press states.
    public static JButton primaryBtn(String text) {
        JButton b = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();

                g2.setRenderingHint(
                        RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);

                // Background color states
                Color bg = getModel().isPressed()
                        ? AppConstants.CLR_ACCENT_DARK
                        : getModel().isRollover()
                        ? AppConstants.CLR_ACCENT.brighter()
                        : AppConstants.CLR_ACCENT;

                // Fill rounded background
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);

                // Border on all sides
                g2.setColor(AppConstants.CLR_BORDER);
                g2.drawRoundRect(
                        0, 0,
                        getWidth() - 1,
                        getHeight() - 1,
                        8, 8);

                g2.dispose();

                super.paintComponent(g);
            }
        };

        b.setFont(AppConstants.FONT_BUTTON);
        b.setForeground(AppConstants.CLR_BG);

        b.setOpaque(false);
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);

        // Center text
        b.setHorizontalAlignment(SwingConstants.CENTER);
        b.setVerticalAlignment(SwingConstants.CENTER);

        // Padding so text stays visually centered
        b.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setPreferredSize(new Dimension(160, 38));

        return b;
    }

    // Creates a ghost (outline-only) secondary button.
    public static JButton ghostBtn(String text) {
        JButton b = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();

                g2.setRenderingHint(
                        RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);

                // Background states
                Color bg = getModel().isPressed()
                        ? new Color(43, 122, 120)
                        : getModel().isRollover()
                        ? new Color(58, 175, 169)
                        : AppConstants.CLR_SURFACE;

                // Fill background
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);

                // Border on all sides
                g2.setColor(AppConstants.CLR_BORDER);
                g2.drawRoundRect(
                        0, 0,
                        getWidth() - 1,
                        getHeight() - 1,
                        8, 8);

                g2.dispose();

                super.paintComponent(g);
            }
        };

        b.setFont(AppConstants.FONT_BUTTON);
        b.setForeground(AppConstants.CLR_TEXT);

        b.setOpaque(false);
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);

        // Center text
        b.setHorizontalAlignment(SwingConstants.CENTER);
        b.setVerticalAlignment(SwingConstants.CENTER);

        // Padding
        b.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setPreferredSize(new Dimension(140, 38));

        return b;
    }

    // CONTAINERS
    // -------------------------------------------------------------------------
    // Creates a card-style panel with a surface background color,
    // a subtle border, and internal padding.
    public static JPanel card(LayoutManager layout) {
        JPanel p = new JPanel(layout);
        p.setBackground(AppConstants.CLR_SURFACE);
        p.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(AppConstants.CLR_BORDER, 1, true),
                new EmptyBorder(20, 24, 20, 24)));
        return p;
    }

    // Builds a section header panel containing a title, a subtitle line,
    // and a short amber accent bar underneath the title.
    public static JPanel sectionHeader(String title, String subtitle) {
        JPanel p = new JPanel(new BorderLayout(0, 2));
        p.setBackground(AppConstants.CLR_BG);
        p.setBorder(new EmptyBorder(0, 0, 16, 0));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(AppConstants.FONT_TITLE);
        titleLbl.setForeground(AppConstants.CLR_TEXT);

        JLabel subLbl = new JLabel(subtitle);
        subLbl.setFont(AppConstants.FONT_SMALL);
        subLbl.setForeground(AppConstants.CLR_MUTED);

        // Short amber underline
        JPanel accentLine = new JPanel();
        accentLine.setBackground(AppConstants.CLR_ACCENT);
        accentLine.setPreferredSize(new Dimension(40, 3));

        JPanel topRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        topRow.setBackground(AppConstants.CLR_BG);
        topRow.add(titleLbl);

        p.add(topRow,     BorderLayout.NORTH);
        p.add(subLbl,     BorderLayout.CENTER);
        p.add(accentLine, BorderLayout.SOUTH);
        return p;
    }

    // OUTPUT AREA
    // -------------------------------------------------------------------------
    // Creates a non-editable monospaced text area suitable for
    // displaying formatted payroll reports.
    public static JTextArea resultArea() {
        JTextArea a = new JTextArea();
        a.setFont(AppConstants.FONT_MONO);
        a.setBackground(AppConstants.CLR_BG);
        a.setForeground(AppConstants.CLR_TEXT);
        a.setCaretColor(AppConstants.CLR_ACCENT);
        a.setEditable(false);
        a.setLineWrap(true);
        a.setWrapStyleWord(true);
        a.setBorder(new EmptyBorder(10, 12, 10, 12));
        return a;
    }

    // Wraps a component in a themed JScrollPane.
    public static JScrollPane scrollPane(JComponent content) {
        JScrollPane sp = new JScrollPane(content);
        sp.setBackground(AppConstants.CLR_BG);
        sp.getViewport().setBackground(AppConstants.CLR_BG);
        sp.setBorder(new LineBorder(AppConstants.CLR_BORDER, 1, true));
        sp.getVerticalScrollBar().setUnitIncrement(12);
        return sp;
    }

    // DIALOG HELPERS
    // -------------------------------------------------------------------------
    // Shows a modal error dialog.
    public static void showError(Component parent, String message) {
        JOptionPane.showMessageDialog(
                parent, message, "Input Error", JOptionPane.ERROR_MESSAGE);
    }

    // Shows a modal informational dialog.
    public static void showInfo(Component parent, String title, String message) {
        JOptionPane.showMessageDialog(
                parent, message, title, JOptionPane.INFORMATION_MESSAGE);
    }
}
