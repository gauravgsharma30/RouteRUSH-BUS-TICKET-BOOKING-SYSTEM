🚌 Bus Ticket Booking System (Java)

A Java-based Bus Ticket Booking System that allows users to search buses, book tickets, and manage bookings through a simple GUI. The system also includes an Admin module to manage buses and monitor the booking process.

This project is built using Java Swing for the UI and MySQL for database storage.

📌 Features
👤 User Module

User Registration and Login

Search available buses

View bus details

Select journey date (up to 7 days in advance)

Book bus tickets

Online payment simulation

View booking confirmation with seat numbers

🛠 Admin Module

Admin Signup and Login

Add new buses

View bus list

Delete buses from the system

Manage bus information

💾 Database Setup

The application uses the MySQL `tickets` database and the `users`, `bus`,
and `booking` tables. The included `database.sql` creates them without
overwriting existing data.
⚙️ Technologies Used

Java

Java Swing (GUI)

JDBC

▶️ How to Run the Project

On Windows, double-click `Run-BusBooking.bat`. It prompts for the MySQL
`root` password without displaying it and starts the app with the bundled JDK
and JDBC driver. The password is not saved in the project.

Before the first launch, create the database and tables by opening
`database.sql` in MySQL Workbench and executing it.

The SQL script creates the `tickets` database and the `users`, `bus`, and
`booking` tables without overwriting existing data. To load demo accounts and
sample bus routes, execute `sample-data.sql` in MySQL Workbench after the
database setup. You can then sign in with `demo_user` / `demo123` or
`demo_admin` / `admin123`. These are demonstration credentials only.

Clone the repository
https://github.com/Satya1296/bus-ticket-booking-system

Import the project into VS Code / any Java IDE

Configure the database connection using environment variables:

| Variable | Default | Description |
|---|---|---|
| `BUS_DB_URL` | `jdbc:mysql://localhost:3306/tickets` | MySQL JDBC URL |
| `BUS_DB_USER` | `root` | MySQL username |
| `BUS_DB_PASSWORD` | `root123` | MySQL password |

Set `BUS_DB_PASSWORD` to the password configured for your MySQL account before starting the app. On Windows PowerShell, for example:

```powershell
$env:BUS_DB_PASSWORD = "your-MySQL-password"
```

Run the project starting from the Login class.

MySQL

VS Code

🎓 Academic Purpose

This project was developed as part of a Java Mini Project to demonstrate:

GUI development

Database connectivity

CRUD operations

Booking system logic

👩‍💻 Author

Asritha

Java Project – Bus Ticket Booking System
