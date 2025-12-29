package application.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    private static final String URL = "jdbc:mysql://localhost:3306/taskflow_db";
    private static final String USER = "root";
    private static final String PASSWORD = "qwertyasdfgh@1234";

    public static Connection getConnection() {
        Connection conn = null;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("INFORM: Connected to database successfully!!!");
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            System.err.println("ERROR: Connection Failed! Check output console!!!");
        }
        return conn;
    }

}
