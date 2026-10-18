
package bus;

import db.DBConnection;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;

public class SearchBusFinal {

    private final Color NAVY = new Color(15, 29, 55);
    private final Color BLUE = new Color(42, 104, 210);
    private final Color LIGHT = new Color(244, 247, 252);
    private final Color TEXT = new Color(40, 52, 72);

    public SearchBusFinal(String username) {

        JFrame f = new JFrame("BusGo | Search Buses");
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
        main.setPreferredSize(new Dimension(1150, 690));

        // HEADER
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);

        JLabel title = new JLabel("Find Your Next Journey");
        title.setFont(new Font("Segoe UI", Font.BOLD, 30));
        title.setForeground(NAVY);

        JLabel subtitle = new JLabel(
                "Search routes, compare fares and book your seat.");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        subtitle.setForeground(new Color(110, 122, 142));

        JPanel headingBox = new JPanel();
        headingBox.setLayout(new BoxLayout(headingBox, BoxLayout.Y_AXIS));
        headingBox.setBackground(Color.WHITE);
        headingBox.add(title);
        headingBox.add(Box.createVerticalStrut(7));
        headingBox.add(subtitle);

        header.add(headingBox, BorderLayout.WEST);

        // SEARCH BAR
        JPanel searchPanel = new JPanel(new GridBagLayout());
        searchPanel.setBackground(LIGHT);
        searchPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JTextField sourceField = createField("Enter source city");
        JTextField destField = createField("Enter destination city");

