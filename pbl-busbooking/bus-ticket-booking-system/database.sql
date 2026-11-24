CREATE DATABASE IF NOT EXISTS tickets
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE tickets;

CREATE TABLE IF NOT EXISTS users (
    id INT NOT NULL AUTO_INCREMENT,
    username VARCHAR(100) NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_users_username (username)
);

CREATE TABLE IF NOT EXISTS bus (
    bus_id INT NOT NULL AUTO_INCREMENT,
    bus_name VARCHAR(150) NOT NULL,
    source VARCHAR(100) NOT NULL,
    destination VARCHAR(100) NOT NULL,
    departure_time TIME NOT NULL,
    journey_hours INT NOT NULL,
    total_seats INT NOT NULL,
    booked_seats INT NOT NULL DEFAULT 0,
    price DECIMAL(10, 2) NOT NULL,
    PRIMARY KEY (bus_id)
);

CREATE TABLE IF NOT EXISTS booking (
    booking_id INT NOT NULL AUTO_INCREMENT,
    username VARCHAR(100) NOT NULL,
    bus_id INT NOT NULL,
    passenger_name VARCHAR(150) NOT NULL,
    seat_number INT NOT NULL,
    booking_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    journey_date DATE NOT NULL,
    payment_method VARCHAR(50) NOT NULL,
    PRIMARY KEY (booking_id),
    UNIQUE KEY uq_booking_seat (bus_id, journey_date, seat_number),
    KEY idx_booking_username (username)
);
