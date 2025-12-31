package application.DAOImpl;

import application.DAO.LabelDAO;
import application.models.Label;
import application.utils.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class LabelDAOImpl implements LabelDAO {
    @Override
    public List<Label> getAllLabels(int userId) {
        List<Label> labels = new ArrayList<>();
        String query = "SELECT * FROM labels WHERE user_id=?";
        try(Connection conn = DatabaseConnection.getConnection()) {
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                labels.add(new Label(
                        rs.getInt("id"),
                        rs.getInt("user_id"),
                        rs.getString("title"),
                        rs.getString("color_code")
                ));
            }
        }catch (SQLException e){
            e.printStackTrace();
            System.err.println("ERROR: Method getAllLabels  failed!!!");
        }
        return labels;
    }

    @Override
    public boolean addLabel(Label label, int userId) {
        String query = "INSERT INTO labels (user_id, title, color_code) VALUES (?, ?, ?)";
        try(Connection conn=DatabaseConnection.getConnection()){
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1,userId);
            pstmt.setString(2, label.getTitle());
            pstmt.setString(3, label.getColorCode());
            return pstmt.executeUpdate() >0;
        } catch (SQLException e){
            e.printStackTrace();
            System.err.println("ERROR: Method addLabel  failed!!!");
            return false;
        }
    }

    @Override
    public boolean deleteLabel(int id, int userId) {
        String query = "DELETE FROM labels WHERE id=? AND user_id=?";
        try(Connection conn =DatabaseConnection.getConnection()){
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, id);
            pstmt.setInt(2, userId);
            return pstmt.executeUpdate()>0;
        } catch (SQLException e){
            e.printStackTrace();
            System.err.println("ERROR: Method deleteLabel  failed!!!");
            return false;
        }
    }
}
