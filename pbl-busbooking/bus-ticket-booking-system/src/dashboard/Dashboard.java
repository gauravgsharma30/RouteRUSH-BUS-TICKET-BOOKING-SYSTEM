
package dashboard;

import auth.Login;
import bus.CancelTicket;
import bus.MyBookings;
import bus.SearchBusFinal;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class Dashboard {

    private final Color navy = new Color(9, 35, 70);
    private final Color blue = new Color(37, 99, 235);
    private final Color textColor = new Color(18, 43, 82);

    public Dashboard(String username) {

        JFrame frame = new JFrame("Bus Booking System");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(1250, 780);
        frame.setMinimumSize(new Dimension(950, 650));
        frame.setLocationRelativeTo(null);
        frame.setLayout(new BorderLayout());

        // Main content
        JPanel content = new JPanel(new BorderLayout(0, 18));
        content.setBackground(new Color(245, 248, 253));
        content.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        // Sidebar
        JPanel sidebar = new JPanel();
        sidebar.setPreferredSize(new Dimension(225, 0));
        sidebar.setBackground(navy);
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBorder(BorderFactory.createEmptyBorder(25, 12, 20, 12));

        JLabel brand = new JLabel("<html>BUS BOOKING<br>SYSTEM</html>");
        brand.setForeground(Color.WHITE);
        brand.setFont(new Font("Arial", Font.BOLD, 23));
        brand.setBorder(BorderFactory.createEmptyBorder(10, 15, 35, 0));
        brand.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(brand);

        addNav(sidebar, "⌂   Home", () -> {
        });
        addNav(sidebar, "⌕   Search Bus",
                () -> new SearchBusFinal(username));
        addNav(sidebar, "▤   My Bookings",
                () -> new MyBookings(username));
        addNav(sidebar, "✕   Cancel Ticket",
                () -> new CancelTicket(username));

        sidebar.add(Box.createVerticalGlue());

        addNav(sidebar, "⇥   Logout", () -> {
            frame.dispose();
            new Login();
        });

        // Top header
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);

        JLabel greeting = new JLabel("Welcome, " + username);
        greeting.setFont(new Font("Arial", Font.BOLD, 22));
        greeting.setForeground(textColor);

        JLabel subGreeting = new JLabel("  Your next journey starts here");
        subGreeting.setFont(new Font("Arial", Font.PLAIN, 14));
        subGreeting.setForeground(new Color(100, 116, 139));

        JPanel greetingPanel = new JPanel(new GridLayout(2, 1, 0, 5));
        greetingPanel.setOpaque(false);
        greetingPanel.add(greeting);
        greetingPanel.add(subGreeting);

        top.add(greetingPanel, BorderLayout.WEST);

        JLabel badge = new JLabel("  ●  TRAVEL WITH COMFORT  ");
        badge.setForeground(new Color(29, 78, 216));
        badge.setFont(new Font("Arial", Font.BOLD, 12));
        top.add(badge, BorderLayout.EAST);

        // Hero banner
        JPanel hero = new JPanel() {
            Image image = new ImageIcon("Images/bus-banner.jpg").getImage();

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();

                g2.setRenderingHint(
                        RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);

                if (image.getWidth(this) > 0) {
                    g2.drawImage(image, 0, 0,
                            getWidth(), getHeight(), this);
                } else {
                    g2.setPaint(new GradientPaint(
                            0, 0, new Color(15, 65, 125),
                            getWidth(), getHeight(),
                            new Color(75, 155, 220)));
                    g2.fillRect(0, 0, getWidth(), getHeight());
                }

                // Dark translucent overlay for readable text
                g2.setColor(new Color(4, 30, 65, 125));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 24, 24);
                g2.dispose();
            }
        };

        hero.setLayout(new BoxLayout(hero, BoxLayout.Y_AXIS));
        hero.setBorder(BorderFactory.createEmptyBorder(35, 35, 25, 20));
        hero.setPreferredSize(new Dimension(700, 220));
        hero.setOpaque(false);

        JLabel heroTitle = new JLabel("Explore. Travel. Discover.");
        heroTitle.setForeground(Color.WHITE);
        heroTitle.setFont(new Font("Arial", Font.BOLD, 30));

        JLabel heroText = new JLabel(
                "<html>Book your next bus journey with comfort,<br>"
                        + "safety and ease.</html>");
        heroText.setForeground(Color.WHITE);
        heroText.setFont(new Font("Arial", Font.PLAIN, 17));

        hero.add(heroTitle);
        hero.add(Box.createVerticalStrut(12));
        hero.add(heroText);

        // Interactive feature cards
        JPanel cards = new JPanel(new GridLayout(1, 4, 15, 0));
        cards.setOpaque(false);

        cards.add(createCard("⌕", "Search Bus",
                "Find buses for your journey.",
                new Color(232, 242, 255), blue,
                () -> new SearchBusFinal(username)));

        cards.add(createCard("▤", "My Bookings",
                "View your current bookings.",
                new Color(231, 249, 239), new Color(22, 163, 74),
                () -> new MyBookings(username)));

        cards.add(createCard("✕", "Cancel Ticket",
                "Manage ticket cancellations.",
                new Color(255, 237, 239), new Color(225, 55, 75),
                () -> new CancelTicket(username)));

        cards.add(createCard("⇥", "Logout",
                "Securely end your session.",
                new Color(241, 237, 255), new Color(109, 70, 210),
                () -> {
                    frame.dispose();
                    new Login();
                }));

        // Bottom information panel
        JPanel info = new JPanel(new GridLayout(1, 4, 10, 0));
        info.setBackground(Color.WHITE);
        info.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(
                        new Color(222, 232, 245)),
                BorderFactory.createEmptyBorder(18, 12, 18, 12)));

        info.add(infoItem("✓", "Safe & Secure", "Travel with confidence"));
        info.add(infoItem("₹", "Best Prices", "Affordable fares"));
        info.add(infoItem("↗", "Wide Network", "Explore more routes"));
        info.add(infoItem("☎", "Travel Support", "Here to help"));

        // Arrange main content
        JPanel center = new JPanel(new BorderLayout(0, 18));
        center.setOpaque(false);
        center.add(hero, BorderLayout.NORTH);
        center.add(cards, BorderLayout.CENTER);
        center.add(info, BorderLayout.SOUTH);

        content.add(top, BorderLayout.NORTH);
        content.add(center, BorderLayout.CENTER);

        frame.add(sidebar, BorderLayout.WEST);
        frame.add(content, BorderLayout.CENTER);
        frame.setVisible(true);
    }

    private void addNav(JPanel sidebar, String name, Runnable action) {
        JLabel item = new JLabel(name);
        item.setForeground(Color.WHITE);
        item.setFont(new Font("Arial", Font.BOLD, 15));
        item.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 8));
        item.setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));
        item.setAlignmentX(Component.LEFT_ALIGNMENT);
        item.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        item.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                item.setOpaque(true);
                item.setBackground(new Color(35, 85, 145));
                item.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                new Color(55, 110, 180)),
                        BorderFactory.createEmptyBorder(13, 13, 13, 7)));
                item.repaint();
            }

            public void mouseExited(MouseEvent e) {
                item.setOpaque(false);
                item.setBorder(
                        BorderFactory.createEmptyBorder(14, 14, 14, 8));
                item.repaint();
            }

            public void mouseClicked(MouseEvent e) {
                action.run();
            }
        });

        sidebar.add(item);
        sidebar.add(Box.createVerticalStrut(8));
    }

    private JPanel createCard(String icon, String title, String description,
            Color background, Color accent, Runnable action) {

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(background);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(
                        new Color(accent.getRed(), accent.getGreen(),
                                accent.getBlue(), 70)),
                BorderFactory.createEmptyBorder(20, 16, 18, 12)));

        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(new Font("Arial", Font.BOLD, 34));
        iconLabel.setForeground(accent);
        iconLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 17));
        titleLabel.setForeground(textColor);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel descLabel = new JLabel(
                "<html><div style='width:130px'>"
                        + description + "</div></html>");
        descLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        descLabel.setForeground(new Color(75, 95, 125));
        descLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel actionLabel = new JLabel("Explore  →");
        actionLabel.setFont(new Font("Arial", Font.BOLD, 13));
        actionLabel.setForeground(accent);
        actionLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(iconLabel);
        card.add(Box.createVerticalStrut(20));
        card.add(titleLabel);
        card.add(Box.createVerticalStrut(10));
        card.add(descLabel);
        card.add(Box.createVerticalGlue());
        card.add(actionLabel);

        MouseAdapter click = new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                action.run();
            }

            public void mouseEntered(MouseEvent e) {
                card.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(accent, 2),
                        BorderFactory.createEmptyBorder(19, 15, 17, 11)));
                card.setBackground(background.brighter());
            }

            public void mouseExited(MouseEvent e) {
                card.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                new Color(accent.getRed(), accent.getGreen(),
                                        accent.getBlue(), 70)),
                        BorderFactory.createEmptyBorder(20, 16, 18, 12)));
                card.setBackground(background);
            }
        };

        card.addMouseListener(click);
        iconLabel.addMouseListener(click);
        titleLabel.addMouseListener(click);
        descLabel.addMouseListener(click);
        actionLabel.addMouseListener(click);

        return card;
    }

    private JPanel infoItem(String icon, String title, String subtitle) {
        JPanel item = new JPanel(new GridLayout(3, 1, 0, 4));
        item.setOpaque(false);

        JLabel i = new JLabel(icon, SwingConstants.CENTER);
        i.setForeground(blue);
        i.setFont(new Font("Arial", Font.BOLD, 23));

        JLabel t = new JLabel(title, SwingConstants.CENTER);
        t.setForeground(textColor);
        t.setFont(new Font("Arial", Font.BOLD, 13));

        JLabel s = new JLabel(subtitle, SwingConstants.CENTER);
        s.setForeground(new Color(100, 116, 139));
        s.setFont(new Font("Arial", Font.PLAIN, 11));

        item.add(i);
        item.add(t);
        item.add(s);
        return item;
    }
}
