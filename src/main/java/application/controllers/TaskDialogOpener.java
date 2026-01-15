package application.controllers;

import application.models.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.IOException;
import java.time.LocalDate;

public class TaskDialogOpener {

    public static void openTaskDialog(Window ownerWindow, Runnable onCloseAction) {
        openTaskDialog(ownerWindow, null, null, onCloseAction);
    }

    public static void openTaskDialog(Window ownerWindow, Task taskToEdit, Runnable onCloseAction){
        openTaskDialog(ownerWindow,taskToEdit,null,onCloseAction);
    }

    public static void openTaskDialog(Window ownerWindow, Task taskToEdit, LocalDate defaultDate, Runnable onCloseAction){
        try {
            FXMLLoader loader = new FXMLLoader(TaskDialogOpener.class.getResource("/application/views/add-task-view.fxml"));
            Parent root = loader.load();

            root.setStyle("-fx-background-color: #FFFFFF;");

            AddTaskController controller = loader.getController();
            String title = "New Task";

            if(taskToEdit != null){
                title ="Edit Task";
                controller.setTaskToEdit(taskToEdit);
            } else if(defaultDate != null){
                controller.setInitialDate(defaultDate);
            }

            Stage stage = new Stage();
            stage.setTitle(title);
            stage.setScene(new Scene(root));

            stage.initModality(Modality.APPLICATION_MODAL);
            if(ownerWindow != null){
                stage.initOwner(ownerWindow);
            }

            stage.showAndWait();

            if(onCloseAction != null){
                onCloseAction.run();
            }
        }catch (IOException e){
            e.printStackTrace();
            System.err.println("ERROR: Could not load add task view FXML!!");
        }
    }

}
