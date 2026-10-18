package bus;

import db.DBConnection;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;

public class MyBookings {

    private final Color NAVY = new Color(15, 29, 55);
    private final Color BLUE = new Color(42, 104, 210);
    private final Color LIGHT = new Color(244, 247, 252);
    private final Color TEXT = new Color(40, 52, 72);

    public MyBookings(String username) {

        JFrame f = new JFrame("BusGo | My Bookings");
        f.setExtendedState(JFrame.MAXIMIZED_BOTH);
        f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel bg = new JPanel(new GridBagLayout()) {
            Image img = new ImageIcon("Images/Bg.jpeg").getImage();

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);

                if (img != null && img.getWidth(this) > 0) {
                    g.drawImage(img, 0, 0, getWidth(), getHeight(), this);
                } else {
                    g.setColor(LIGHT);
                    g.fillRect(0, 0, getWidth(), getHeight());
                }
            }
        };

        JPanel main = new JPanel(new BorderLayout(0, 22));
        main.setBackground(Color.WHITE);
        main.setBorder(new EmptyBorder(30, 35, 30, 35));
        main.setPreferredSize(new Dimension(1100, 650));

        // HEADER
        JLabel title = new JLabel("My Bookings");
        title.setFont(new Font("Segoe UI", Font.BOLD, 30));
        title.setForeground(NAVY);

        JLabel subtitle = new JLabel(
                "Welcome, " + username + " — view your ticket booking history.");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        subtitle.setForeground(new Color(110, 122, 142));

        JPanel heading = new JPanel();
        heading.setLayout(new BoxLayout(heading, BoxLayout.Y_AXIS));
        heading.setBackground(Color.WHITE);
        heading.add(title);
        heading.add(Box.createVerticalStrut(8));
        heading.add(subtitle);

        // BOOKING COUNT CARD
        JLabel countValue = new JLabel("0");
        countValue.setFont(new Font("Segoe UI", Font.BOLD, 28));
        countValue.setForeground(BLUE);

        JLabel countLabel = new JLabel("TOTAL BOOKINGS");
        countLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        countLabel.setForeground(new Color(110, 122, 142));

        JPanel countCard = new JPanel();
        countCard.setLayout(new BoxLayout(countCard, BoxLayout.Y_AXIS));
        countCard.setBackground(LIGHT);
        countCard.setBorder(new EmptyBorder(15, 20, 15, 30));
        countCard.add(countLabel);
        countCard.add(Box.createVerticalStrut(5));
        countCard.add(countValue);

        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(Color.WHITE);
        top.add(heading, BorderLayout.WEST);
        top.add(countCard, BorderLayout.EAST);

        // TABLE
        String[] cols = {
                "Booking ID", "Bus ID", "Seat Number", "Booking Date"
        };

        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        JTable table = new JTable(model);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setForeground(TEXT);
        table.setRowHeight(42);
        table.setSelectionBackground(new Color(218, 232, 255));
        table.setSelectionForeground(NAVY);
        table.setShowVerticalLines(false);
        table.setGridColor(new Color(230, 235, 243));
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setAutoCreateRowSorter(true);

        JTableHeader tableHeader = table.getTableHeader();
        tableHeader.setFont(new Font("Segoe UI", Font.BOLD, 13));
        tableHeader.setBackground(NAVY);
        tableHeader.setForeground(Color.WHITE);
        tableHeader.setPreferredSize(new Dimension(0, 45));
        tableHeader.setReorderingAllowed(false);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(
                new Color(226, 232, 241)));
        scroll.getViewport().setBackground(Color.WHITE);

        // EMPTY STATE / TABLE AREA
        JLabel tableTitle = new JLabel("Booking History");
        tableTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        tableTitle.setForeground(NAVY);

        JLabel status = new JLabel("Loading bookings...");
        status.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        status.setForeground(new Color(110, 122, 142));

        JPanel tableHeading = new JPanel(new BorderLayout());
        tableHeading.setBackground(Color.WHITE);
        tableHeading.add(tableTitle, BorderLayout.WEST);
        tableHeading.add(status, BorderLayout.EAST);

        JPanel tablePanel = new JPanel(new BorderLayout(0, 12));
        tablePanel.setBackground(Color.WHITE);
        tablePanel.add(tableHeading, BorderLayout.NORTH);
        tablePanel.add(scroll, BorderLayout.CENTER);

        // FOOTER
        JButton refreshBtn = createButton("Refresh", BLUE);
        JButton closeBtn = createButton("Close", NAVY);

        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(Color.WHITE);

        JLabel hint = new JLabel("Your booking records are shown below.");
        hint.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        hint.setForeground(new Color(110, 122, 142));

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setBackground(Color.WHITE);
        actions.add(refreshBtn);
        actions.add(closeBtn);

        footer.add(hint, BorderLayout.WEST);
        footer.add(actions, BorderLayout.EAST);

        main.add(top, BorderLayout.NORTH);
        main.add(tablePanel, BorderLayout.CENTER);
        main.add(footer, BorderLayout.SOUTH);

        bg.add(main);
        f.setContentPane(bg);

        // LOAD BOOKINGS
        Runnable loadBookings = () -> {
            model.setRowCount(0);
            status.setText("Loading bookings...");

            String sql = "SELECT booking_id, bus_id, seat_number, booking_date "
                    + "FROM booking WHERE username = ? ORDER BY booking_date DESC";

            try (Connection con = DBConnection.getConnection();
                    PreparedStatement ps = con.prepareStatement(sql)) {

                ps.setString(1, username);

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        model.addRow(new Object[] {
                                rs.getInt("booking_id"),
                                rs.getInt("bus_id"),
                                rs.getInt("seat_number"),
                                rs.getTimestamp("booking_date")
                        });
                    }
                }

                countValue.setText(String.valueOf(model.getRowCount()));
                status.setText(model.getRowCount() + " booking(s) found");

                if (model.getRowCount() == 0) {
                    hint.setText("No bookings yet. Search for a bus to get started.");
                } else {
                    hint.setText("Select a column header to sort your bookings.");
                }

            } catch (Exception ex) {
                ex.printStackTrace();
                status.setText("Could not load bookings");

                JOptionPane.showMessageDialog(
                        f,
                        "Unable to load your bookings.\n"
                                + "Please check the database connection.",
                        "Database Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        };

        refreshBtn.addActionListener(e -> loadBookings.run());
        closeBtn.addActionListener(e -> f.dispose());

        loadBookings.run();

        f.setLocationRelativeTo(null);
        f.setVisible(true);
    }

    private JButton createButton(String text, Color base) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setForeground(Color.WHITE);
        button.setBackground(base);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(new EmptyBorder(12, 22, 12, 22));
        button.setOpaque(true);

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(base.brighter());
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(base);
            }
        });

        return button;
    }
}