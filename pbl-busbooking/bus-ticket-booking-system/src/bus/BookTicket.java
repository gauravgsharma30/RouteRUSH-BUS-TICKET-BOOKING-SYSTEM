package bus;

import db.DBConnection;
import java.awt.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import javax.swing.*;

public class BookTicket {

    public BookTicket(String username, String busId, String passenger, int ticketCount) {

        Connection con = null;

        try {

            if (passenger == null || passenger.trim().isEmpty()) {
                JOptionPane.showMessageDialog(null, "Passenger name required");
                return;
            }

            if (ticketCount <= 0) {
                JOptionPane.showMessageDialog(null, "Invalid ticket count");
                return;
            }

            con = DBConnection.getConnection();
            con.setAutoCommit(false);

            // ===== MODERN JOURNEY DATE SELECTION =====
            String[] dates = new String[7];
            LocalDate today = LocalDate.now();

            for (int i = 0; i < 7; i++) {
                dates[i] = today.plusDays(i).toString();
            }

            JDialog dateDialog = new JDialog(
                    (Frame) null, "Select Journey Date", true);
            dateDialog.setSize(460, 290);
            dateDialog.setLocationRelativeTo(null);
            dateDialog.setLayout(new BorderLayout());
            dateDialog.getContentPane().setBackground(Color.WHITE);

            JPanel dateContent = new JPanel();
            dateContent.setLayout(new BoxLayout(dateContent, BoxLayout.Y_AXIS));
            dateContent.setBackground(Color.WHITE);
            dateContent.setBorder(
                    BorderFactory.createEmptyBorder(25, 30, 15, 30));

            JLabel dateTitle = new JLabel("Plan Your Journey");
            dateTitle.setFont(new Font("Segoe UI", Font.BOLD, 25));
            dateTitle.setForeground(new Color(15, 29, 55));
            dateTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

            JLabel dateSubtitle = new JLabel("Choose your preferred travel date");
            dateSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            dateSubtitle.setForeground(new Color(110, 122, 142));
            dateSubtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

            JComboBox<String> dateCombo = new JComboBox<>(dates);
            dateCombo.setFont(new Font("Segoe UI", Font.PLAIN, 16));
            dateCombo.setBackground(new Color(244, 247, 252));
            dateCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
            dateCombo.setAlignmentX(Component.LEFT_ALIGNMENT);

            dateContent.add(dateTitle);
            dateContent.add(Box.createVerticalStrut(8));
            dateContent.add(dateSubtitle);
            dateContent.add(Box.createVerticalStrut(25));
            dateContent.add(dateCombo);

            final String[] selectedDate = { null };

            JButton continueBtn = new JButton("Continue");
            continueBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
            continueBtn.setBackground(new Color(42, 104, 210));
            continueBtn.setForeground(Color.WHITE);
            continueBtn.setFocusPainted(false);
            continueBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            continueBtn.setBorder(BorderFactory.createEmptyBorder(12, 25, 12, 25));

            JButton backBtn = new JButton("Cancel");
            backBtn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            backBtn.setBackground(new Color(235, 239, 246));
            backBtn.setForeground(new Color(40, 52, 72));
            backBtn.setFocusPainted(false);

            JPanel dateButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 15));
            dateButtons.setBackground(Color.WHITE);
            dateButtons.setBorder(
                    BorderFactory.createEmptyBorder(0, 20, 10, 20));

            dateButtons.add(backBtn);
            dateButtons.add(continueBtn);

            continueBtn.addActionListener(e -> {
                selectedDate[0] = (String) dateCombo.getSelectedItem();
                dateDialog.dispose();
            });

            backBtn.addActionListener(e -> dateDialog.dispose());

            dateDialog.add(dateContent, BorderLayout.CENTER);
            dateDialog.add(dateButtons, BorderLayout.SOUTH);
            dateDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            dateDialog.setVisible(true);

            if (selectedDate[0] == null) {
                return;
            }

            String selectedDateStr = selectedDate[0];
            Date journeyDate = Date.valueOf(selectedDateStr);

