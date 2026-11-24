package bus;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * OS concept: mutual exclusion (mutex).
 *
 * Hands out ONE lock per (bus, journey date). Anyone who wants to assign
 * seats on that bus for that date must hold the lock first, so two users can
 * never run the "check free seats -> book them" critical section at the same
 * time for the same bus and date. Different buses/dates use different locks,
 * so they never block each other.
 *
 * The locks are "fair": waiting threads are served in arrival order (FCFS).
 */
public final class Seatlockmanager {

    private static final ConcurrentHashMap<String, ReentrantLock> LOCKS = new ConcurrentHashMap<>();

    private Seatlockmanager() {
    }

    public static ReentrantLock lockFor(int busId, String journeyDate) {
        return LOCKS.computeIfAbsent(busId + "|" + journeyDate,
                key -> new ReentrantLock(true));
    }
}