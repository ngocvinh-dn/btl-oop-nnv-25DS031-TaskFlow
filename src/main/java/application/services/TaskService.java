package application.services;

import application.DAO.TaskDAO;
import application.DAOImpl.TaskDAOImpl;
import application.models.Task;
import application.utils.UserSession;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.stream.Collectors;

public class TaskService {

    private final TaskDAO taskDAO;

    public TaskService() {
        this.taskDAO = new TaskDAOImpl();
    }

    private int getCurrentUserId() {
        if(UserSession.getInstance() != null && UserSession.getInstance().getUser() != null) {
            return UserSession.getInstance().getUser().getId();
        }
        return -1;
    }

    public List<Task> loadAllTasks() {
        return taskDAO.getAllTasks(getCurrentUserId());
    }

    public boolean toggleStatus(Task task) {
        int userId = getCurrentUserId();
        boolean newStatus = !task.isCompleted();
        boolean success = taskDAO.toggleTaskStatus(task.getId(), newStatus, userId);
        if (success) {
            task.setCompleted(newStatus);
            task.setCompletedAt(newStatus ? LocalDateTime.now() : null);
        }
        return success;
    }

    public boolean delete(int id){
        return taskDAO.deleteTask(id, getCurrentUserId());
    }

    public boolean add(Task task){
        return taskDAO.addTask(task, getCurrentUserId());
    }

    public boolean update(Task task){
        return taskDAO.updateTask(task, getCurrentUserId());
    }

    public List<Task> getFilteredTasks(String searchKeyword, String timeFilter, String statusFilter, String priorityFilter, String sortOrder) {
        List<Task> allTasks = taskDAO.getAllTasks(getCurrentUserId());
        return allTasks.stream().filter(task -> filterByStatus(task, statusFilter))
                .filter(task -> filterByTime(task, timeFilter))
                .filter(task -> filterByPriority(task, priorityFilter))
                .filter(task -> filterBySearch(task, searchKeyword))
                .sorted(getComparator(sortOrder))
                .collect(Collectors.toList());
    }

    private boolean filterByStatus(Task task, String statusFilter) {
        switch (statusFilter) {
            case "Active": return !task.isCompleted();
            case "Completed": return task.isCompleted();
            default: return true;
        }
    }

    private boolean filterByPriority(Task task, String priorityFilter) {
        if(priorityFilter == null || "All Priorities".equals(priorityFilter)) return true;
        int level = 3;
        if("High".equals(priorityFilter)) level = 1;
        else if("Medium".equals(priorityFilter)) level = 2;
        return task.getPriority() == level;
    }

    private boolean filterByTime(Task task, String timeFilter) {
        if(task.getDueDate()==null) return true;
        LocalDate due = task.getDueDate().toLocalDate();
        LocalDate today = LocalDate.now();

        switch(timeFilter) {
            case "Today": return due.isEqual(today);
            case "This Week":
                WeekFields weekFields = WeekFields.of(Locale.getDefault());
                int weekTask = due.get(weekFields.weekOfWeekBasedYear());
                int weekNow = today.get(weekFields.weekOfWeekBasedYear());
                return weekTask == weekNow && due.getYear() == today.getYear();
            case "This Month": return due.getMonth() == today.getMonth() && due.getYear() == today.getYear();
            default: return true;
        }
    }

    private boolean filterBySearch(Task task, String keyword) {
        if(keyword == null || keyword.trim().isEmpty()) return true;
        return task.getTitle().toLowerCase().contains(keyword.toLowerCase()) ||
                (task.getDescription() != null && task.getDescription().toLowerCase().contains(keyword.toLowerCase()));
    }

    private Comparator<Task> getComparator(String sortOrder) {
        Comparator<Task> defaultComp = Comparator.comparing(Task::isCompleted)
                                             .thenComparing(Task::getPriority)
                                             .thenComparing(Task::getDueDate, Comparator.nullsLast(Comparator.naturalOrder()));
        if(sortOrder==null) return defaultComp;

        switch (sortOrder) {
            case "Date: Ascending":
                return Comparator.comparing(Task::getDueDate, Comparator.nullsLast(Comparator.naturalOrder()));
            case "Date: Descending":
                return Comparator.comparing(Task::getDueDate, Comparator.nullsLast(Comparator.reverseOrder()));
            case "Priority: High to Low":
                return Comparator.comparingInt(Task::getPriority);
            case "Priority: Low to High":
                return Comparator.comparingInt(Task::getPriority).reversed();
            default: return defaultComp;
        }

    }

    public boolean addRecurrentTasks(Task baseTask, String frequency, LocalDate untilDate, List<DayOfWeek> specificDays) {
        if (baseTask.getDueDate() == null) return add(baseTask);

        LocalDateTime currentDateTime = baseTask.getDueDate();
        boolean atLeastOneSuccess = false;

        if ("Weekly".equals(frequency) && specificDays != null && !specificDays.isEmpty()) {
            while (!currentDateTime.toLocalDate().isAfter(untilDate)) {
                if (specificDays.contains(currentDateTime.getDayOfWeek())) {
                    if (createAndSaveTask(baseTask, currentDateTime)) {
                        atLeastOneSuccess = true;
                    }
                }
                currentDateTime = currentDateTime.plusDays(1);
            }
        }
        else {
            while (!currentDateTime.toLocalDate().isAfter(untilDate)) {
                if (createAndSaveTask(baseTask, currentDateTime)) {
                    atLeastOneSuccess = true;
                }
                switch (frequency) {
                    case "Daily": currentDateTime = currentDateTime.plusDays(1); break;
                    case "Weekly": currentDateTime = currentDateTime.plusWeeks(1); break;
                    case "Monthly": currentDateTime = currentDateTime.plusMonths(1); break;
                    default: return atLeastOneSuccess;
                }
            }
        }
        return atLeastOneSuccess;
    }

    public List<Task> getTasksForSpecificDate(LocalDate date, String statusFilter, String priorityFilter, String sortOrder){
        List<Task> allTasks = taskDAO.getAllTasks(getCurrentUserId());
        return allTasks.stream()
                .filter(t -> t.getDueDate() !=null &&t.getDueDate().toLocalDate().equals(date))
                .filter(task -> filterByStatus(task, statusFilter))
                .filter(task -> filterByPriority(task, priorityFilter))
                .sorted(getComparator(sortOrder))
                .collect(Collectors.toList());
    }

    public Map<LocalDate, Long> getTaskCountsForMonth(YearMonth yearMonth) {
        List<Task> allTasks = taskDAO.getAllTasks(getCurrentUserId());
        return allTasks.stream()
                .filter(t -> t.getDueDate() != null &&
                        !t.isCompleted() &&
                        YearMonth.from(t.getDueDate()).equals(yearMonth))
                .collect(Collectors.groupingBy(t -> t.getDueDate().toLocalDate(), Collectors.counting()));
    }

    private boolean createAndSaveTask(Task baseTask, LocalDateTime date) {
        Task newTask = new Task();
        newTask.setTitle(baseTask.getTitle());
        newTask.setDescription(baseTask.getDescription());
        newTask.setPriority(baseTask.getPriority());
        newTask.setDueDate(date);
        if(baseTask.getLabels() != null){
            newTask.setLabels(new ArrayList<>(baseTask.getLabels()));
        }
        return taskDAO.addTask(newTask, getCurrentUserId());
    }
}