            // ===== GET BUS DETAILS =====
            PreparedStatement busStmt = con.prepareStatement(
                    "SELECT total_seats,departure_time,journey_hours,price FROM bus WHERE bus_id=?");

            busStmt.setInt(1, Integer.parseInt(busId));

            ResultSet rs = busStmt.executeQuery();

            int totalSeats = 0;
            Time departureTime = null;
            int hours = 0;
            double price = 0;

            if (rs.next()) {
                totalSeats = rs.getInt("total_seats");
                departureTime = rs.getTime("departure_time");
                hours = rs.getInt("journey_hours");
                price = rs.getDouble("price");
            }

            // ===== DATE WISE BOOKING CHECK =====
            PreparedStatement checkStmt = con.prepareStatement(
                    "SELECT COUNT(*) FROM booking WHERE bus_id=? AND journey_date=?");

            checkStmt.setInt(1, Integer.parseInt(busId));
            checkStmt.setDate(2, journeyDate);

            ResultSet rs2 = checkStmt.executeQuery();
            int bookedSeats = 0;

            if (rs2.next()) {
                bookedSeats = rs2.getInt(1);
            }

            int availableSeats = totalSeats - bookedSeats;

            if (ticketCount > availableSeats) {

                JOptionPane.showMessageDialog(null,
                        "Seats are not available for " + selectedDateStr +
                                "\nAvailable seats: " + availableSeats);

                return;
            }

            if (availableSeats <= 0) {

                JOptionPane.showMessageDialog(null,
                        "Bus is fully booked for " + selectedDateStr);

                return;
            }

            // ===== MODERN PAYMENT SELECTION =====
            String[] options = { "UPI", "Credit Card", "Debit Card" };

            JDialog methodDialog = new JDialog(
                    (Frame) null, "Choose Payment Method", true);
            methodDialog.setSize(460, 320);
            methodDialog.setLocationRelativeTo(null);
            methodDialog.setLayout(new BorderLayout());
            methodDialog.getContentPane().setBackground(Color.WHITE);

            JPanel methodContent = new JPanel();
            methodContent.setLayout(new BoxLayout(methodContent, BoxLayout.Y_AXIS));
            methodContent.setBackground(Color.WHITE);
            methodContent.setBorder(
                    BorderFactory.createEmptyBorder(25, 30, 15, 30));

            JLabel methodTitle = new JLabel("Secure Checkout");
            methodTitle.setFont(new Font("Segoe UI", Font.BOLD, 25));
            methodTitle.setForeground(new Color(15, 29, 55));

            JLabel amountLabel = new JLabel(
                    String.format("Total payable: ₹ %.2f", price * ticketCount));
            amountLabel.setFont(new Font("Segoe UI", Font.BOLD, 17));
            amountLabel.setForeground(new Color(42, 104, 210));

            JLabel methodLabel = new JLabel("Select payment method");
            methodLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            methodLabel.setForeground(new Color(90, 105, 128));