        JButton searchBtn = createButton("Search Buses", BLUE);
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 8, 5, 8);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;

        c.gridx = 0;
        c.gridy = 0;
        searchPanel.add(createLabel("FROM"), c);

        c.gridx = 1;
        searchPanel.add(createLabel("TO"), c);

        c.gridx = 0;
        c.gridy = 1;
        searchPanel.add(sourceField, c);

        c.gridx = 1;
        searchPanel.add(destField, c);

        c.gridx = 2;
        c.gridy = 1;
        c.weightx = 0;
        searchPanel.add(searchBtn, c);

        // RESULTS TABLE
        String[] cols = {
                "Bus ID", "Bus Name", "Source", "Destination",
                "Departure", "Arrival", "Seats Available", "Fare (₹)"
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
        table.setRowHeight(38);
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
        tableHeader.setPreferredSize(new Dimension(0, 43));
        tableHeader.setReorderingAllowed(false);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(
                new Color(226, 232, 241)));
        scroll.getViewport().setBackground(Color.WHITE);

        JLabel resultTitle = new JLabel("Available Buses");
        resultTitle.setFont(new Font("Segoe UI", Font.BOLD, 21));
        resultTitle.setForeground(NAVY);

        JLabel resultCount = new JLabel("Search a route to see buses");
        resultCount.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        resultCount.setForeground(new Color(110, 122, 142));

        JPanel resultHeading = new JPanel(new BorderLayout());
        resultHeading.setBackground(Color.WHITE);
        resultHeading.add(resultTitle, BorderLayout.WEST);
        resultHeading.add(resultCount, BorderLayout.EAST);

        JPanel resultsPanel = new JPanel(new BorderLayout(0, 12));
        resultsPanel.setBackground(Color.WHITE);
        resultsPanel.add(resultHeading, BorderLayout.NORTH);
        resultsPanel.add(scroll, BorderLayout.CENTER);

        // BOTTOM ACTIONS
        JButton bookBtn = createButton("Book Selected Bus", NAVY);

        JLabel hint = new JLabel(
                "Select a bus from the table before booking.");
        hint.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        hint.setForeground(new Color(110, 122, 142));

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setBackground(Color.WHITE);
        bottom.add(hint, BorderLayout.WEST);
        bottom.add(bookBtn, BorderLayout.EAST);

        JPanel center = new JPanel(new BorderLayout(0, 22));
        center.setBackground(Color.WHITE);
        center.add(searchPanel, BorderLayout.NORTH);
        center.add(resultsPanel, BorderLayout.CENTER);

        main.add(header, BorderLayout.NORTH);
        main.add(center, BorderLayout.CENTER);
        main.add(bottom, BorderLayout.SOUTH);

        bg.add(main);
        f.setContentPane(bg);

        // SEARCH FUNCTION
        Runnable searchAction = () -> {
            String source = sourceField.getText().trim();
            String destination = destField.getText().trim();

            if (source.isEmpty() || destination.isEmpty()) {
                JOptionPane.showMessageDialog(
                        f,
                        "Please enter both source and destination.",
                        "Missing Information",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (source.equalsIgnoreCase(destination)) {
                JOptionPane.showMessageDialog(
                        f,
                        "Source and destination must be different.",
                        "Invalid Route",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            model.setRowCount(0);

            String sql = "SELECT * FROM bus "
                    + "WHERE LOWER(TRIM(source)) = ? "
                    + "AND LOWER(TRIM(destination)) = ?";

            try (Connection con = DBConnection.getConnection();
                    PreparedStatement ps = con.prepareStatement(sql)) {

                ps.setString(1, source.toLowerCase());
                ps.setString(2, destination.toLowerCase());

                try (ResultSet rs = ps.executeQuery()) {
                    SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a");

                    while (rs.next()) {
                        int total = rs.getInt("total_seats");
                        int booked = rs.getInt("booked_seats");
                        int available = total - booked;

                        Time dep = rs.getTime("departure_time");
                        double hours = rs.getDouble("journey_hours");

                        String depStr = "Not Set";
                        String arrStr = "Not Set";

                        if (dep != null) {
                            long arrMillis = dep.getTime()
                                    + (long) (hours * 60 * 60 * 1000);

                            Time arr = new Time(arrMillis);
                            depStr = sdf.format(dep);
                            arrStr = sdf.format(arr);
                        }

                        model.addRow(new Object[] {
                                rs.getInt("bus_id"),
                                rs.getString("bus_name"),
                                rs.getString("source"),
                                rs.getString("destination"),
                                depStr,
                                arrStr,
                                available,
                                String.format("%.2f",
                                        rs.getDouble("price"))
                        });
                    }
                }

                resultCount.setText(model.getRowCount()
                        + (model.getRowCount() == 1
                                ? " bus found"
                                : " buses found"));

                if (model.getRowCount() == 0) {
                    JOptionPane.showMessageDialog(
                            f,
                            "No buses found for this route.",
                            "No Results",
                            JOptionPane.INFORMATION_MESSAGE);
                }

            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(
                        f,
                        "Unable to search buses. Check your database connection.",
                        "Database Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        };

        searchBtn.addActionListener(e -> searchAction.run());

        sourceField.addActionListener(e -> destField.requestFocusInWindow());
        destField.addActionListener(e -> searchAction.run());

        // BOOK SELECTED BUS
        bookBtn.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();

            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(
                        f, "Please select a bus first.");
                return;
            }

            int row = table.convertRowIndexToModel(selectedRow);

            String busId = model.getValueAt(row, 0).toString();
            int available;

            try {
                available = Integer.parseInt(
                        model.getValueAt(row, 6).toString());
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(f, "Invalid seat information.");
                return;
            }

            if (available <= 0) {
                JOptionPane.showMessageDialog(
                        f, "No seats are available on this bus.");
                return;
            }

            String passenger = JOptionPane.showInputDialog(
                    f, "Enter passenger name:");

            if (passenger == null) {
                return;
            }

            passenger = passenger.trim();

            if (passenger.isEmpty()) {
                JOptionPane.showMessageDialog(
                        f, "Passenger name cannot be empty.");
                return;
            }

            String ticketStr = JOptionPane.showInputDialog(
                    f, "Enter number of tickets (1-" + available + "):");

            if (ticketStr == null) {
                return;
            }

            try {
                int tickets = Integer.parseInt(ticketStr.trim());

                if (tickets < 1 || tickets > available) {
                    JOptionPane.showMessageDialog(
                            f,
                            "Enter a ticket count between 1 and "
                                    + available + ".");
                    return;
                }

                new BookTicket(username, busId, passenger, tickets);

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(
                        f, "Please enter a valid whole number.");
            }
        });

        f.setLocationRelativeTo(null);
        f.setVisible(true);
    }

    private JTextField createField(String placeholder) {
        JTextField field = new JTextField(14);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        field.setForeground(TEXT);
        field.setBackground(Color.WHITE);
        field.setCaretColor(new Color(42, 104, 210));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(
                        new Color(210, 220, 234)),
                new EmptyBorder(10, 12, 10, 12)));
        field.setToolTipText(placeholder);
        return field;
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        label.setForeground(new Color(90, 105, 128));
        return label;
    }

    private JButton createButton(String text, Color base) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setForeground(Color.WHITE);
        button.setBackground(base);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(new EmptyBorder(12, 20, 12, 20));
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