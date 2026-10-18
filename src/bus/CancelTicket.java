package bus;

import db.DBConnection;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class CancelTicket {

    private final Color NAVY = new Color(15, 29, 55);
    private final Color BLUE = new Color(42, 104, 210);
    private final Color RED = new Color(190, 55, 65);
    private final Color LIGHT = new Color(244, 247, 252);
    private final Color TEXT = new Color(40, 52, 72);

    private JComboBox<BookingOption> bookingCombo;
    private JLabel passengerValue;
    private JLabel busValue;
    private JLabel seatValue;
    private JLabel dateValue;
    private JLabel fareValue;
    private JLabel statusValue;

    private final String username;

    public CancelTicket(String username) {
        this.username = username;

        JFrame f = new JFrame("BusGo | Cancel Ticket");
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
        main.setPreferredSize(new Dimension(900, 650));

        // HEADER
        JLabel title = new JLabel("Cancel Your Booking");
        title.setFont(new Font("Segoe UI", Font.BOLD, 30));
        title.setForeground(NAVY);

        JLabel subtitle = new JLabel(
                "Review your ticket details before confirming cancellation.");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        subtitle.setForeground(new Color(110, 122, 142));

        JPanel heading = new JPanel();
        heading.setLayout(new BoxLayout(heading, BoxLayout.Y_AXIS));
        heading.setBackground(Color.WHITE);
        heading.add(title);
        heading.add(Box.createVerticalStrut(8));
        heading.add(subtitle);

        // BOOKING SELECTION
        JLabel selectLabel = new JLabel("SELECT BOOKING");
        selectLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        selectLabel.setForeground(new Color(90, 105, 128));

        bookingCombo = new JComboBox<>();
        bookingCombo.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        bookingCombo.setBackground(Color.WHITE);
        bookingCombo.setPreferredSize(new Dimension(500, 44));

        JPanel selection = new JPanel(new BorderLayout(0, 10));
        selection.setBackground(LIGHT);
        selection.setBorder(new EmptyBorder(20, 22, 20, 22));
        selection.add(selectLabel, BorderLayout.NORTH);
        selection.add(bookingCombo, BorderLayout.CENTER);

        // DETAILS CARD
        JLabel detailsTitle = new JLabel("Booking Details");
        detailsTitle.setFont(new Font("Segoe UI", Font.BOLD, 21));
        detailsTitle.setForeground(NAVY);

        passengerValue = createValueLabel();
        busValue = createValueLabel();
        seatValue = createValueLabel();
        dateValue = createValueLabel();
        fareValue = createValueLabel();

        JPanel details = new JPanel(new GridLayout(0, 2, 18, 18));
        details.setBackground(Color.WHITE);
        details.setBorder(new EmptyBorder(20, 5, 20, 5));

        addDetail(details, "PASSENGER", passengerValue);
        addDetail(details, "BUS", busValue);
        addDetail(details, "SEAT NUMBER", seatValue);
        addDetail(details, "JOURNEY DATE", dateValue);
        addDetail(details, "TICKET FARE", fareValue);

        JPanel detailCard = new JPanel(new BorderLayout(0, 8));
        detailCard.setBackground(Color.WHITE);
        detailCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 241)),
                new EmptyBorder(20, 22, 15, 22)));
        detailCard.add(detailsTitle, BorderLayout.NORTH);
        detailCard.add(details, BorderLayout.CENTER);

        // REFUND INFORMATION
        JPanel warning = new JPanel(new BorderLayout(12, 0));
        warning.setBackground(new Color(255, 247, 232));
        warning.setBorder(new EmptyBorder(15, 18, 15, 18));

        JLabel warningIcon = new JLabel("!");
        warningIcon.setFont(new Font("Segoe UI", Font.BOLD, 24));
        warningIcon.setForeground(new Color(180, 115, 25));

        JLabel warningText = new JLabel(
                "<html><b>Cancellation policy</b><br>"
                        + "A 10% cancellation charge is applied to the "
                        + "selected ticket's fare in this demo.</html>");
        warningText.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        warningText.setForeground(TEXT);

        warning.add(warningIcon, BorderLayout.WEST);
        warning.add(warningText, BorderLayout.CENTER);

        // ACTIONS
        JButton cancelBtn = createButton("Cancel Selected Ticket", RED);
        JButton refreshBtn = createButton("Refresh", BLUE);
        JButton closeBtn = createButton("Close", NAVY);

        statusValue = new JLabel("Loading your bookings...");
        statusValue.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        statusValue.setForeground(new Color(110, 122, 142));

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttons.setBackground(Color.WHITE);
        buttons.add(refreshBtn);
        buttons.add(closeBtn);
        buttons.add(cancelBtn);

        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(Color.WHITE);
        footer.add(statusValue, BorderLayout.WEST);
        footer.add(buttons, BorderLayout.EAST);

        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setBackground(Color.WHITE);
        center.add(selection);
        center.add(Box.createVerticalStrut(18));
        center.add(detailCard);
        center.add(Box.createVerticalStrut(18));
        center.add(warning);

        main.add(heading, BorderLayout.NORTH);
        main.add(center, BorderLayout.CENTER);
        main.add(footer, BorderLayout.SOUTH);

        bg.add(main);
        f.setContentPane(bg);

        // LOAD BOOKINGS
        Runnable loadBookings = () -> {
            bookingCombo.removeAllItems();
            clearDetails();
            statusValue.setText("Loading your bookings...");

            String sql = "SELECT booking_id, bus_id, passenger_name, "
                    + "seat_number, journey_date FROM booking "
                    + "WHERE username = ? ORDER BY booking_date DESC";

            try (Connection con = DBConnection.getConnection();
                    PreparedStatement ps = con.prepareStatement(sql)) {

                ps.setString(1, username);

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        bookingCombo.addItem(new BookingOption(
                                rs.getInt("booking_id"),
                                rs.getInt("bus_id"),
                                rs.getString("passenger_name"),
                                rs.getInt("seat_number"),
                                rs.getDate("journey_date")));
                    }
                }

                statusValue.setText(
                        bookingCombo.getItemCount() + " booking(s) available");

                if (bookingCombo.getItemCount() == 0) {
                    statusValue.setText("You have no bookings to cancel.");
                }

            } catch (Exception ex) {
                ex.printStackTrace();
                statusValue.setText("Unable to load bookings.");

                JOptionPane.showMessageDialog(
                        f,
                        "Could not load your bookings. Check the database connection.",
                        "Database Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        };

        bookingCombo.addActionListener(e -> loadSelectedDetails());

        refreshBtn.addActionListener(e -> loadBookings.run());
        closeBtn.addActionListener(e -> f.dispose());

        // CANCEL TICKET
        cancelBtn.addActionListener(e -> {
            BookingOption selected = (BookingOption) bookingCombo.getSelectedItem();

            if (selected == null) {
                JOptionPane.showMessageDialog(
                        f, "There are no bookings selected to cancel.");
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(
                    f,
                    "Are you sure you want to cancel booking #"
                            + selected.bookingId + "?\n"
                            + "This action cannot be undone.",
                    "Confirm Cancellation",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);

            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }

            cancelBtn.setEnabled(false);

            try (Connection con = DBConnection.getConnection()) {
                con.setAutoCommit(false);

                try {
                    String paymentMethod = "Demo";
                    double price = 0;

                    // Confirm ownership and get the fare.
                    String detailsSql = "SELECT b.payment_method, bus.price "
                            + "FROM booking b JOIN bus "
                            + "ON b.bus_id = bus.bus_id "
                            + "WHERE b.booking_id = ? AND b.username = ? "
                            + "FOR UPDATE";

                    try (PreparedStatement ps = con.prepareStatement(detailsSql)) {
                        ps.setInt(1, selected.bookingId);
                        ps.setString(2, username);

                        try (ResultSet rs = ps.executeQuery()) {
                            if (!rs.next()) {
                                throw new SQLException(
                                        "Booking not found or no longer available.");
                            }

                            paymentMethod = rs.getString("payment_method");
                            price = rs.getDouble("price");

                            if (paymentMethod == null
                                    || paymentMethod.trim().isEmpty()) {
                                paymentMethod = "Demo";
                            }
                        }
                    }

                    double charge = price * 0.10;
                    double refund = price - charge;

                    // Delete only this user's selected booking.
                    String deleteSql = "DELETE FROM booking "
                            + "WHERE booking_id = ? AND username = ?";

                    int deleted;

                    try (PreparedStatement ps = con.prepareStatement(deleteSql)) {
                        ps.setInt(1, selected.bookingId);
                        ps.setString(2, username);
                        deleted = ps.executeUpdate();
                    }

                    if (deleted != 1) {
                        throw new SQLException(
                                "The booking could not be cancelled.");
                    }

                    // This schema tracks booked seats on the bus row.
                    String updateSql = "UPDATE bus SET booked_seats = "
                            + "GREATEST(0, booked_seats - 1) "
                            + "WHERE bus_id = ?";

                    try (PreparedStatement ps = con.prepareStatement(updateSql)) {
                        ps.setInt(1, selected.busId);
                        ps.executeUpdate();
                    }

                    con.commit();

                    loadBookings.run();

                    showRefundDialog(
                            f, paymentMethod, price, charge, refund);

                } catch (Exception ex) {
                    con.rollback();
                    throw ex;
                } finally {
                    con.setAutoCommit(true);
                }

            } catch (Exception ex) {
                ex.printStackTrace();

                JOptionPane.showMessageDialog(
                        f,
                        "Cancellation failed:\n" + ex.getMessage(),
                        "Cancellation Error",
                        JOptionPane.ERROR_MESSAGE);
            } finally {
                cancelBtn.setEnabled(true);
            }
        });

        loadBookings.run();

        f.setLocationRelativeTo(null);
        f.setVisible(true);
    }

    private void loadSelectedDetails() {
        BookingOption selected = (BookingOption) bookingCombo.getSelectedItem();

        if (selected == null) {
            clearDetails();
            return;
        }

        passengerValue.setText(selected.passenger);
        busValue.setText("Bus " + selected.busId);
        seatValue.setText(String.valueOf(selected.seatNumber));
        dateValue.setText(
                selected.journeyDate == null
                        ? "Not available"
                        : selected.journeyDate.toString());

        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(
                        "SELECT price FROM bus WHERE bus_id = ?")) {

            ps.setInt(1, selected.busId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    fareValue.setText(
                            String.format("₹ %.2f", rs.getDouble("price")));
                } else {
                    fareValue.setText("Not available");
                }
            }

        } catch (Exception ex) {
            fareValue.setText("Unavailable");
        }
    }

    private void clearDetails() {
        if (passengerValue != null)
            passengerValue.setText("—");
        if (busValue != null)
            busValue.setText("—");
        if (seatValue != null)
            seatValue.setText("—");
        if (dateValue != null)
            dateValue.setText("—");
        if (fareValue != null)
            fareValue.setText("—");
    }

    private void showRefundDialog(
            JFrame parent,
            String paymentMethod,
            double price,
            double charge,
            double refund) {

        JDialog dialog = new JDialog(parent, "Cancellation Summary", true);
        dialog.setSize(480, 340);
        dialog.setLocationRelativeTo(parent);
        dialog.setLayout(new BorderLayout(15, 15));

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(Color.WHITE);
        content.setBorder(new EmptyBorder(25, 30, 15, 30));

        JLabel title = new JLabel("Ticket Cancelled");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(NAVY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel method = new JLabel("Payment method: " + paymentMethod);
        JLabel fare = new JLabel(
                String.format("Ticket fare: ₹ %.2f", price));
        JLabel chargeLabel = new JLabel(
                String.format("Cancellation charge (10%%): ₹ %.2f", charge));
        JLabel refundLabel = new JLabel(
                String.format("Estimated refund: ₹ %.2f", refund));

        for (JLabel label : new JLabel[] {
                method, fare, chargeLabel, refundLabel }) {
            label.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            label.setForeground(TEXT);
            label.setAlignmentX(Component.LEFT_ALIGNMENT);
            content.add(Box.createVerticalStrut(12));
            content.add(label);
        }

        refundLabel.setFont(new Font("Segoe UI", Font.BOLD, 17));
        refundLabel.setForeground(new Color(34, 139, 90));

        JLabel note = new JLabel(
                "<html><small>This is a demo summary. No real payment "
                        + "refund has been initiated.</small></html>");
        note.setForeground(new Color(110, 122, 142));
        note.setBorder(new EmptyBorder(0, 30, 10, 30));

        JButton done = createButton("Done", NAVY);
        done.addActionListener(e -> dialog.dispose());

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.setBackground(Color.WHITE);
        bottom.add(done);

        dialog.add(content, BorderLayout.CENTER);
        dialog.add(note, BorderLayout.NORTH);
        dialog.add(bottom, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private JLabel createValueLabel() {
        JLabel label = new JLabel("—");
        label.setFont(new Font("Segoe UI", Font.BOLD, 15));
        label.setForeground(TEXT);
        return label;
    }

    private void addDetail(JPanel panel, String name, JLabel value) {
        JPanel item = new JPanel();
        item.setLayout(new BoxLayout(item, BoxLayout.Y_AXIS));
        item.setBackground(Color.WHITE);

        JLabel label = new JLabel(name);
        label.setFont(new Font("Segoe UI", Font.BOLD, 11));
        label.setForeground(new Color(110, 122, 142));

        item.add(label);
        item.add(Box.createVerticalStrut(6));
        item.add(value);

        panel.add(item);
    }

    private JButton createButton(String text, Color base) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setForeground(Color.WHITE);
        button.setBackground(base);
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(new EmptyBorder(12, 18, 12, 18));

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

    private static class BookingOption {
        final int bookingId;
        final int busId;
        final String passenger;
        final int seatNumber;
        final Date journeyDate;

        BookingOption(
                int bookingId,
                int busId,
                String passenger,
                int seatNumber,
                Date journeyDate) {
            this.bookingId = bookingId;
            this.busId = busId;
            this.passenger = passenger;
            this.seatNumber = seatNumber;
            this.journeyDate = journeyDate;
        }

        @Override
        public String toString() {
            return "Booking #" + bookingId
                    + "  |  Bus " + busId
                    + "  |  " + passenger
                    + "  |  Seat " + seatNumber;
        }
    }
}