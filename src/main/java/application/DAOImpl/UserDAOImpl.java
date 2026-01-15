package application.DAOImpl;

import application.DAO.UserDAO;
import application.models.User;
import application.utils.DatabaseConnection;
import application.utils.SecurityUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDAOImpl implements UserDAO {

    @Override
    public User login(String username, String password) {
        String hashedPassword = SecurityUtils.hashPassword(password);
        return loginWithHash(username, hashedPassword);
    }

    @Override
    public User loginWithHash(String username, String password) {
        String query = "SELECT * FROM users WHERE username = ? AND password = ?";
        try (Connection conn = DatabaseConnection.getConnection()) {
            PreparedStatement pstmt = conn.prepareStatement(query);

            pstmt.setString(1, username);
            pstmt.setString(2, password);

            ResultSet rs = pstmt.executeQuery();
            if(rs.next()) {
                return new User (
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getTimestamp("created_at").toLocalDateTime()
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("ERROR: Connection failed in method UserDAOImpl!!");
        }
        return null;
    }

    @Override
    public boolean register(User user) {
        String query = "INSERT INTO users (username, password) VALUES (?, ?)";
        String hashedPassword = SecurityUtils.hashPassword(user.getPassword());
        try (Connection conn =DatabaseConnection.getConnection()) {
            PreparedStatement pstmt = conn.prepareStatement(query);

            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, hashedPassword);

            int rowsAffected = pstmt.executeUpdate();
            return (rowsAffected > 0);
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("ERROR: Connection failed in method UserDAOImpl!!");
            return false;
        }
    }

    @Override
    public boolean checkUsernameExists(String username) {
        String query = "SELECT id FROM users WHERE username = ?";
        try (Connection conn = DatabaseConnection.getConnection()) {
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("ERROR: Connection failed in method UserDAOImpl!!");
            return false;
        }
    }
}
