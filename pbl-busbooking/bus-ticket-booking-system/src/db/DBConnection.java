package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL JDBC driver is not on the classpath.", e);
        }

        String url = System.getenv().getOrDefault(
                "BUS_DB_URL", "jdbc:mysql://localhost:3306/tickets");
        String username = System.getenv().getOrDefault("BUS_DB_USER", "root");
        String password = System.getenv().getOrDefault("BUS_DB_PASSWORD", "root123");

        return DriverManager.getConnection(url, username, password);
    }
}
