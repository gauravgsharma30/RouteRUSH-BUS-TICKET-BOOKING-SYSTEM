
package auth;

import dashboard.AdminDashboard;
import dashboard.Dashboard;
import db.DBConnection;

import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class Login {

    private final Color navy = new Color(9, 35, 70);
    private final Color blue = new Color(37, 99, 235);
    private final Color muted = new Color(100, 116, 139);

    public Login() {

        JFrame frame = new JFrame("Bus Booking System | Login");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1050, 680);
        frame.setMinimumSize(new Dimension(850, 600));
        frame.setLocationRelativeTo(null);
        frame.setLayout(new GridLayout(1, 2));

        // LEFT: Travel image panel
        JPanel hero = new JPanel() {
            Image image = new ImageIcon("Images/bus-banner.jpg").getImage();

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);

                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(
                        RenderingHints.KEY_INTERPOLATION,
                        RenderingHints.VALUE_INTERPOLATION_BILINEAR);

                if (image.getWidth(this) > 0) {
                    g2.drawImage(image, 0, 0,
                            getWidth(), getHeight(), this);
                } else {
                    g2.setPaint(new GradientPaint(
                            0, 0, navy,
                            getWidth(), getHeight(),
                            new Color(37, 99, 180)));
                    g2.fillRect(0, 0, getWidth(), getHeight());
                }

                g2.setColor(new Color(5, 28, 60, 155));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };

        hero.setLayout(new BoxLayout(hero, BoxLayout.Y_AXIS));
        hero.setBorder(new EmptyBorder(65, 38, 40, 30));

        JLabel brand = new JLabel("BUS BOOKING SYSTEM");
        brand.setFont(new Font("Arial", Font.BOLD, 17));
        brand.setForeground(Color.WHITE);
        brand.setAlignmentX(Component.LEFT_ALIGNMENT);

        hero.add(brand);
        hero.add(Box.createVerticalGlue());

        JLabel heading = new JLabel(
                "<html>Your journey,<br>our priority.</html>");
        heading.setFont(new Font("Arial", Font.BOLD, 38));
        heading.setForeground(Color.WHITE);
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel description = new JLabel(
                "<html>Discover routes, book your seats,<br>"
                        + "and travel with comfort and confidence.</html>");
        description.setFont(new Font("Arial", Font.PLAIN, 16));
        description.setForeground(new Color(235, 242, 255));
        description.setAlignmentX(Component.LEFT_ALIGNMENT);

        hero.add(heading);
        hero.add(Box.createVerticalStrut(18));
        hero.add(description);
        hero.add(Box.createVerticalGlue());

        JLabel footer = new JLabel("Travel far. Explore more.");
        footer.setFont(new Font("Arial", Font.ITALIC, 15));
        footer.setForeground(Color.WHITE);
        footer.setAlignmentX(Component.LEFT_ALIGNMENT);
        hero.add(footer);

        // RIGHT: Login form
        JPanel right = new JPanel(new GridBagLayout());
        right.setBackground(new Color(248, 250, 254));

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBackground(new Color(248, 250, 254));
        form.setBorder(new EmptyBorder(25, 35, 25, 35));

        JLabel title = new JLabel("Welcome back!");
        title.setFont(new Font("Arial", Font.BOLD, 30));
        title.setForeground(navy);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitle = new JLabel("Sign in to continue your journey");
        subtitle.setFont(new Font("Arial", Font.PLAIN, 14));
        subtitle.setForeground(muted);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextField username = new JTextField();
        JPasswordField password = new JPasswordField();

        styleField(username);
        styleField(password);

        JComboBox<String> roleBox = new JComboBox<>(new String[] { "user", "admin" });
        roleBox.setFont(new Font("Arial", Font.PLAIN, 14));
        roleBox.setBackground(Color.WHITE);
        roleBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 43));
        roleBox.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel userLabel = createLabel("Username");
        JLabel passLabel = createLabel("Password");
        JLabel roleLabel = createLabel("Login as");

        JCheckBox showPassword = new JCheckBox("Show password");
        showPassword.setFont(new Font("Arial", Font.PLAIN, 12));
        showPassword.setForeground(muted);
        showPassword.setBackground(new Color(248, 250, 254));
        showPassword.setAlignmentX(Component.LEFT_ALIGNMENT);

        char defaultEcho = password.getEchoChar();

        showPassword.addActionListener(e -> password.setEchoChar(
                showPassword.isSelected() ? (char) 0 : defaultEcho));

        JButton login = new JButton("Sign In");
        styleButton(login, blue);

        JButton signup = new JButton("Create Account");
        styleButton(signup, navy);

        form.add(title);
        form.add(Box.createVerticalStrut(8));
        form.add(subtitle);
        form.add(Box.createVerticalStrut(30));

        form.add(userLabel);
        form.add(Box.createVerticalStrut(7));
        form.add(username);
        form.add(Box.createVerticalStrut(18));

        form.add(passLabel);
        form.add(Box.createVerticalStrut(7));
        form.add(password);
        form.add(Box.createVerticalStrut(5));
        form.add(showPassword);
        form.add(Box.createVerticalStrut(15));

        form.add(roleLabel);
        form.add(Box.createVerticalStrut(7));
        form.add(roleBox);
        form.add(Box.createVerticalStrut(24));

        form.add(login);
        form.add(Box.createVerticalStrut(12));
        form.add(signup);

        right.add(form);

        frame.add(hero);
        frame.add(right);

        // LOGIN: preserve existing authentication logic
        login.addActionListener(e -> {

            if (username.getText().trim().isEmpty()
                    || password.getPassword().length == 0) {
                JOptionPane.showMessageDialog(
                        frame, "Please enter username and password.");
                return;
            }

            try (Connection con = DBConnection.getConnection()) {

                if (con == null) {
                    JOptionPane.showMessageDialog(
                            frame, "Database connection failed.");
                    return;
                }

                PreparedStatement ps = con.prepareStatement(
                        "SELECT * FROM users "
                                + "WHERE username=? AND password=? AND role=?");

                ps.setString(1, username.getText().trim());
                ps.setString(2, new String(password.getPassword()));
                ps.setString(3, roleBox.getSelectedItem().toString());

                ResultSet rs = ps.executeQuery();

                if (rs.next()) {

                    String name = username.getText().trim();

                    if (roleBox.getSelectedItem()
                            .toString().equals("admin")) {
                        new AdminDashboard(name);
                    } else {
                        new Dashboard(name);
                    }

                    frame.dispose();

                } else {
                    JOptionPane.showMessageDialog(
                            frame,
                            "Invalid username, password, or role.",
                            "Login Failed",
                            JOptionPane.WARNING_MESSAGE);
                }

                rs.close();
                ps.close();

            } catch (Exception ex) {
                ex.printStackTrace();

                JOptionPane.showMessageDialog(
                        frame,
                        "Login error: " + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        // SIGNUP
        signup.addActionListener(e -> {
            frame.dispose();
            new Signup();
        });

        frame.setVisible(true);
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.BOLD, 13));
        label.setForeground(navy);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private void styleField(JTextField field) {
        field.setFont(new Font("Arial", Font.PLAIN, 15));
        field.setPreferredSize(new Dimension(320, 43));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 43));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(
                        new Color(215, 225, 238)),
                new EmptyBorder(8, 12, 8, 12)));
        field.setBackground(Color.WHITE);
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
    }

    private void styleButton(JButton button, Color color) {
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setForeground(Color.WHITE);
        button.setBackground(color);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        button.setPreferredSize(new Dimension(320, 45));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(
                Cursor.HAND_CURSOR));
        button.setAlignmentX(Component.LEFT_ALIGNMENT);

        Color original = color;

        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                button.setBackground(original.brighter());
            }

            public void mouseExited(MouseEvent e) {
                button.setBackground(original);
            }
        });
    }
}
