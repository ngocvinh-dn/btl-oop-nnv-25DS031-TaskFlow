package application.DAO;

import application.models.Task;

import java.util.List;

public interface TaskDAO {
    List<Task> getAllTasks(int userId);
    boolean addTask(Task task, int userId);
    boolean deleteTask(int id, int userId);
    boolean updateTask(Task task, int userId);
    boolean toggleTaskStatus(int id, boolean isCompleted, int userId);
}
