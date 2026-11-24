
package bus;

import db.DBConnection;

import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.SimpleDateFormat;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class CancelTicket {

    private final Color NAVY = new Color(15, 29, 55);
    private final Color BLUE = new Color(42, 104, 210);
    private final Color RED = new Color(190, 55, 65);
    private final Color GREEN = new Color(34, 139, 90);
    private final Color LIGHT = new Color(244, 247, 252);
    private final Color TEXT = new Color(40, 52, 72);

    private DefaultListModel<BookingOption> bookingModel;
    private JList<BookingOption> bookingList;

    private JLabel bookingIdValue;
    private JLabel passengerValue;
    private JLabel busValue;
    private JLabel seatValue;
    private JLabel dateValue;
    private JLabel departureValue;
    private JLabel arrivalValue;
    private JLabel paymentValue;
    private JLabel fareValue;

    private JLabel statusValue;

    private JButton cancelBtn;

    private final String username;

    public CancelTicket(String username) {

        this.username = username;

        JFrame f = new JFrame("BusGo | Cancel Ticket");
        f.setExtendedState(JFrame.MAXIMIZED_BOTH);
        f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // BACKGROUND
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

        // MAIN PANEL
        JPanel main = new JPanel(new BorderLayout(0, 22));
        main.setBackground(Color.WHITE);
        main.setBorder(new EmptyBorder(30, 35, 30, 35));
        main.setPreferredSize(new Dimension(1250, 720));

        // HEADER
        JLabel title = new JLabel("Cancel Your Booking");
        title.setFont(new Font("Segoe UI", Font.BOLD, 30));
        title.setForeground(NAVY);

        JLabel subtitle = new JLabel(
                "Select a ticket to view its details and cancel your booking.");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        subtitle.setForeground(new Color(110, 122, 142));

        JPanel heading = new JPanel();
        heading.setLayout(new BoxLayout(heading, BoxLayout.Y_AXIS));
        heading.setBackground(Color.WHITE);
        heading.add(title);
        heading.add(Box.createVerticalStrut(8));
        heading.add(subtitle);

        // LEFT: BOOKING LIST
        JLabel listTitle = new JLabel("YOUR BOOKINGS");
        listTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        listTitle.setForeground(NAVY);

        bookingModel = new DefaultListModel<>();
        bookingList = new JList<>(bookingModel);

        bookingList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        bookingList.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        bookingList.setBackground(Color.WHITE);
        bookingList.setSelectionBackground(new Color(218, 232, 255));
        bookingList.setSelectionForeground(NAVY);
        bookingList.setFixedCellHeight(85);
        bookingList.setBorder(new EmptyBorder(5, 5, 5, 5));

        // Custom booking card renderer
        bookingList.setCellRenderer(new DefaultListCellRenderer() {

            @Override
            public Component getListCellRendererComponent(
                    JList<?> list,
                    Object value,
                    int index,
                    boolean isSelected,
                    boolean cellHasFocus) {

                JLabel label = (JLabel) super.getListCellRendererComponent(
                        list, value, index, isSelected, cellHasFocus);

                BookingOption booking = (BookingOption) value;

                label.setText(
                        "<html><b>Booking #" + booking.bookingId
                                + "</b><br>"
                                + "Bus " + booking.busId
                                + " &nbsp; | &nbsp; Seat " + booking.seatNumber
                                + "<br>"
                                + booking.passenger
                                + "</html>");

                label.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                label.setBorder(new EmptyBorder(12, 12, 12, 12));

                if (isSelected) {
                    label.setBackground(new Color(218, 232, 255));
                    label.setForeground(NAVY);
                } else {
                    label.setBackground(Color.WHITE);
                    label.setForeground(TEXT);
                }

                label.setOpaque(true);

                return label;
            }
        });

        JScrollPane listScroll = new JScrollPane(bookingList);
        listScroll.setBorder(BorderFactory.createLineBorder(
                new Color(226, 232, 241)));

        JPanel listPanel = new JPanel(new BorderLayout(0, 12));
        listPanel.setBackground(Color.WHITE);
        listPanel.setBorder(new EmptyBorder(0, 0, 0, 15));

        listPanel.add(listTitle, BorderLayout.NORTH);
        listPanel.add(listScroll, BorderLayout.CENTER);

        // RIGHT: SELECTED TICKET DETAILS
        JLabel detailsTitle = new JLabel("E-Ticket Details");
        detailsTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        detailsTitle.setForeground(NAVY);

        JLabel ticketSubtitle = new JLabel(
                "Your selected booking information");
        ticketSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        ticketSubtitle.setForeground(new Color(110, 122, 142));

        JPanel detailsHeading = new JPanel();
        detailsHeading.setLayout(new BoxLayout(detailsHeading, BoxLayout.Y_AXIS));
        detailsHeading.setBackground(Color.WHITE);
        detailsHeading.add(detailsTitle);
        detailsHeading.add(Box.createVerticalStrut(5));
        detailsHeading.add(ticketSubtitle);

        bookingIdValue = createValueLabel();
        passengerValue = createValueLabel();
        busValue = createValueLabel();
        seatValue = createValueLabel();
        dateValue = createValueLabel();
        departureValue = createValueLabel();
        arrivalValue = createValueLabel();
        paymentValue = createValueLabel();
        fareValue = createValueLabel();

        JPanel details = new JPanel(new GridLayout(0, 2, 20, 20));
        details.setBackground(Color.WHITE);
        details.setBorder(new EmptyBorder(25, 5, 25, 5));

        addDetail(details, "BOOKING ID", bookingIdValue);
        addDetail(details, "PASSENGER", passengerValue);
        addDetail(details, "BUS", busValue);
        addDetail(details, "SEAT NUMBER", seatValue);
        addDetail(details, "JOURNEY DATE", dateValue);
        addDetail(details, "DEPARTURE TIME", departureValue);
        addDetail(details, "ARRIVAL TIME", arrivalValue);
        addDetail(details, "PAYMENT METHOD", paymentValue);
        addDetail(details, "TICKET FARE", fareValue);

        JPanel detailCard = new JPanel(new BorderLayout(0, 10));
        detailCard.setBackground(Color.WHITE);
        detailCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 241)),
                new EmptyBorder(22, 25, 22, 25)));

        detailCard.add(detailsHeading, BorderLayout.NORTH);
        detailCard.add(details, BorderLayout.CENTER);

        // EMPTY DETAILS MESSAGE
        JLabel emptyLabel = new JLabel(
                "<html><center>Select any booking from the left<br>"
                        + "to view its e-ticket details.</center></html>",
                SwingConstants.CENTER);

        emptyLabel.setFont(new Font("Segoe UI", Font.PLAIN, 17));
        emptyLabel.setForeground(new Color(130, 140, 155));

        JPanel rightPanel = new JPanel(new BorderLayout(0, 15));
        rightPanel.setBackground(Color.WHITE);
        rightPanel.add(detailCard, BorderLayout.NORTH);

        // REFUND POLICY
        JPanel warning = new JPanel(new BorderLayout(12, 0));
        warning.setBackground(new Color(255, 247, 232));
        warning.setBorder(new EmptyBorder(15, 18, 15, 18));

        JLabel warningIcon = new JLabel("!");
        warningIcon.setFont(new Font("Segoe UI", Font.BOLD, 24));
        warningIcon.setForeground(new Color(180, 115, 25));

        JLabel warningText = new JLabel(
                "<html><b>Cancellation policy</b><br>"
                        + "A 10% cancellation charge is applied. "
                        + "The remaining 90% is shown as your estimated refund."
                        + "</html>");

        warningText.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        warningText.setForeground(TEXT);

        warning.add(warningIcon, BorderLayout.WEST);
        warning.add(warningText, BorderLayout.CENTER);

        rightPanel.add(warning, BorderLayout.CENTER);

        // MAIN CONTENT
        JPanel center = new JPanel(new GridLayout(1, 2, 20, 0));
        center.setBackground(Color.WHITE);

        center.add(listPanel);
        center.add(rightPanel);

        // FOOTER
        JButton refreshBtn = createButton("Refresh", BLUE);
        JButton closeBtn = createButton("Close", NAVY);

        cancelBtn = createButton("Cancel Selected Ticket", RED);
        cancelBtn.setEnabled(false);

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

        main.add(heading, BorderLayout.NORTH);
        main.add(center, BorderLayout.CENTER);
        main.add(footer, BorderLayout.SOUTH);

        bg.add(main);
        f.setContentPane(bg);

        // SELECT BOOKING
        bookingList.addListSelectionListener(e -> {

            if (!e.getValueIsAdjusting()) {
                loadSelectedDetails();
            }
        });

        // REFRESH
        refreshBtn.addActionListener(e -> loadBookings());

        // CLOSE
        closeBtn.addActionListener(e -> f.dispose());

        // CANCEL SELECTED TICKET
        cancelBtn.addActionListener(e -> cancelSelectedBooking(f));

        // LOAD ALL BOOKINGS
        loadBookings();

        f.setLocationRelativeTo(null);
        f.setVisible(true);
    }

    // LOAD ALL USER BOOKINGS
    private void loadBookings() {

        bookingModel.clear();
        clearDetails();

        statusValue.setText("Loading your bookings...");
        cancelBtn.setEnabled(false);

        String sql = "SELECT b.booking_id, b.bus_id, b.passenger_name, "
                + "b.seat_number, b.journey_date, b.payment_method, "
                + "bus.price, bus.departure_time, bus.journey_hours "
                + "FROM booking b "
                + "JOIN bus ON b.bus_id = bus.bus_id "
                + "WHERE b.username = ? "
                + "ORDER BY b.booking_date DESC";

        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {

            if (con == null) {
                throw new SQLException("Database connection failed.");
            }

            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    Time departureTime = rs.getTime("departure_time");
                    int hours = rs.getInt("journey_hours");

                    Time arrivalTime = null;

                    if (departureTime != null) {
                        arrivalTime = new Time(
                                departureTime.getTime()
                                        + hours * 60L * 60L * 1000L);
                    }

                    bookingModel.addElement(new BookingOption(
                            rs.getInt("booking_id"),
                            rs.getInt("bus_id"),
                            rs.getString("passenger_name"),
                            rs.getInt("seat_number"),
                            rs.getDate("journey_date"),
                            rs.getString("payment_method"),
                            rs.getDouble("price"),
                            departureTime,
                            arrivalTime));
                }
            }

            statusValue.setText(
                    bookingModel.size() + " booking(s) available");

            if (!bookingModel.isEmpty()) {
                bookingList.setSelectedIndex(0);
            } else {
                clearDetails();
                statusValue.setText("You have no bookings to cancel.");
            }

        } catch (Exception ex) {

            ex.printStackTrace();

            statusValue.setText("Unable to load bookings.");

            JOptionPane.showMessageDialog(
                    null,
                    "Could not load your bookings.\n"
                            + ex.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // DISPLAY SELECTED TICKET DETAILS
    private void loadSelectedDetails() {

        BookingOption selected = bookingList.getSelectedValue();

        if (selected == null) {
            clearDetails();
            cancelBtn.setEnabled(false);
            return;
        }

        bookingIdValue.setText("#" + selected.bookingId);
        passengerValue.setText(selected.passenger);
        busValue.setText("Bus " + selected.busId);
        seatValue.setText(String.valueOf(selected.seatNumber));

        dateValue.setText(
                selected.journeyDate == null
                        ? "Not available"
                        : selected.journeyDate.toString());

        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a");

        departureValue.setText(
                selected.departureTime == null
                        ? "Not Set"
                        : sdf.format(selected.departureTime));

        arrivalValue.setText(
                selected.arrivalTime == null
                        ? "Not Set"
                        : sdf.format(selected.arrivalTime));

        paymentValue.setText(
                selected.paymentMethod == null
                        ? "Demo"
                        : selected.paymentMethod);

        fareValue.setText(
                String.format("₹ %.2f", selected.price));

        cancelBtn.setEnabled(true);
    }

    // CANCEL SELECTED BOOKING
    private void cancelSelectedBooking(JFrame parent) {

        BookingOption selected = bookingList.getSelectedValue();

        if (selected == null) {
            JOptionPane.showMessageDialog(
                    parent,
                    "Please select a booking first.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                parent,
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

            if (con == null) {
                throw new SQLException("Database connection failed.");
            }

            con.setAutoCommit(false);

            try {

                String paymentMethod = "Demo";
                double price = 0;

                // CONFIRM OWNERSHIP AND GET FARE
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

                // REFUND CALCULATION
                double charge = price * 0.10;
                double refund = price - charge;

                // DELETE SELECTED BOOKING
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

                // UPDATE BUS BOOKED SEATS
                String updateSql = "UPDATE bus SET booked_seats = "
                        + "GREATEST(0, booked_seats - 1) "
                        + "WHERE bus_id = ?";

                try (PreparedStatement ps = con.prepareStatement(updateSql)) {

                    ps.setInt(1, selected.busId);
                    ps.executeUpdate();
                }

                con.commit();

                // RELEASE WAITING SEAT LOCK
                Seatlockmanager.releaseWaiting(
                        selected.busId,
                        selected.seatNumber);

                // REFRESH THE BOOKING LIST
                loadBookings();

                // SHOW REFUND SUMMARY
                showRefundDialog(
                        parent,
                        paymentMethod,
                        price,
                        charge,
                        refund);

            } catch (Exception ex) {

                con.rollback();
                throw ex;

            } finally {

                con.setAutoCommit(true);
            }

        } catch (Exception ex) {

            ex.printStackTrace();

            JOptionPane.showMessageDialog(
                    parent,
                    "Cancellation failed:\n" + ex.getMessage(),
                    "Cancellation Error",
                    JOptionPane.ERROR_MESSAGE);

        } finally {

            cancelBtn.setEnabled(bookingList.getSelectedValue() != null);
        }
    }

    // REFUND SUMMARY
    private void showRefundDialog(
            JFrame parent,
            String paymentMethod,
            double price,
            double charge,
            double refund) {

        JDialog dialog = new JDialog(
                parent, "Cancellation Summary", true);

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

        content.add(title);

        for (JLabel label : new JLabel[] {
                method, fare, chargeLabel, refundLabel }) {

            label.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            label.setForeground(TEXT);
            label.setAlignmentX(Component.LEFT_ALIGNMENT);

            content.add(Box.createVerticalStrut(12));
            content.add(label);
        }

        refundLabel.setFont(new Font("Segoe UI", Font.BOLD, 17));
        refundLabel.setForeground(GREEN);

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

    // CLEAR TICKET DETAILS
    private void clearDetails() {

        if (bookingIdValue == null)
            return;

        bookingIdValue.setText("—");
        passengerValue.setText("—");
        busValue.setText("—");
        seatValue.setText("—");
        dateValue.setText("—");
        departureValue.setText("—");
        arrivalValue.setText("—");
        paymentValue.setText("—");
        fareValue.setText("—");
    }

    // CREATE VALUE LABEL
    private JLabel createValueLabel() {

        JLabel label = new JLabel("—");
        label.setFont(new Font("Segoe UI", Font.BOLD, 15));
        label.setForeground(TEXT);

        return label;
    }

    // ADD DETAIL FIELD
    private void addDetail(
            JPanel panel,
            String name,
            JLabel value) {

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

    // BUTTON DESIGN
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
                if (button.isEnabled()) {
                    button.setBackground(base.brighter());
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(base);
            }
        });

        return button;
    }

    // BOOKING DATA
    private static class BookingOption {

        final int bookingId;
        final int busId;
        final String passenger;
        final int seatNumber;
        final Date journeyDate;
        final String paymentMethod;
        final double price;
        final Time departureTime;
        final Time arrivalTime;

        BookingOption(
                int bookingId,
                int busId,
                String passenger,
                int seatNumber,
                Date journeyDate,
                String paymentMethod,
                double price,
                Time departureTime,
                Time arrivalTime) {

            this.bookingId = bookingId;
            this.busId = busId;
            this.passenger = passenger;
            this.seatNumber = seatNumber;
            this.journeyDate = journeyDate;
            this.paymentMethod = paymentMethod;
            this.price = price;
            this.departureTime = departureTime;
            this.arrivalTime = arrivalTime;
        }
    }
}
