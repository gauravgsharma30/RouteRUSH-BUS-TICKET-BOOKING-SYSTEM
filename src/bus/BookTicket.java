
package bus;

import db.DBConnection;

import java.awt.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import javax.swing.*;

public class BookTicket {

    // Selected seat and date are received from SeatGridFrame.
    public BookTicket(String username, String busId, String passenger,
            int ticketCount, int selectedSeat,
            String journeyDateText) {

        Connection con = null;

        try {

            // ===== VALIDATION =====

            if (passenger == null || passenger.trim().isEmpty()) {
                JOptionPane.showMessageDialog(null,
                        "Passenger name required");
                return;
            }

            // This seat-selection flow books one seat per passenger.
            if (ticketCount != 1) {
                JOptionPane.showMessageDialog(null,
                        "Please select one seat at a time.");
                return;
            }

            if (selectedSeat <= 0) {
                JOptionPane.showMessageDialog(null,
                        "Please select a valid seat.");
                return;
            }

            if (journeyDateText == null || journeyDateText.trim().isEmpty()) {
                JOptionPane.showMessageDialog(null,
                        "Journey date is required.");
                return;
            }

            int busNo = Integer.parseInt(busId);
            Date journeyDate = Date.valueOf(journeyDateText);

            con = DBConnection.getConnection();
            con.setAutoCommit(false);

            // ===== GET BUS DETAILS =====

            int totalSeats;
            Time departureTime;
            int hours;
            double price;

            String busQuery = "SELECT total_seats, departure_time, journey_hours, price " +
                    "FROM bus WHERE bus_id=?";

            try (PreparedStatement busStmt = con.prepareStatement(busQuery)) {

                busStmt.setInt(1, busNo);

                try (ResultSet rs = busStmt.executeQuery()) {

                    if (!rs.next()) {
                        JOptionPane.showMessageDialog(null,
                                "Bus not found.");
                        return;
                    }

                    totalSeats = rs.getInt("total_seats");
                    departureTime = rs.getTime("departure_time");
                    hours = rs.getInt("journey_hours");
                    price = rs.getDouble("price");
                }
            }

            // ===== VALIDATE SELECTED SEAT =====

            if (selectedSeat > totalSeats) {
                JOptionPane.showMessageDialog(null,
                        "Invalid seat number.");
                return;
            }

            String checkQuery = "SELECT seat_number FROM booking " +
                    "WHERE bus_id=? AND journey_date=? AND seat_number=?";

            try (PreparedStatement checkStmt = con.prepareStatement(checkQuery)) {

                checkStmt.setInt(1, busNo);
                checkStmt.setDate(2, journeyDate);
                checkStmt.setInt(3, selectedSeat);

                try (ResultSet checkRs = checkStmt.executeQuery()) {

                    if (checkRs.next()) {
                        JOptionPane.showMessageDialog(null,
                                "Sorry, this seat is already booked.\n" +
                                        "Please select another seat.");

                        return;
                    }
                }
            }

            // ===== PAYMENT METHOD SELECTION =====

            String[] options = {
                    "UPI",
                    "Credit Card",
                    "Debit Card"
            };

            JDialog methodDialog = new JDialog(
                    (Frame) null, "Choose Payment Method", true);

            methodDialog.setSize(460, 320);
            methodDialog.setLocationRelativeTo(null);
            methodDialog.setLayout(new BorderLayout());
            methodDialog.getContentPane().setBackground(Color.WHITE);

            JPanel methodContent = new JPanel();
            methodContent.setLayout(
                    new BoxLayout(methodContent, BoxLayout.Y_AXIS));
            methodContent.setBackground(Color.WHITE);
            methodContent.setBorder(
                    BorderFactory.createEmptyBorder(25, 30, 15, 30));

            JLabel methodTitle = new JLabel("Secure Checkout");
            methodTitle.setFont(
                    new Font("Segoe UI", Font.BOLD, 25));
            methodTitle.setForeground(new Color(15, 29, 55));

            JLabel amountLabel = new JLabel(
                    String.format("Total payable: ₹ %.2f", price));

            amountLabel.setFont(
                    new Font("Segoe UI", Font.BOLD, 17));
            amountLabel.setForeground(new Color(42, 104, 210));

            JLabel methodLabel = new JLabel("Select payment method");

            methodLabel.setFont(
                    new Font("Segoe UI", Font.PLAIN, 14));
            methodLabel.setForeground(new Color(90, 105, 128));

            JComboBox<String> methodCombo = new JComboBox<>(options);

            methodCombo.setFont(
                    new Font("Segoe UI", Font.PLAIN, 15));
            methodCombo.setBackground(new Color(244, 247, 252));
            methodCombo.setMaximumSize(
                    new Dimension(Integer.MAX_VALUE, 42));

            methodContent.add(methodTitle);
            methodContent.add(Box.createVerticalStrut(12));
            methodContent.add(amountLabel);
            methodContent.add(Box.createVerticalStrut(20));
            methodContent.add(methodLabel);
            methodContent.add(Box.createVerticalStrut(8));
            methodContent.add(methodCombo);

            final String[] chosenMethod = { null };

            JButton payContinue = new JButton("Continue to Payment");

            payContinue.setFont(
                    new Font("Segoe UI", Font.BOLD, 14));
            payContinue.setBackground(new Color(42, 104, 210));
            payContinue.setForeground(Color.WHITE);
            payContinue.setFocusPainted(false);
            payContinue.setCursor(
                    new Cursor(Cursor.HAND_CURSOR));
            payContinue.setBorder(
                    BorderFactory.createEmptyBorder(12, 20, 12, 20));

            JButton methodCancel = new JButton("Cancel");

            methodCancel.setFont(
                    new Font("Segoe UI", Font.PLAIN, 14));
            methodCancel.setFocusPainted(false);

            JPanel methodButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 15));

