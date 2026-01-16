package application.DAOImpl;

import application.DAO.TaskDAO;
import application.models.Label;
import application.models.Task;
import application.utils.DatabaseConnection;
import javafx.scene.chart.PieChart;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TaskDAOImpl implements TaskDAO {

    // === PHUONG THUC CHINH ===
    @Override
    public List<Task> getAllTasks(int userId) {
        List<Task> taskList=new ArrayList<>();
        String query = "SELECT * FROM tasks WHERE user_id = ? ORDER BY due_date ASC";
        try(Connection conn = DatabaseConnection.getConnection()){
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            while(rs.next()){
                Task task = mapResultSetToTask(rs);
                task.setLabels(getLabelsForTask(task.getId(), conn));
                taskList.add(task);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("ERROR: Methods getAllTasks failed!!");
        }
        return taskList;
    }

    @Override
    public boolean addTask(Task task, int userId) {
        String query1 = "INSERT INTO tasks (title, description, due_date, is_completed, completed_at, priority, user_id) VALUES (?,?,?,?,?,?,?)";
        String query2 = "INSERT INTO task_labels (task_id, label_id) VALUES (?,?)";

        try(Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try(PreparedStatement pstmt = conn.prepareStatement(query1, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setString(1, task.getTitle());
                pstmt.setString(2, task.getDescription());
                pstmt.setTimestamp(3, task.getDueDate() != null ? Timestamp.valueOf(task.getDueDate()) : null);
                pstmt.setBoolean(4, task.isCompleted());
                pstmt.setTimestamp(5, task.getCompletedAt() != null ? Timestamp.valueOf(task.getCompletedAt()) : null);
                pstmt.setInt(6, task.getPriority());
                pstmt.setInt(7, userId);

                int aRows = pstmt.executeUpdate();
                if (aRows == 0) throw new SQLException("Failed to add task");

                try(ResultSet rs = pstmt.getGeneratedKeys()) {
                    if(rs.next()) {
                        task.setId(rs.getInt(1));
                    }
                }

                if(task.getLabels() != null && !task.getLabels().isEmpty()) {
                    try( PreparedStatement labelStmt =conn.prepareStatement(query2)) {
                       for(Label label : task.getLabels()) {
                           labelStmt.setInt(1, task.getId());
                           labelStmt.setInt(2, label.getId());
                           labelStmt.addBatch();
                       }
                       labelStmt.executeBatch();
                    }
                }
                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                e.printStackTrace();
                System.err.println("ERROR: Method addTask failed!!!");
                return false;
            } finally {
                conn.setAutoCommit(true);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("ERROR: Method addTask failed!!");
            return false;
        }
    }

    @Override
    public boolean deleteTask(int id, int userId) {
        String query = "DELETE FROM tasks s WHERE id = ? AND user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection()) {
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, id);
            pstmt.setInt(2, userId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("ERROR: Method deleteTask failed!!!");
            return false;
        }
    }

    @Override
    public boolean updateTask(Task task, int userId) {
        String query1 = "UPDATE tasks SET title = ?, description = ?, due_date = ?, is_completed = ?, completed_at=?, priority=? WHERE id = ? AND user_id =?";
        String query2 = "DELETE FROM task_labels WHERE task_id = ? ";
        String query3 = "INSERT INTO task_labels (task_id, label_id) VALUES (?,?)";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);

            try(PreparedStatement pstmt = conn.prepareStatement(query1)) {
                pstmt.setString(1, task.getTitle());
                pstmt.setString(2, task.getDescription());
                pstmt.setTimestamp(3, task.getDueDate() != null ? Timestamp.valueOf(task.getDueDate()) : null);
                pstmt.setBoolean(4, task.isCompleted());
                pstmt.setTimestamp(5, task.getDueDate() != null ? Timestamp.valueOf(task.getDueDate()) : null);
                pstmt.setInt(6, task.getPriority());
                pstmt.setInt(7, task.getId());
                pstmt.setInt(8, userId);
                pstmt.executeUpdate();
            }

            try(PreparedStatement delPstmt = conn.prepareStatement(query2)) {
                delPstmt.setInt(1, task.getId());
                delPstmt.executeUpdate();
            }

            if (task.getLabels() != null && !task.getLabels().isEmpty()) {
                try(PreparedStatement insPstmt = conn.prepareStatement(query3)) {
                    for(Label label : task.getLabels()) {
                        insPstmt.setInt(1, task.getId());
                        insPstmt.setInt(2, task.getUserId());
                        insPstmt.addBatch();
                    }
                    insPstmt.executeUpdate();
                }
            }

            conn.commit();
            return true;
        } catch (SQLException e){
            e.printStackTrace();
            System.err.println("ERROR: Method updateTask failed!!!");
        }
        return false;
    }

    @Override
    public boolean toggleTaskStatus(int id, boolean isCompleted, int userId) {
        String sql = "UPDATE tasks SET is_completed = ?, completed_at = ? WHERE id = ? AND user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setBoolean(1, isCompleted);
            stmt.setTimestamp(2, isCompleted ? Timestamp.valueOf(LocalDateTime.now()) : null);
            stmt.setInt(3, id);
            stmt.setInt(4, userId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("ERROR: Method toggleTaskStatus failed!!!");
            return false;
        }
    }

    //=== PHUONG THUC BO TRO ===
    private Task mapResultSetToTask(ResultSet rs) throws SQLException {
        Task task = new Task();
        task.setId(rs.getInt("id"));
        task.setUserId(rs.getInt("user_id"));
        task.setTitle(rs.getString("title"));
        task.setDescription(rs.getString("description"));
        task.setCompleted(rs.getBoolean("is_completed"));

        Timestamp dueDateTs = rs.getTimestamp("due_date");
        if(dueDateTs!=null) task.setDueDate(dueDateTs.toLocalDateTime());

        task.setPriority(rs.getInt("priority"));

        Timestamp createdAtTs = rs.getTimestamp("created_at");
        if(createdAtTs!=null) task.setCreatedAt(createdAtTs.toLocalDateTime());

        Timestamp updatedAtTs = rs.getTimestamp("updated_at");
        if(updatedAtTs!=null) task.setUpdatedAt(updatedAtTs.toLocalDateTime());

        Timestamp compTs = rs.getTimestamp("completed_at");
        if (compTs != null) task.setCompletedAt(compTs.toLocalDateTime());

        return task;
    }

    private List<Label> getLabelsForTask(int taskId, Connection conn) throws SQLException {
        List<Label> labels=new ArrayList<>();
        String query = "SELECT L.* FROM labels L JOIN task_labels TL ON L.id = TL.label_id WHERE TL.task_id = ?";
        try(PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, taskId);
            ResultSet rs = pstmt.executeQuery();
            while(rs.next()){
                labels.add(new Label(
                        rs.getInt("id"),
                        rs.getInt("user_id"),
                        rs.getString("title"),
                        rs.getString("color_code")
                ));
            }
        } catch(SQLException e){
            e.printStackTrace();
            System.err.println("ERROR: Method getLabelsForTask failed!!");
        }
        return labels;
    }
}
