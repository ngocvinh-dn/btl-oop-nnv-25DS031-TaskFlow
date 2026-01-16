package application.components;

import application.models.Label;
import application.models.Task;
import application.services.TaskService;
import application.controllers.TaskDialogOpener;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.time.format.DateTimeFormatter;

public class TaskCard extends HBox {

    private final Task task;
    private final TaskService taskService;
    private final Runnable onRefreshRequest;

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy - HH:mm");

    public TaskCard(Task task, Runnable onRefreshRequest) {
        this.task = task;
        this.onRefreshRequest = onRefreshRequest;
        this.taskService = new TaskService();

        initUI();
    }

    private void initUI() {
        this.getStyleClass().add("task-card");
        this.setAlignment(Pos.CENTER_LEFT);
        this.setSpacing(15);

        Region priorityIndicator = new Region();
        priorityIndicator.setPrefWidth(4);
        priorityIndicator.setPrefHeight(40);

        String priorityColor = "transparent";
        if (task.getPriority() == 1) priorityColor = "#FF5252";
        else if (task.getPriority() == 2) priorityColor = "#FFA726";

        priorityIndicator.setStyle("-fx-background-color: " + priorityColor + "; -fx-background-radius: 2;");

        CheckBox checkBox = new CheckBox();
        checkBox.getStyleClass().add("task-checkbox");
        checkBox.setSelected(task.isCompleted());
        checkBox.setOnAction(e -> handleToggleStatus());

        VBox contentBox = new VBox(5);
        contentBox.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(contentBox, Priority.ALWAYS);

        javafx.scene.control.Label titleLabel = new javafx.scene.control.Label(task.getTitle());
        titleLabel.getStyleClass().add("task-title");
        if (task.isCompleted()) titleLabel.getStyleClass().add("task-title-completed");

        javafx.scene.control.Label dateLabel = new javafx.scene.control.Label();
        dateLabel.getStyleClass().add("task-date");
        StringBuilder dateText = new StringBuilder();

        if (task.getDueDate() != null) {
            dateText.append("Due: ").append(task.getDueDate().format(dateTimeFormatter));
        }

        if (task.isCompleted() && task.getCompletedAt() != null) {
            dateText.append(" | Done: ").append(task.getCompletedAt().format(dateTimeFormatter));
            dateLabel.setStyle("-fx-text-fill: #4CAF50;");
        } else if (!task.isCompleted() && task.getDueDate() != null && task.getDueDate().isBefore(java.time.LocalDateTime.now())) {
            dateLabel.setStyle("-fx-text-fill: #FF5252;");
            dateText.append(" (Overdue)");
        }
        dateLabel.setText(dateText.toString());

        HBox labelsBox = new HBox(5);
        if (task.getLabels() != null) {
            for (Label l : task.getLabels()) {
                javafx.scene.control.Label tag = new javafx.scene.control.Label(l.getTitle());
                tag.getStyleClass().add("task-tag");
                try {
                    Color c = Color.web(l.getColorCode());
                    String hex = String.format("#%02X%02X%02X",
                            (int)(c.getRed()*255), (int)(c.getGreen()*255), (int)(c.getBlue()*255));
                    tag.setStyle("-fx-background-color: " + hex + "33; -fx-text-fill: " + hex + ";");
                } catch (Exception e) {
                    tag.setStyle("-fx-background-color: #E0E0E0; -fx-text-fill: #333;");
                }
                labelsBox.getChildren().add(tag);
            }
        }
        contentBox.getChildren().addAll(titleLabel, dateLabel, labelsBox);

        Button btnEdit = new Button("✎");
        btnEdit.getStyleClass().add("task-edit-btn");
        btnEdit.setOnAction(e -> {
            TaskDialogOpener.openTaskDialog(this.getScene().getWindow(), task, onRefreshRequest);
        });

        Button btnDelete = new Button("✕");
        btnDelete.getStyleClass().add("task-delete-btn");
        btnDelete.setOnAction(e -> handleDelete());

        this.getChildren().addAll(priorityIndicator,checkBox, contentBox, btnEdit, btnDelete);
    }

    private void handleToggleStatus() {
        boolean success = taskService.toggleStatus(task);
        if (success) {
            if (onRefreshRequest != null) onRefreshRequest.run();
        }
    }

    private void handleDelete() {
        if (taskService.delete(task.getId())) {
            if (onRefreshRequest != null) onRefreshRequest.run();
        }
    }
}
