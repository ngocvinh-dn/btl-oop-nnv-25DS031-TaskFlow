package application.controllers;

import application.models.Task;
import application.models.Label;
import application.services.TaskService;
import application.utils.DatabaseConnection;
import application.utils.UserSession;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class AddTaskController implements Initializable {

    @FXML private javafx.scene.control.Label lblHeaderTitle;
    @FXML private TextField txtTitle;
    @FXML private TextArea txtDescription;

    @FXML private DatePicker dpDueDate;
    @FXML private Spinner<Integer> spHour;
    @FXML private Spinner<Integer> spMinute;
    @FXML private ComboBox<String> cbPriority;

    @FXML private CheckBox chkRecurrence;
    @FXML private HBox boxRecurrenceOptions;
    @FXML private ComboBox<String> cbRecurrence;
    @FXML private DatePicker dpRecurrenceEnd;

    @FXML private HBox boxWeekDays;
    @FXML private CheckBox chkMon, chkTue, chkWed, chkThu, chkFri, chkSat, chkSun;

    @FXML private FlowPane labelsPane;
    @FXML private Button btnSave;
    @FXML private Button btnCancel;

    private final TaskService taskService = new TaskService();
    private final List<CheckBox> labelCheckBoxes = new ArrayList<>();
    private Task currentTask = null;

    @Override
    public void initialize(URL Location, ResourceBundle resources){
        cbPriority.setItems(FXCollections.observableArrayList("High", "Medium", "Low"));
        cbPriority.getSelectionModel().select("Low");

        SpinnerValueFactory<Integer> hourFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0,23,23);
        hourFactory.setConverter(new TwoDigitStringConverter());
        spHour.setValueFactory(hourFactory);
        SpinnerValueFactory<Integer> minuteFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0,59,59);
        minuteFactory.setConverter(new TwoDigitStringConverter());
        spMinute.setValueFactory(minuteFactory);

        cbRecurrence.setItems(FXCollections.observableArrayList("Daily", "Weekly", "Monthly"));
        cbRecurrence.getSelectionModel().select("Daily");

        cbRecurrence.valueProperty().addListener((ov, oldValue, newValue) -> {
            updateRecurrenceEndDate(newValue);
            boolean isWeekly = "Weekly".equals(newValue);
            boxWeekDays.setVisible(isWeekly);
            boxWeekDays.setManaged(isWeekly);
        });

        chkRecurrence.selectedProperty().addListener((obs, oldVal, newVal) -> {
            boxRecurrenceOptions.setVisible(newVal);
            boxRecurrenceOptions.setManaged(newVal);

            if (newVal) {
                String freq = cbRecurrence.getValue();
                updateRecurrenceEndDate(freq);

                boolean isWeekly = "Weekly".equals(freq);
                boxWeekDays.setVisible(isWeekly);
                boxWeekDays.setManaged(isWeekly);
            } else {
                boxWeekDays.setVisible(false);
                boxWeekDays.setManaged(false);
            }
        });

        dpDueDate.valueProperty().addListener((ov, oldVal, newVal) -> {
           if(newVal!=null && chkRecurrence.isSelected()) {
               updateRecurrenceEndDate(cbRecurrence.getValue());
               if (boxWeekDays.isVisible()) {
                   checkDayOfWeek(newVal.getDayOfWeek());
               }
           }
        });

        loadLabelsFromDB(UserSession.getInstance().getUser().getId());

        btnSave.setOnAction(event -> saveTask());
        btnCancel.setOnAction(event -> closeWindow());

    }

    private void updateRecurrenceEndDate(String freq) {
        LocalDate start = dpDueDate.getValue();
        if(start==null) start = LocalDate.now();
        LocalDate endDate = null;
        if("Daily".equals(freq)) endDate = start.plusWeeks(1);
        else if("Weekly".equals(freq)) endDate = start.plusWeeks(1);
        else if("Monthly".equals(freq)) endDate = start.plusMonths(6);
        if(endDate!=null) dpRecurrenceEnd.setValue(endDate);
    }

    private void checkDayOfWeek(DayOfWeek dayOfWeek) {
        chkMon.setSelected(false); chkTue.setSelected(false); chkWed.setSelected(false);
        chkThu.setSelected(false); chkFri.setSelected(false); chkSat.setSelected(false); chkSun.setSelected(false);

        switch (dayOfWeek) {
            case MONDAY: chkMon.setSelected(true); break;
            case TUESDAY: chkTue.setSelected(true); break;
            case WEDNESDAY: chkWed.setSelected(true); break;
            case THURSDAY: chkThu.setSelected(true); break;
            case FRIDAY: chkFri.setSelected(true); break;
            case SATURDAY: chkSat.setSelected(true); break;
            case SUNDAY: chkSun.setSelected(true); break;
        }
    }

    private void loadLabelsFromDB(int userId) {
        String query = "SELECT * FROM labels WHERE user_id=?";
        try(Connection conn = DatabaseConnection.getConnection()){
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            while(rs.next()) {
                Label label = new Label(
                        rs.getInt("id"),
                        rs.getInt("user_id"),
                        rs.getString("title"),
                        rs.getString("color_code")
                );

                CheckBox cb = new CheckBox(label.getTitle());
                cb.getStyleClass().add("label-checkbox");
                cb.setUserData(label);
                cb.setStyle("-fx-text-fill: " + label.getColorCode() + ";");

                labelCheckBoxes.add(cb);
                labelsPane.getChildren().add(cb);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("ERROR: load Labels from DB in Add Task Controller failed !!");
        }
    }

    private void saveTask() {
        String title = txtTitle.getText().trim();
        if (title.isEmpty()) {
            showAlert("Validation Error", "Title cannot be empty!");
            return;
        }

        Task taskToSave;
        if (currentTask == null) {
            taskToSave = new Task();
        } else {
            taskToSave = currentTask;
        }

        taskToSave.setTitle(title);
        taskToSave.setDescription(txtDescription.getText().trim());

        if (dpDueDate.getValue() != null) {
            int h = spHour.getValue();
            int m = spMinute.getValue();
            LocalTime time = LocalTime.of(h, m);
            taskToSave.setDueDate(dpDueDate.getValue().atTime(time));
        } else {
            taskToSave.setDueDate(null);
        }

        String priorityStr = cbPriority.getValue();
        int priorityVal = 3;
        if ("High".equals(priorityStr)) priorityVal = 1;
        else if ("Medium".equals(priorityStr)) priorityVal = 2;
        taskToSave.setPriority(priorityVal);

        taskToSave.setLabels(new ArrayList<>());
        for (CheckBox cb : labelCheckBoxes) {
            if (cb.isSelected()) {
                taskToSave.addLabel((Label) cb.getUserData());
            }
        }

        boolean success;

        if (currentTask == null && chkRecurrence.isSelected() && taskToSave.getDueDate() != null) {
            String freq = cbRecurrence.getValue();
            LocalDate until = dpRecurrenceEnd.getValue();

            if (until == null) {
                showAlert("Missing Info", "Please select an end date for recurrence.");
                return;
            }
            if (until.isBefore(taskToSave.getDueDate().toLocalDate())) {
                showAlert("Invalid Date", "End date must be after due date.");
                return;
            }

            List<DayOfWeek> specificDays = new ArrayList<>();
            if ("Weekly".equals(freq)) {
                if (chkMon.isSelected()) specificDays.add(DayOfWeek.MONDAY);
                if (chkTue.isSelected()) specificDays.add(DayOfWeek.TUESDAY);
                if (chkWed.isSelected()) specificDays.add(DayOfWeek.WEDNESDAY);
                if (chkThu.isSelected()) specificDays.add(DayOfWeek.THURSDAY);
                if (chkFri.isSelected()) specificDays.add(DayOfWeek.FRIDAY);
                if (chkSat.isSelected()) specificDays.add(DayOfWeek.SATURDAY);
                if (chkSun.isSelected()) specificDays.add(DayOfWeek.SUNDAY);

                if (specificDays.isEmpty()) {
                    specificDays.add(taskToSave.getDueDate().getDayOfWeek());
                }
            }

            success = taskService.addRecurrentTasks(taskToSave, freq, until, specificDays);
        } else {
            success = (currentTask == null) ? taskService.add(taskToSave) : taskService.update(taskToSave);
        }

        if (success) {
            closeWindow();
        } else {
            showAlert("Database Error", "Could not save task.");
        }
    }

    private void closeWindow() {
        Stage stage = (Stage) btnCancel.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String title, String s){
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(s);
        alert.showAndWait();
    }

    private static class TwoDigitStringConverter extends StringConverter<Integer> {
        @Override
        public String toString(Integer integer) {
            if(integer == null) return "00";
            return String.format("%02d",integer);
        }

        @Override
        public Integer fromString(String s) {
            try{
                return Integer.valueOf(s);
            } catch(NumberFormatException e){
                return 0;
            }
        }
    }

    public void setTaskToEdit(Task task) {
        this.currentTask = task;

        if(lblHeaderTitle !=null) lblHeaderTitle.setText("Edit Task");
        btnSave.setText("Update");

        txtTitle.setText(task.getTitle());
        txtDescription.setText(task.getDescription());

        if(task.getDueDate() != null) {
            dpDueDate.setValue(task.getDueDate().toLocalDate());
            LocalTime time = task.getDueDate().toLocalTime();
            spHour.getValueFactory().setValue(time.getHour());
            spMinute.getValueFactory().setValue(time.getMinute());
        }

        switch(task.getPriority()) {
            case 1: cbPriority.getSelectionModel().select("High"); break;
            case 2: cbPriority.getSelectionModel().select("Medium"); break;
            default: cbPriority.getSelectionModel().select("Low"); break;
        }

        if(task.getLabels() != null) {
            for(CheckBox cb :labelCheckBoxes) {
                Label labelTag =(Label) cb.getUserData();
                boolean isSelected = task.getLabels().stream().anyMatch(l->l.getId()==labelTag.getId());
                cb.setSelected(isSelected);
            }
        }
        chkRecurrence.setDisable(true);
    }

    public void setInitialDate(LocalDate date) {
        if(date!=null && dpDueDate.getValue()==null) {
            dpDueDate.setValue(date);
            checkDayOfWeek(date.getDayOfWeek());
        }
    }

}
