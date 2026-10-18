USE tickets;

INSERT IGNORE INTO users (username, password, role)
VALUES
    ('demo_user', 'demo123', 'user'),
    ('demo_admin', 'admin123', 'admin');

INSERT INTO bus (
    bus_name, source, destination, departure_time,
    journey_hours, total_seats, booked_seats, price
)
SELECT 'City Express', 'New York', 'Boston', '08:00:00', 4, 40, 0, 29.99
WHERE NOT EXISTS (
    SELECT 1 FROM bus
    WHERE bus_name = 'City Express'
      AND source = 'New York'
      AND destination = 'Boston'
);

INSERT INTO bus (
    bus_name, source, destination, departure_time,
    journey_hours, total_seats, booked_seats, price
)
SELECT 'Coastal Rider', 'Boston', 'New York', '14:30:00', 4, 36, 0, 34.50
WHERE NOT EXISTS (
    SELECT 1 FROM bus
    WHERE bus_name = 'Coastal Rider'
      AND source = 'Boston'
      AND destination = 'New York'
);

INSERT INTO bus (
    bus_name, source, destination, departure_time,
    journey_hours, total_seats, booked_seats, price
)
SELECT 'Mountain Line', 'Denver', 'Colorado Springs', '09:15:00', 2, 32, 0, 18.00
WHERE NOT EXISTS (
    SELECT 1 FROM bus
    WHERE bus_name = 'Mountain Line'
      AND source = 'Denver'
      AND destination = 'Colorado Springs'
);

INSERT INTO bus (
    bus_name, source, destination, departure_time,
    journey_hours, total_seats, booked_seats, price
)
SELECT 'Lake Connector', 'Chicago', 'Milwaukee', '11:00:00', 2, 40, 0, 16.75
WHERE NOT EXISTS (
    SELECT 1 FROM bus
    WHERE bus_name = 'Lake Connector'
      AND source = 'Chicago'
      AND destination = 'Milwaukee'
);

INSERT INTO bus (
    bus_name, source, destination, departure_time,
    journey_hours, total_seats, booked_seats, price
)
SELECT 'Sunrise Coach', 'San Francisco', 'Los Angeles', '07:45:00', 7, 44, 0, 45.00
WHERE NOT EXISTS (
    SELECT 1 FROM bus
    WHERE bus_name = 'Sunrise Coach'
      AND source = 'San Francisco'
      AND destination = 'Los Angeles'
);
