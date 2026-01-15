package application.controllers;

import application.components.TaskCard;
import application.models.Task;
import application.services.TaskService;
import application.controllers.TaskDialogOpener;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class CalendarController implements Initializable{

    @FXML private Label lblMonthYear;
    @FXML private Button btnPrevMonth, btnNextMonth, btnToday;
    @FXML private GridPane calendarGrid;

    @FXML private Label lblSelectedDate;
    @FXML private VBox tasksContainer;
    @FXML private VBox emptyStateBox;
    @FXML private Button btnAddToday;

    @FXML private ToggleGroup filterGroup;
    @FXML private ComboBox<String> cbFilterPriority;
    @FXML private ComboBox<String> cbSort;
    @FXML private Label lblActiveCount, lblCompletedCount;

    private YearMonth currentYearMonth;
    private LocalDate selectedDate;
    private final TaskService taskService = new TaskService();
    private String currentStatusFilter = "All";

    @Override
    public void initialize(URL location, ResourceBundle resource){
        currentYearMonth = YearMonth.now();
        selectedDate = LocalDate.now();
        cbFilterPriority.setItems(FXCollections.observableArrayList("All Priorities", "High", "Medium", "Low"));
        cbFilterPriority.getSelectionModel().selectFirst();
        cbSort.setItems(FXCollections.observableArrayList("Default", "Time: Ascending", "Time: Descending", "Priority: High to Low"));
        cbSort.getSelectionModel().selectFirst();

        updateCalendar();
        loadTasksForDate(selectedDate);
        setupEventHandlers();
    }

    private void updateCalendar(){
        lblMonthYear.setText(currentYearMonth.getMonth().toString() + " " + currentYearMonth.getYear());
        calendarGrid.getChildren().clear();

        Map<LocalDate, Long> taskCounts = taskService.getTaskCountsForMonth(currentYearMonth);

        LocalDate firstOfMonth = currentYearMonth.atDay(1);
        int dayOfWeekValue=firstOfMonth.getDayOfWeek().getValue();
        int startCol =(dayOfWeekValue==7) ? 0:dayOfWeekValue;
        int row =0;
        int col =startCol;

        for(int day=1; day<=currentYearMonth.lengthOfMonth(); day++) {
            LocalDate date = currentYearMonth.atDay(day);
            long count = taskCounts.getOrDefault(date, 0L);
            StackPane dayCell = createDayCell(date, count);
            calendarGrid.add(dayCell, col, row);
            col++;
            if (col > 6) {
                col = 0;
                row++;
            }
        }
    }

    private StackPane createDayCell(LocalDate date, long count){
        StackPane cell = new StackPane();
        Button button = new Button(String.valueOf(date.getDayOfMonth()));
        button.getStyleClass().add("calendar-day-btn");
        if(date.equals(selectedDate)) button.getStyleClass().add("calendar-day-selected");
        button.setOnAction(event -> {
            selectedDate = date;
            updateCalendar();
            loadTasksForDate(date);
        });
        cell.getChildren().add(button);
        if(count>0){
            Label badge = new Label(String.valueOf(count));
            badge.getStyleClass().add("calendar-badge");
            StackPane.setAlignment(badge, Pos.TOP_RIGHT);
            badge.setTranslateX(2); badge.setTranslateY(2);
            badge.setMouseTransparent(true);
            cell.getChildren().add(badge);
        }
        return cell;
    }

    private void loadTasksForDate(LocalDate date){
        lblSelectedDate.setText("Tasks for "+date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

        String priorityFilter = cbFilterPriority.getValue();
        String sortOrder =cbSort.getValue();

        List<Task> filteredTasks = taskService.getTasksForSpecificDate(date, currentStatusFilter, priorityFilter, sortOrder);

        tasksContainer.getChildren().clear();

        if (filteredTasks.isEmpty()) {
            emptyStateBox.setVisible(true);
            tasksContainer.setVisible(false);
        } else {
            emptyStateBox.setVisible(false);
            tasksContainer.setVisible(true);
            for (Task task : filteredTasks) {
                TaskCard card = new TaskCard(task, () -> {
                    updateCalendar();
                    loadTasksForDate(date);
                });
                tasksContainer.getChildren().add(card);
            }
        }
    }

    private void setupEventHandlers(){
        btnPrevMonth.setOnAction(event -> {
            currentYearMonth=currentYearMonth.minusMonths(1);
            updateCalendar();
        });
        btnNextMonth.setOnAction(event -> {
            currentYearMonth=currentYearMonth.plusMonths(1);
            updateCalendar();
        });

        btnToday.setOnAction(event -> {
            currentYearMonth= YearMonth.now();
            selectedDate = LocalDate.now();
            updateCalendar();
            loadTasksForDate(selectedDate);
        });

        filterGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue!=null){
                currentStatusFilter = ((ToggleButton)newValue).getText();
                loadTasksForDate(selectedDate);
            }
        });
        cbFilterPriority.valueProperty().addListener((observable, oldValue, newValue) -> loadTasksForDate(selectedDate));
        cbSort.valueProperty().addListener((observable, oldValue, newValue) -> loadTasksForDate(selectedDate));
        btnAddToday.setOnAction(event -> TaskDialogOpener.openTaskDialog(
                btnAddToday.getScene().getWindow(),
                null,
                selectedDate,
                ()-> {updateCalendar(); loadTasksForDate(selectedDate);}
        ));
    }

}
