package bus;

import db.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

public class Seatlockmanager {

    private static final ConcurrentHashMap<String, ReentrantLock> LOCKS = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, ReentrantLock> SEAT_LOCKS = new ConcurrentHashMap<>();

    public static ReentrantLock lockFor(int busNo, String journeyDateText) {
        return LOCKS.computeIfAbsent(busNo + "_" + journeyDateText, k -> new ReentrantLock());
    }

    public static ReentrantLock lockForSeat(int busNo, String journeyDateText, int seatNo) {
        String key = busNo + "_" + journeyDateText + "_seat_" + seatNo;
        return SEAT_LOCKS.computeIfAbsent(key, k -> new ReentrantLock());
    }

    public static class SeatItem {
        public final int seatNumber;
        public final String status; // "AVAILABLE", "WAITING", "BOOKED"

        public SeatItem(int seatNumber, String status) {
            this.seatNumber = seatNumber;
            this.status = status;
        }
    }

    public static List<SeatItem> getSeats(int busId) {
        List<SeatItem> list = new ArrayList<>();
        try (Connection con = DBConnection.getConnection()) {
            ensureSeatsCreated(con, busId);

            String sql = "SELECT seat_number, status FROM bus_seats WHERE bus_id = ? ORDER BY seat_number ASC";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, busId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        list.add(new SeatItem(rs.getInt("seat_number"), rs.getString("status")));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // Green -> Orange when user clicks an available seat
    public static boolean setWaiting(int busId, int seatNumber, String username) {
        String sql = "UPDATE bus_seats SET status = 'WAITING', locked_by = ? " +
                "WHERE bus_id = ? AND seat_number = ? AND status = 'AVAILABLE'";
        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setInt(2, busId);
            ps.setInt(3, seatNumber);
            return ps.executeUpdate() == 1;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Orange -> Green if user cancels or closes the ticket form
    public static void releaseWaiting(int busId, int seatNumber) {
        String sql = "UPDATE bus_seats SET status = 'AVAILABLE', locked_by = NULL " +
                "WHERE bus_id = ? AND seat_number = ? AND status = 'WAITING'";
        try (Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, busId);
            ps.setInt(2, seatNumber);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Inserts into booking, marks seat BOOKED (Red), and decrements bus.seats
    // atomically
    public static boolean completeBooking(String username, int busId, String passengerName,
            int seatNumber, Date journeyDate, String paymentMethod) {
        String insertBooking = "INSERT INTO booking (username, bus_id, passenger_name, seat_number, journey_date, payment_method) "
                +
                "VALUES (?, ?, ?, ?, ?, ?)";
        String markBooked = "UPDATE bus_seats SET status = 'BOOKED', locked_by = NULL " +
                "WHERE bus_id = ? AND seat_number = ? AND status IN ('WAITING', 'AVAILABLE')";
        String decrementBusSeats = "UPDATE bus SET seats = GREATEST(seats - 1, 0) WHERE bus_id = ?";

        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try {
                try (PreparedStatement psBook = con.prepareStatement(insertBooking)) {
                    psBook.setString(1, username);
                    psBook.setInt(2, busId);
                    psBook.setString(3, passengerName);
                    psBook.setInt(4, seatNumber);
                    psBook.setDate(5, journeyDate);
                    psBook.setString(6, paymentMethod);
                    psBook.executeUpdate();
                }

                int updatedSeats;
                try (PreparedStatement psSeat = con.prepareStatement(markBooked)) {
                    psSeat.setInt(1, busId);
                    psSeat.setInt(2, seatNumber);
                    updatedSeats = psSeat.executeUpdate();
                }

                if (updatedSeats != 1) {
                    con.rollback();
                    return false;
                }

                try (PreparedStatement psBus = con.prepareStatement(decrementBusSeats)) {
                    psBus.setInt(1, busId);
                    psBus.executeUpdate();
                }

                con.commit();
                return true;
            } catch (Exception ex) {
                con.rollback();
                ex.printStackTrace();
                return false;
            } finally {
                con.setAutoCommit(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static void ensureSeatsCreated(Connection con, int busId) throws SQLException {
        String countSql = "SELECT COUNT(*) FROM bus_seats WHERE bus_id = ?";
        try (PreparedStatement countPs = con.prepareStatement(countSql)) {
            countPs.setInt(1, busId);
            try (ResultSet rs = countPs.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0)
                    return;
            }
        }

        int totalSeats = 20;
        String busSql = "SELECT seats FROM bus WHERE bus_id = ?";
        try (PreparedStatement busPs = con.prepareStatement(busSql)) {
            busPs.setInt(1, busId);
            try (ResultSet rs = busPs.executeQuery()) {
                if (rs.next()) {
                    totalSeats = Math.max(1, rs.getInt("seats"));
                }
            }
        }

        String insertSql = "INSERT IGNORE INTO bus_seats (bus_id, seat_number, status) VALUES (?, ?, 'AVAILABLE')";
        try (PreparedStatement insPs = con.prepareStatement(insertSql)) {
            for (int i = 1; i <= totalSeats; i++) {
                insPs.setInt(1, busId);
                insPs.setInt(2, i);
                insPs.addBatch();
            }
            insPs.executeBatch();
        }
    }
}