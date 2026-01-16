package application.controllers;

import application.components.TaskCard;
import application.models.Task;
import application.services.TaskService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class TasksController implements Initializable {

    @FXML private TextField txtSearch;
    @FXML private ToggleGroup timeGroup;
    @FXML private ToggleGroup filterGroup;

    @FXML private ComboBox<String> cbFilterPriority;
    @FXML private ComboBox<String> cbSort;

    @FXML private Label lblActiveCount, lblCompletedCount;
    @FXML private VBox tasksContainer;
    @FXML private ScrollPane scrollPane;
    @FXML private VBox emptyStateContainer;
    @FXML private Button btnAdd;

    private final TaskService taskService = new TaskService();
    private String currentTimeFilter = "All tasks";
    private String currentStatusFilter ="All";

    @FXML
    public void initialize(URL location, ResourceBundle resources){
        cbFilterPriority.setItems(FXCollections.observableArrayList("All Priorities", "High", "Medium", "Low"));
        cbFilterPriority.getSelectionModel().selectFirst();
        cbSort.setItems(FXCollections.observableArrayList("Default", "Date: Ascending", "Date: Descending", "Priority: High to Low"));
        cbSort.getSelectionModel().selectFirst();

        setupEventHandlers();
        loadTasks();
    }

    private void setupEventHandlers(){
        timeGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                currentTimeFilter = ((ToggleButton)newVal).getText();
                loadTasks();
            }
        });

        filterGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                currentStatusFilter = ((ToggleButton)newVal).getText();
                loadTasks();
            }
        });

        txtSearch.textProperty().addListener((obs, oldVal, newVal) -> loadTasks());

        cbFilterPriority.valueProperty().addListener((obs, oldVal, newVal) -> loadTasks());

        cbSort.valueProperty().addListener((obs, oldVal, newVal) -> loadTasks());

        btnAdd.setOnAction(e -> TaskDialogOpener.openTaskDialog(btnAdd.getScene().getWindow(), this::loadTasks));
    }

    public void loadTasks() {
        String keyword = txtSearch.getText();
        String priority = cbFilterPriority.getValue();
        String sort = cbSort.getValue();

        List<Task> tasks = taskService.getFilteredTasks(keyword, currentTimeFilter, currentStatusFilter, priority, sort);
        long active = tasks.stream().filter(t -> !t.isCompleted()).count();
        long completed = tasks.stream().filter(t -> t.isCompleted()).count();
        lblActiveCount.setText(active + " active");
        lblCompletedCount.setText(completed + " completed");
        renderTaskCards(tasks);
    }

    private void renderTaskCards(List<Task> tasks) {
        tasksContainer.getChildren().clear();
        if (tasks.isEmpty()) {
            emptyStateContainer.setVisible(true);
            scrollPane.setVisible(false);
        } else {
            emptyStateContainer.setVisible(false);
            scrollPane.setVisible(true);
            for (Task task : tasks) {
                TaskCard card = new TaskCard(task, this::loadTasks);
                tasksContainer.getChildren().add(card);
            }
        }
    }
}
