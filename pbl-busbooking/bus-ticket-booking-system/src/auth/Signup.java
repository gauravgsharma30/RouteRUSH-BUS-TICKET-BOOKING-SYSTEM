
package auth;

import db.DBConnection;

import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class Signup {

    private final Color navy = new Color(9, 35, 70);
    private final Color blue = new Color(37, 99, 235);
    private final Color muted = new Color(100, 116, 139);

    public Signup() {

        JFrame frame = new JFrame("Bus Booking System | Sign Up");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1050, 680);
        frame.setMinimumSize(new Dimension(850, 600));
        frame.setLocationRelativeTo(null);
        frame.setLayout(new GridLayout(1, 2));

        // LEFT: Scenic travel panel
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
                "<html>Your journey<br>starts with us.</html>");
        heading.setFont(new Font("Arial", Font.BOLD, 36));
        heading.setForeground(Color.WHITE);
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel description = new JLabel(
                "<html>Create your account to explore routes,<br>"
                        + "manage bookings, and travel with ease.</html>");
        description.setFont(new Font("Arial", Font.PLAIN, 15));
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

        // RIGHT: Signup form
        JPanel right = new JPanel(new GridBagLayout());
        right.setBackground(new Color(248, 250, 254));

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBackground(new Color(248, 250, 254));
        form.setBorder(new EmptyBorder(25, 35, 25, 35));

        JLabel title = new JLabel("Create your account");
        title.setFont(new Font("Arial", Font.BOLD, 27));
        title.setForeground(navy);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitle = new JLabel("Join us and plan your next trip");
        subtitle.setFont(new Font("Arial", Font.PLAIN, 14));
        subtitle.setForeground(muted);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextField username = new JTextField();
        JPasswordField password = new JPasswordField();

        styleField(username);
        styleField(password);

        JComboBox<String> role = new JComboBox<>(new String[] { "user", "admin" });
        role.setFont(new Font("Arial", Font.PLAIN, 14));
        role.setBackground(Color.WHITE);
        role.setMaximumSize(new Dimension(Integer.MAX_VALUE, 43));
        role.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel usernameLabel = createLabel("Username");
        JLabel passwordLabel = createLabel("Password");
        JLabel roleLabel = createLabel("Account type");

        JCheckBox showPassword = new JCheckBox("Show password");
        showPassword.setFont(new Font("Arial", Font.PLAIN, 12));
        showPassword.setForeground(muted);
        showPassword.setBackground(new Color(248, 250, 254));
        showPassword.setAlignmentX(Component.LEFT_ALIGNMENT);

        char defaultEcho = password.getEchoChar();
        showPassword.addActionListener(e -> password.setEchoChar(
                showPassword.isSelected() ? (char) 0 : defaultEcho));

        JButton register = new JButton("Create Account");
        styleButton(register, blue);

        JButton backToLogin = new JButton("Back to Login");
        styleButton(backToLogin, navy);

        form.add(title);
        form.add(Box.createVerticalStrut(8));
        form.add(subtitle);
        form.add(Box.createVerticalStrut(25));

        form.add(usernameLabel);
        form.add(Box.createVerticalStrut(7));
        form.add(username);
        form.add(Box.createVerticalStrut(16));

        form.add(passwordLabel);
        form.add(Box.createVerticalStrut(7));
        form.add(password);
        form.add(Box.createVerticalStrut(4));
        form.add(showPassword);
        form.add(Box.createVerticalStrut(12));

        form.add(roleLabel);
        form.add(Box.createVerticalStrut(7));
        form.add(role);
        form.add(Box.createVerticalStrut(22));

        form.add(register);
        form.add(Box.createVerticalStrut(12));
        form.add(backToLogin);

        right.add(form);

        frame.add(hero);
        frame.add(right);

        // REGISTRATION: preserve existing database operation
        register.addActionListener(e -> {

            String name = username.getText().trim();
            String pwd = new String(password.getPassword());

            if (name.isEmpty() || pwd.isEmpty()) {
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
                        "INSERT INTO users(username,password,role) "
                                + "VALUES(?,?,?)");

                ps.setString(1, name);
                ps.setString(2, pwd);
                ps.setString(3, role.getSelectedItem().toString());

                ps.executeUpdate();
                ps.close();

                JOptionPane.showMessageDialog(
                        frame, "Registration successful!");

                frame.dispose();
                new Login();

            } catch (SQLIntegrityConstraintViolationException ex) {
                JOptionPane.showMessageDialog(
                        frame,
                        "This username already exists. Please choose another.",
                        "Registration Failed",
                        JOptionPane.WARNING_MESSAGE);

            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(
                        frame,
                        "Registration error: " + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        // Return to login
        backToLogin.addActionListener(e -> {
            frame.dispose();
            new Login();
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

        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                button.setBackground(color.brighter());
            }

            public void mouseExited(MouseEvent e) {
                button.setBackground(color);
            }
        });
    }
}