            methodButtons.setBackground(Color.WHITE);
            methodButtons.add(methodCancel);
            methodButtons.add(payContinue);

            payContinue.addActionListener(e -> {
                chosenMethod[0] = (String) methodCombo.getSelectedItem();

                methodDialog.dispose();
            });

            methodCancel.addActionListener(e -> methodDialog.dispose());

            methodDialog.add(methodContent, BorderLayout.CENTER);
            methodDialog.add(methodButtons, BorderLayout.SOUTH);

            methodDialog.setDefaultCloseOperation(
                    JDialog.DISPOSE_ON_CLOSE);

            methodDialog.setVisible(true);

            if (chosenMethod[0] == null) {
                return;
            }

            String paymentMethod = chosenMethod[0];

            // ===== PROCESS PAYMENT =====

            double totalAmount = price;

            boolean paymentSuccess = processPayment(paymentMethod, totalAmount);

            if (!paymentSuccess) {
                JOptionPane.showMessageDialog(null,
                        "Payment was cancelled or unsuccessful.");
                return;
            }

            // ===== BOOK THE EXACT SELECTED SEAT =====

            List<Integer> seatList = bookSeatsAtomically(
                    con,
                    busNo,
                    journeyDateText,
                    journeyDate,
                    selectedSeat,
                    username,
                    passenger.trim(),
                    paymentMethod);

            if (seatList == null) {

                JOptionPane.showMessageDialog(null,
                        "Sorry, seat " + selectedSeat +
                                " was just booked by another user.\n" +
                                "Your demo payment was not actually charged.\n" +
                                "Please refresh and select another seat.");

                return;
            }

            // ===== ARRIVAL TIME =====

            Time arrivalTime = null;

            if (departureTime != null) {

                long depMillis = departureTime.getTime();

                long arrMillis = depMillis + (hours * 60L * 60L * 1000L);

                arrivalTime = new Time(arrMillis);
            }

            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a");

            String depStr = departureTime != null
                    ? sdf.format(departureTime)
                    : "Not Set";

            String arrStr = arrivalTime != null
                    ? sdf.format(arrivalTime)
                    : "Not Set";

