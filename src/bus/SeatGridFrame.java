
package bus;

import db.DBConnection;
import java.awt.*;
import java.sql.*;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.swing.*;

public class SeatGridFrame extends JFrame {

    private static final ConcurrentHashMap<String, Set<Integer>> WAITING_SEATS = new ConcurrentHashMap<>();

    private final String username;
    private final String busId;
    private final String busSummary;
    private final JFrame searchFrame;
    private final JComboBox<String> dateBox;
    private final JPanel seatGrid;

    private int selectedSeat = -1;
    private JLabel selectedSeatLabel;

    public SeatGridFrame(String username, int busId,
            String busSummary, JFrame searchFrame) {

        this.username = username;
        this.busId = String.valueOf(busId);
        this.busSummary = busSummary;
        this.searchFrame = searchFrame;

        setTitle("Select Seat - " + busSummary);
        setSize(700, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(Color.WHITE);

        // ================= TOP PANEL =================

        JPanel topPanel = new JPanel(new BorderLayout(8, 8));
        topPanel.setBackground(Color.WHITE);
        topPanel.setBorder(
                BorderFactory.createEmptyBorder(10, 15, 5, 15));

        JLabel title = new JLabel("Select Your Seat");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(new Color(15, 29, 55));

        String[] dates = new String[7];
        LocalDate today = LocalDate.now();

        for (int i = 0; i < 7; i++) {
            dates[i] = today.plusDays(i).toString();
        }

        dateBox = new JComboBox<>(dates);
        dateBox.addActionListener(e -> {
            selectedSeat = -1;
            selectedSeatLabel.setText("No seat selected");
            refreshGrid();
        });

        JPanel dateRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        dateRow.setBackground(Color.WHITE);
        dateRow.add(new JLabel("Journey Date:"));
        dateRow.add(dateBox);

        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(Color.WHITE);
        titlePanel.add(title, BorderLayout.NORTH);
        titlePanel.add(dateRow, BorderLayout.SOUTH);

        topPanel.add(titlePanel, BorderLayout.NORTH);

        // ================= SEAT LEGEND =================

        JPanel legend = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 8));
        legend.setBackground(Color.WHITE);

        legend.add(makeBadge("Available", new Color(0, 180, 100)));
        legend.add(makeBadge("Waiting", new Color(255, 140, 0)));
        legend.add(makeBadge("Booked", new Color(220, 50, 50)));
        legend.add(makeBadge("Selected", new Color(42, 104, 210)));

        topPanel.add(legend, BorderLayout.CENTER);

        add(topPanel, BorderLayout.NORTH);

        // ================= SEAT GRID =================

        seatGrid = new JPanel(new GridLayout(0, 5, 12, 12));
        seatGrid.setBackground(Color.WHITE);
        seatGrid.setBorder(
                BorderFactory.createEmptyBorder(20, 30, 20, 30));

        JScrollPane scrollPane = new JScrollPane(seatGrid);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        add(scrollPane, BorderLayout.CENTER);