            JComboBox<String> methodCombo = new JComboBox<>(options);
            methodCombo.setFont(new Font("Segoe UI", Font.PLAIN, 15));
            methodCombo.setBackground(new Color(244, 247, 252));
            methodCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));

            methodContent.add(methodTitle);
            methodContent.add(Box.createVerticalStrut(12));
            methodContent.add(amountLabel);
            methodContent.add(Box.createVerticalStrut(20));
            methodContent.add(methodLabel);
            methodContent.add(Box.createVerticalStrut(8));
            methodContent.add(methodCombo);

            final String[] chosenMethod = { null };

            JButton payContinue = new JButton("Continue to Payment");
            payContinue.setFont(new Font("Segoe UI", Font.BOLD, 14));
            payContinue.setBackground(new Color(42, 104, 210));
            payContinue.setForeground(Color.WHITE);
            payContinue.setFocusPainted(false);
            payContinue.setCursor(new Cursor(Cursor.HAND_CURSOR));
            payContinue.setBorder(
                    BorderFactory.createEmptyBorder(12, 20, 12, 20));

            JButton methodCancel = new JButton("Cancel");
            methodCancel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
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
            methodDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            methodDialog.setVisible(true);

            if (chosenMethod[0] == null) {
                return;
            }

            String paymentMethod = chosenMethod[0];

            double totalAmount = price * ticketCount;
            boolean paymentSuccess = processPayment(paymentMethod, totalAmount);

            if (!paymentSuccess) {
                JOptionPane.showMessageDialog(
                        null, "Payment was cancelled or unsuccessful.");
                return;
            }

            // ===== BOOK SEATS (critical section, protected by a mutex) =====
            // The availability check above ran BEFORE the user spent time on
            // the payment screens, so it may be stale by now. The seats are
            // re-checked and assigned atomically in bookSeatsAtomically().
            List<Integer> seatList = bookSeatsAtomically(con, Integer.parseInt(busId),
                    selectedDateStr, journeyDate, ticketCount, username,
                    passenger, paymentMethod);

            if (seatList == null) {
                JOptionPane.showMessageDialog(null,
                        "Sorry, these seats were just booked by another user.\n"
                                + "No seats were reserved for you and your payment of ₹ "
                                + String.format("%.2f", totalAmount)
                                + " will be refunded.\nPlease search again.");
                return;
            }

            // ===== ARRIVAL TIME =====
            Time arrivalTime = null;

            if (departureTime != null) {

                long depMillis = departureTime.getTime();
                long arrMillis = depMillis + (hours * 60 * 60 * 1000);

                arrivalTime = new Time(arrMillis);
            }

            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a");

            String depStr = (departureTime != null) ? sdf.format(departureTime) : "Not Set";
            String arrStr = (arrivalTime != null) ? sdf.format(arrivalTime) : "Not Set";

            // Show the e-ticket (has the "Save Ticket" button to save as PDF).
            StringBuilder seatText = new StringBuilder();
            for (int i = 0; i < seatList.size(); i++) {
                if (i > 0) {
                    seatText.append(", ");
                }
                seatText.append(seatList.get(i));
            }

            final String ticketSeats = seatText.toString();
            final String ticketDepart = depStr;
            final String ticketArrive = arrStr;

            javax.swing.SwingUtilities.invokeLater(() -> new ETicket(
                    busId, passenger, ticketSeats, selectedDateStr,
                    ticketDepart, ticketArrive, paymentMethod, totalAmount));

        } catch (Exception e) {

            try {
                if (con != null)
                    con.rollback();
            } catch (Exception ex) {
            }

            e.printStackTrace();

            JOptionPane.showMessageDialog(null, "Booking Error : " + e.getMessage());
        }
    }

    // ===== CRITICAL SECTION: ASSIGN SEATS =====
    //
    // OS concepts used here:
    // * Mutual exclusion - a ReentrantLock per (bus, date) so only one user
    // at a time runs "check free seats -> book them".
    // * Critical section - everything between lock() and unlock().
    // * Transaction/lock - SELECT ... FOR UPDATE row locks, so the same
    // protection also holds if the app runs on several
    // computers against the same MySQL database.
    // * Timeout - tryLock(15s) so a stuck user can never block
    // everybody else forever (deadlock avoidance).
    //
    // Lock order is always: Java mutex first, then DB row lock. Using one fixed
    // order everywhere prevents deadlock.
    //
    // Returns the booked seat numbers, or null if there are not enough free
    // seats any more. The caller's transaction (autocommit off) is committed or
    // rolled back here, and the commit happens BEFORE the mutex is released so
    // the next user always sees the seats we just took.
    static List<Integer> bookSeatsAtomically(Connection con, int busNo,
            String journeyDateText, Date journeyDate, int ticketCount,
            String username, String passenger, String paymentMethod)
            throws Exception {

        List<Integer> seatList = new ArrayList<>();
        String who = Thread.currentThread().getName();

        ReentrantLock seatLock = Seatlockmanager.lockFor(busNo, journeyDateText);

        System.out.println("[" + who + "] waiting for seat lock (bus "
                + busNo + ", " + journeyDateText + ")");

        if (!seatLock.tryLock(15, TimeUnit.SECONDS)) {
            con.rollback();
            throw new IllegalStateException(
                    "System is busy, please try again in a moment.");
        }

        try {
            System.out.println("[" + who + "] ENTERED critical section");

            // Row lock on the bus: serialises bookings across app instances.
            PreparedStatement busLock = con.prepareStatement(
                    "SELECT total_seats FROM bus WHERE bus_id=? FOR UPDATE");
            busLock.setInt(1, busNo);

            ResultSet busRs = busLock.executeQuery();
            int totalSeats = 0;
            if (busRs.next()) {
                totalSeats = busRs.getInt(1);
            }

            // Locking read (FOR UPDATE) always sees the latest committed data,
            // even though this transaction started earlier.
            PreparedStatement seatStmt = con.prepareStatement(
                    "SELECT seat_number FROM booking WHERE bus_id=? AND journey_date=? FOR UPDATE");
            seatStmt.setInt(1, busNo);
            seatStmt.setDate(2, journeyDate);

            ResultSet seatRs = seatStmt.executeQuery();
            List<Integer> bookedSeatNumbers = new ArrayList<>();
            while (seatRs.next()) {
                bookedSeatNumbers.add(seatRs.getInt("seat_number"));
            }

            // Re-check: somebody may have booked while this user was paying.
            if (totalSeats - bookedSeatNumbers.size() < ticketCount) {
                con.rollback();
                System.out.println("[" + who + "] not enough seats, leaving");
                return null;
            }

            int seat = 1;
            while (seatList.size() < ticketCount && seat <= totalSeats) {
                if (!bookedSeatNumbers.contains(seat)) {
                    PreparedStatement insert = con.prepareStatement(
                            "INSERT INTO booking(username,bus_id,passenger_name,seat_number,journey_date,payment_method) VALUES(?,?,?,?,?,?)");

                    insert.setString(1, username);
                    insert.setInt(2, busNo);
                    insert.setString(3, passenger);
                    insert.setInt(4, seat);
                    insert.setDate(5, journeyDate);
                    insert.setString(6, paymentMethod);
                    insert.executeUpdate();

                    seatList.add(seat);
                }
                seat++;
            }

            con.commit(); // commit while still holding the lock
            System.out.println("[" + who + "] booked seats " + seatList
                    + ", leaving critical section");
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
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(Color.WHITE);
        content.setBorder(
                BorderFactory.createEmptyBorder(25, 30, 15, 30));

        JLabel title = new JLabel("Complete Your Payment");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(new Color(15, 29, 55));

        JLabel methodLabel = new JLabel("Payment method: " + method);
        methodLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        methodLabel.setForeground(new Color(90, 105, 128));

        JLabel amountLabel = new JLabel(
                String.format("₹ %.2f", amount));
        amountLabel.setFont(new Font("Segoe UI", Font.BOLD, 30));
        amountLabel.setForeground(new Color(42, 104, 210));

        JLabel fieldLabel = new JLabel(
                method.equals("UPI") ? "UPI ID"
                        : method.equals("Credit Card")
                                ? "Credit card number"
                                : "Debit card number");

        fieldLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        fieldLabel.setForeground(new Color(40, 52, 72));

        JTextField paymentField = new JTextField();
        paymentField.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        paymentField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        paymentField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(210, 220, 234)),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)));

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
        payBtn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        payBtn.setForeground(Color.WHITE);
        payBtn.setBackground(new Color(42, 104, 210));
        payBtn.setFocusPainted(false);
        payBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        payBtn.setBorder(BorderFactory.createEmptyBorder(13, 25, 13, 25));

        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cancelBtn.setFocusPainted(false);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 15));
        buttons.setBackground(Color.WHITE);
        buttons.add(cancelBtn);
        buttons.add(payBtn);

        payBtn.addActionListener(e -> {
            String input = paymentField.getText().trim();

            if (input.isEmpty()) {
                JOptionPane.showMessageDialog(
                        dialog, "Please enter your payment details.");
                return;
            }

            // Demonstration only: this does not contact a payment gateway.
            int answer = JOptionPane.showConfirmDialog(
                    dialog,
                    "Confirm demo payment of ₹ "
                            + String.format("%.2f", amount) + "?",
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
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setVisible(true);

        return success[0];
    }
}