            // ===== E-TICKET =====

            String ticketSeats = String.valueOf(seatList.get(0));

            final String ticketDepart = depStr;
            final String ticketArrive = arrStr;

            javax.swing.SwingUtilities.invokeLater(() -> new ETicket(
                    busId,
                    passenger,
                    ticketSeats,
                    journeyDateText,
                    ticketDepart,
                    ticketArrive,
                    paymentMethod,
                    totalAmount));

        } catch (Exception e) {

            try {
                if (con != null) {
                    con.rollback();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            e.printStackTrace();

            JOptionPane.showMessageDialog(null,
                    "Booking Error: " + e.getMessage());

        } finally {

            try {
                if (con != null) {
                    con.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // ===== ATOMIC SEAT BOOKING =====

    static List<Integer> bookSeatsAtomically(
            Connection con,
            int busNo,
            String journeyDateText,
            Date journeyDate,
            int selectedSeat,
            String username,
            String passenger,
            String paymentMethod) throws Exception {

        List<Integer> seatList = new ArrayList<>();

        String who = Thread.currentThread().getName();

        // Lock for this bus and journey date.
        ReentrantLock seatLock = Seatlockmanager.lockFor(busNo, journeyDateText);

        System.out.println(
                "[" + who + "] Waiting for seat lock: " +
                        busNo + ", " + journeyDateText);

        if (!seatLock.tryLock(15, TimeUnit.SECONDS)) {

            con.rollback();

            throw new IllegalStateException(
                    "System is busy. Please try again.");
        }

        try {

            System.out.println(
                    "[" + who + "] Entered critical section");

            // ===== LOCK BUS ROW IN DATABASE =====

            String busLockQuery = "SELECT total_seats FROM bus " +
                    "WHERE bus_id=? FOR UPDATE";

            int totalSeats = 0;

            try (PreparedStatement busLock = con.prepareStatement(busLockQuery)) {

                busLock.setInt(1, busNo);

                try (ResultSet busRs = busLock.executeQuery()) {

                    if (busRs.next()) {
                        totalSeats = busRs.getInt("total_seats");
                    }
                }
            }

            // ===== GET CURRENTLY BOOKED SEATS =====

            String seatQuery = "SELECT seat_number FROM booking " +
                    "WHERE bus_id=? AND journey_date=? FOR UPDATE";

            List<Integer> bookedSeatNumbers = new ArrayList<>();

            try (PreparedStatement seatStmt = con.prepareStatement(seatQuery)) {

                seatStmt.setInt(1, busNo);
                seatStmt.setDate(2, journeyDate);

                try (ResultSet seatRs = seatStmt.executeQuery()) {

                    while (seatRs.next()) {
                        bookedSeatNumbers.add(
                                seatRs.getInt("seat_number"));
                    }
                }
            }

            // ===== CHECK THE SELECTED SEAT AGAIN =====

            if (selectedSeat <= 0 ||
                    selectedSeat > totalSeats ||
                    bookedSeatNumbers.contains(selectedSeat)) {

                con.rollback();

                System.out.println(
                        "[" + who + "] Selected seat unavailable");

                return null;
            }

            // ===== INSERT EXACT SELECTED SEAT =====

            String insertQuery = "INSERT INTO booking " +
                    "(username, bus_id, passenger_name, seat_number, " +
                    "journey_date, payment_method) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";

            try (PreparedStatement insert = con.prepareStatement(insertQuery)) {

                insert.setString(1, username);
                insert.setInt(2, busNo);
                insert.setString(3, passenger);
                insert.setInt(4, selectedSeat);
                insert.setDate(5, journeyDate);
                insert.setString(6, paymentMethod);

                insert.executeUpdate();
            }

            seatList.add(selectedSeat);

            // Commit before releasing the lock.
            con.commit();

            System.out.println(
                    "[" + who + "] Successfully booked seat " +
                            selectedSeat);

            return seatList;

        } finally {

            seatLock.unlock();
        }
    }

    // ===== PAYMENT WINDOW =====

    private boolean processPayment(String method, double amount) {

        JDialog dialog = new JDialog(
                (Frame) null, "BusGo | Payment", true);

        dialog.setSize(470, 390);
        dialog.setLocationRelativeTo(null);
        dialog.setLayout(new BorderLayout());
        dialog.getContentPane().setBackground(Color.WHITE);

        JPanel content = new JPanel();

        content.setLayout(
                new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(Color.WHITE);
        content.setBorder(
                BorderFactory.createEmptyBorder(25, 30, 15, 30));

        JLabel title = new JLabel("Complete Your Payment");

        title.setFont(
                new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(new Color(15, 29, 55));

        JLabel methodLabel = new JLabel("Payment method: " + method);

        methodLabel.setFont(
                new Font("Segoe UI", Font.PLAIN, 14));
        methodLabel.setForeground(new Color(90, 105, 128));

        JLabel amountLabel = new JLabel(String.format("₹ %.2f", amount));

        amountLabel.setFont(
                new Font("Segoe UI", Font.BOLD, 30));
        amountLabel.setForeground(new Color(42, 104, 210));

        JLabel fieldLabel = new JLabel(
                method.equals("UPI") ? "UPI ID"
                        : method.equals("Credit Card")
                                ? "Credit card number"
                                : "Debit card number");

        fieldLabel.setFont(
                new Font("Segoe UI", Font.BOLD, 13));
        fieldLabel.setForeground(new Color(40, 52, 72));

        JTextField paymentField = new JTextField();

        paymentField.setFont(
                new Font("Segoe UI", Font.PLAIN, 15));
        paymentField.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 42));

        paymentField.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                new Color(210, 220, 234)),
                        BorderFactory.createEmptyBorder(
                                8, 10, 8, 10)));

        content.add(title);
        content.add(Box.createVerticalStrut(10));
        content.add(methodLabel);
        content.add(Box.createVerticalStrut(12));
        content.add(amountLabel);
        content.add(Box.createVerticalStrut(22));
        content.add(fieldLabel);
        content.add(Box.createVerticalStrut(8));
        content.add(paymentField);

        final boolean[] success = { false };

        JButton payBtn = new JButton("Pay Now");

        payBtn.setFont(
                new Font("Segoe UI", Font.BOLD, 15));
        payBtn.setForeground(Color.WHITE);
        payBtn.setBackground(new Color(42, 104, 210));
        payBtn.setFocusPainted(false);
        payBtn.setCursor(
                new Cursor(Cursor.HAND_CURSOR));
        payBtn.setBorder(
                BorderFactory.createEmptyBorder(
                        13, 25, 13, 25));

        JButton cancelBtn = new JButton("Cancel");

        cancelBtn.setFont(
                new Font("Segoe UI", Font.PLAIN, 14));
        cancelBtn.setFocusPainted(false);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 15));

        buttons.setBackground(Color.WHITE);
        buttons.add(cancelBtn);
        buttons.add(payBtn);

        payBtn.addActionListener(e -> {

            String input = paymentField.getText().trim();

            if (input.isEmpty()) {

                JOptionPane.showMessageDialog(
                        dialog,
                        "Please enter your payment details.");

                return;
            }

            // Demonstration only.
            // This does not contact a real payment gateway.

            int answer = JOptionPane.showConfirmDialog(
                    dialog,
                    "Confirm demo payment of ₹ " +
                            String.format("%.2f", amount) + "?",
                    "Confirm Payment",
                    JOptionPane.YES_NO_OPTION);

            if (answer == JOptionPane.YES_OPTION) {

                success[0] = true;
                dialog.dispose();
            }
        });

        cancelBtn.addActionListener(e -> dialog.dispose());

        dialog.add(content, BorderLayout.CENTER);
        dialog.add(buttons, BorderLayout.SOUTH);

        dialog.setDefaultCloseOperation(
                JDialog.DISPOSE_ON_CLOSE);

        dialog.setVisible(true);

        return success[0];
    }
}