        // ================= BOTTOM PANEL =================

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 10));
        bottom.setBackground(Color.WHITE);

        selectedSeatLabel = new JLabel("No seat selected");
        selectedSeatLabel.setFont(
                new Font("Segoe UI", Font.BOLD, 14));
        selectedSeatLabel.setForeground(new Color(15, 29, 55));

        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> refreshGrid());

        JButton backBtn = new JButton("Back");
        backBtn.addActionListener(e -> {
            dispose();

            if (searchFrame != null) {
                searchFrame.setVisible(true);
            }
        });

        JButton continueBtn = new JButton("Continue");
        continueBtn.setBackground(new Color(42, 104, 210));
        continueBtn.setForeground(Color.WHITE);
        continueBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));

        continueBtn.addActionListener(e -> continueBooking());

        bottom.add(selectedSeatLabel);
        bottom.add(refreshBtn);
        bottom.add(backBtn);
        bottom.add(continueBtn);

        add(bottom, BorderLayout.SOUTH);

        refreshGrid();
    }

    // ================= LEGEND BADGE =================

    private JLabel makeBadge(String text, Color color) {

        JLabel lbl = new JLabel(text, SwingConstants.CENTER);
        lbl.setOpaque(true);
        lbl.setBackground(color);
        lbl.setForeground(Color.WHITE);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setPreferredSize(new Dimension(110, 28));

        return lbl;
    }

    // ================= REFRESH SEAT GRID =================

    public void refreshGrid() {

        seatGrid.removeAll();

        int id = Integer.parseInt(busId);
        String dateStr = (String) dateBox.getSelectedItem();

        Date sqlDate = Date.valueOf(dateStr);

        int totalSeats = fetchTotalSeats(id);

        Set<Integer> bookedSeats = fetchBookedSeats(id, sqlDate);

        Set<Integer> waitingSeats = WAITING_SEATS.computeIfAbsent(
                id + "_" + dateStr,
                k -> ConcurrentHashMap.newKeySet());

        // Driver area
        JLabel driverLabel = new JLabel("DRIVER", SwingConstants.CENTER);
        driverLabel.setOpaque(true);
        driverLabel.setBackground(new Color(230, 235, 243));
        driverLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));

        seatGrid.add(driverLabel);
        seatGrid.add(new JLabel(""));
        seatGrid.add(new JLabel(""));
        seatGrid.add(new JLabel(""));
        seatGrid.add(new JLabel(""));

        // Seats: 2 + aisle + 2
        for (int seatNo = 1; seatNo <= totalSeats; seatNo++) {

            // Add aisle after two seats in each row
            if ((seatNo - 1) % 4 == 2) {
                seatGrid.add(new JLabel(""));
            }

            JButton btn = new JButton("S" + seatNo);

            btn.setOpaque(true);
            btn.setBorderPainted(false);
            btn.setForeground(Color.WHITE);
            btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
            btn.setPreferredSize(new Dimension(75, 50));

            final int currentSeat = seatNo;

            if (bookedSeats.contains(currentSeat)) {

                btn.setBackground(new Color(220, 50, 50));
                btn.setEnabled(false);
                btn.setToolTipText("Booked");

            } else if (waitingSeats.contains(currentSeat)) {

                btn.setBackground(new Color(255, 140, 0));
                btn.setEnabled(false);
                btn.setToolTipText("Waiting");

            } else if (selectedSeat == currentSeat) {

                btn.setBackground(new Color(42, 104, 210));
                btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

                btn.addActionListener(e -> selectSeat(currentSeat));

            } else {

                btn.setBackground(new Color(0, 180, 100));
                btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

                btn.addActionListener(e -> selectSeat(currentSeat));
            }

            seatGrid.add(btn);
        }

        seatGrid.revalidate();
        seatGrid.repaint();
    }

    // ================= SELECT SEAT =================

    private void selectSeat(int seatNumber) {

        selectedSeat = seatNumber;

        selectedSeatLabel.setText(
                "Selected Seat: S" + seatNumber);

        refreshGrid();
    }

    // ================= CONTINUE BOOKING =================

    private void continueBooking() {

        if (selectedSeat == -1) {
            JOptionPane.showMessageDialog(
                    this, "Please select a seat first.");
            return;
        }

        int id = Integer.parseInt(busId);

        String dateStr = (String) dateBox.getSelectedItem();

        Set<Integer> waitingSeats = WAITING_SEATS.computeIfAbsent(
                id + "_" + dateStr,
                k -> ConcurrentHashMap.newKeySet());

        // Temporarily mark seat as waiting
        waitingSeats.add(selectedSeat);

        refreshGrid();

        String passenger = JOptionPane.showInputDialog(
                this,
                "Enter passenger name for Seat S" + selectedSeat + ":");

        if (passenger == null || passenger.trim().isEmpty()) {
            waitingSeats.remove(selectedSeat);
            refreshGrid();
            return;
        }

        // Existing booking logic
        new BookTicket(
                username,
                busId,
                passenger.trim(),
                1,
                selectedSeat,
                dateStr);

        waitingSeats.remove(selectedSeat);
        refreshGrid();
    }

    // ================= DATABASE METHODS =================

    private int fetchTotalSeats(int id) {

        String sql = "SELECT total_seats FROM bus WHERE bus_id=?";

        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    return Math.max(0, rs.getInt("total_seats"));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }

    private Set<Integer> fetchBookedSeats(int id, Date sqlDate) {

        Set<Integer> booked = new HashSet<>();

        String sql = "SELECT seat_number FROM booking WHERE bus_id=? AND journey_date=?";

        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.setDate(2, sqlDate);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    booked.add(rs.getInt("seat_number"));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return booked;
    }
}
