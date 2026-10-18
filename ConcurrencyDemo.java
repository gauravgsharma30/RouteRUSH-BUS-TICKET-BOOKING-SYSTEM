package bus;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/**
 * OS concepts demo: race condition, critical section, mutual exclusion
 * and multithreading.
 *
 * Many "users" (threads) try to book seats on one bus at the same time.
 * - Without a lock : check-then-book is NOT atomic -> two users can get
 * the same seat (race condition / double booking).
 * - With a lock : the check-then-book block is a critical section
 * protected by a ReentrantLock -> no double booking.
 */
public class ConcurrencyDemo extends JFrame {

    // ------------------------------------------------------------------
    // Core simulation (no UI) - can also be run from main() in a console.
    // ------------------------------------------------------------------

    public static class Result {
        public final boolean locked;
        public final int users;
        public final int seats;
        public final int successfulBookings; // users who were told "booked"
        public final int seatsActuallyTaken; // distinct seats that are occupied
        public final int doubleBookings; // successfulBookings - seatsActuallyTaken
        public final long millis;

        Result(boolean locked, int users, int seats, int successfulBookings,
                int seatsActuallyTaken, long millis) {
            this.locked = locked;
            this.users = users;
            this.seats = seats;
            this.successfulBookings = successfulBookings;
            this.seatsActuallyTaken = seatsActuallyTaken;
            this.doubleBookings = successfulBookings - seatsActuallyTaken;
            this.millis = millis;
        }
    }

    /** One bus: seatOwner[i] == 0 means seat i is free, otherwise the user id. */
    private static class Bus {
        final int[] seatOwner;
        final ReentrantLock lock = new ReentrantLock();

        Bus(int seats) {
            seatOwner = new int[seats];
        }

        // UNSAFE: check and update are separate steps (not atomic).
        boolean bookUnsafe(int seat, int userId) {
            if (seatOwner[seat] == 0) { // 1. check
                simulateDatabaseDelay(); // other threads run here!
                seatOwner[seat] = userId; // 2. act (may overwrite someone)
                return true;
            }
            return false;
        }

        // SAFE: check + update happen as one critical section.
        boolean bookSafe(int seat, int userId) {
            lock.lock(); // entry section
            try {
                if (seatOwner[seat] == 0) { // critical section
                    simulateDatabaseDelay();
                    seatOwner[seat] = userId;
                    return true;
                }
                return false;
            } finally {
                lock.unlock(); // exit section
            }
        }

        // Stands in for the time a real DB query / payment takes.
        private void simulateDatabaseDelay() {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        int occupiedSeats() {
            int count = 0;
            for (int owner : seatOwner) {
                if (owner != 0) {
                    count++;
                }
            }
            return count;
        }
    }

    public static Result simulate(boolean useLock, int users, int seats) {
        final Bus bus = new Bus(seats);
        final AtomicInteger success = new AtomicInteger();
        final CountDownLatch startGate = new CountDownLatch(1);
        final CountDownLatch done = new CountDownLatch(users);
        final ExecutorService pool = Executors.newFixedThreadPool(users);
        final Random random = new Random();

        for (int u = 1; u <= users; u++) {
            final int userId = u;
            final int wantedSeat = random.nextInt(seats);

            pool.execute(() -> {
                try {
                    startGate.await(); // all users start together
                    boolean ok = useLock
                            ? bus.bookSafe(wantedSeat, userId)
                            : bus.bookUnsafe(wantedSeat, userId);
                    if (ok) {
                        success.incrementAndGet();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    done.countDown();
                }
            });
        }

        long start = System.nanoTime();
        startGate.countDown(); // go!

        try {
            done.await(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        long millis = (System.nanoTime() - start) / 1_000_000;
        pool.shutdownNow();

        return new Result(useLock, users, seats, success.get(),
                bus.occupiedSeats(), millis);
    }

    // ------------------------------------------------------------------
    // Window
    // ------------------------------------------------------------------

    private final JSpinner usersSpinner = new JSpinner(new SpinnerNumberModel(50, 2, 500, 10));
    private final JSpinner seatsSpinner = new JSpinner(new SpinnerNumberModel(20, 1, 200, 5));
    private final JTextArea log = new JTextArea();
    private final JButton unsafeButton = new JButton("Run WITHOUT lock");
    private final JButton safeButton = new JButton("Run WITH lock");
    private final JLabel status = new JLabel("Ready");

    public ConcurrencyDemo() {
        setTitle("BusGo | Race Condition & Multithreading Demo");
        setSize(720, 560);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBorder(new EmptyBorder(14, 16, 14, 16));
        root.setBackground(Color.WHITE);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        controls.setBackground(Color.WHITE);
        controls.add(new JLabel("Users (threads):"));
        controls.add(usersSpinner);
        controls.add(new JLabel("Seats:"));
        controls.add(seatsSpinner);
        controls.add(unsafeButton);
        controls.add(safeButton);

        log.setEditable(false);
        log.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        log.setText("Each user is a separate thread trying to book a random seat\n"
                + "at the same moment. Run both modes and compare.\n\n");

        root.add(controls, BorderLayout.NORTH);
        root.add(new JScrollPane(log), BorderLayout.CENTER);
        root.add(status, BorderLayout.SOUTH);
        setContentPane(root);

        unsafeButton.addActionListener(e -> run(false));
        safeButton.addActionListener(e -> run(true));

        setVisible(true);
    }

    // Runs the simulation on a background thread so the window never freezes.
    private void run(boolean useLock) {
        final int users = (Integer) usersSpinner.getValue();
        final int seats = (Integer) seatsSpinner.getValue();

        unsafeButton.setEnabled(false);
        safeButton.setEnabled(false);
        status.setText("Running " + users + " booking threads "
                + "(UI stays responsive on its own thread)...");

        new SwingWorker<Result, Void>() {
            @Override
            protected Result doInBackground() {
                return simulate(useLock, users, seats);
            }

            @Override
            protected void done() {
                try {
                    show(get());
                } catch (Exception ex) {
                    log.append("Error: " + ex.getMessage() + "\n");
                } finally {
                    unsafeButton.setEnabled(true);
                    safeButton.setEnabled(true);
                    status.setText("Ready");
                }
            }
        }.execute();
    }

    private void show(Result r) {
        log.append(r.locked ? "=== WITH lock (mutual exclusion) ===\n"
                : "=== WITHOUT lock (race condition) ===\n");
        log.append("Users (threads)            : " + r.users + "\n");
        log.append("Seats on bus               : " + r.seats + "\n");
        log.append("Users told 'Booked'        : " + r.successfulBookings + "\n");
        log.append("Seats actually occupied    : " + r.seatsActuallyTaken + "\n");
        log.append("DOUBLE BOOKINGS            : " + r.doubleBookings + "\n");
        log.append("Time taken                 : " + r.millis + " ms\n");
        log.append(r.doubleBookings > 0
                ? "-> RACE CONDITION: several users got the same seat!\n\n"
                : "-> Correct: every seat was given to exactly one user.\n\n");
        log.setCaretPosition(log.getDocument().getLength());
    }

    public static void main(String[] args) {
        // Console mode (no window), handy for screenshots in the report.
        for (boolean lock : new boolean[] { false, true }) {
            Result r = simulate(lock, 50, 20);
            System.out.println((lock ? "WITH lock    " : "WITHOUT lock ")
                    + " booked=" + r.successfulBookings
                    + " occupied=" + r.seatsActuallyTaken
                    + " doubleBookings=" + r.doubleBookings);
        }
        SwingUtilities.invokeLater(ConcurrencyDemo::new);
    }
}