package application.controllers;

import application.models.User;
import application.utils.CredentialStore;
import application.utils.UserSession;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class MainController implements Initializable {

    @FXML private Button btnNavTasks;
    @FXML private Button btnNavCalendar;
    @FXML private Button btnNavLabels;

    @FXML private StackPane contentArea;

    @FXML private Label usernameLabel;
    @FXML private Button btnLogout;

    @Override
    public void initialize(URL location, ResourceBundle resource) {
        if(UserSession.getInstance() != null && UserSession.getInstance().getUser()!=null){
            User currentUser = UserSession.getInstance().getUser();
            usernameLabel.setText(currentUser.getUsername());
        }

        loadView("/application/views/tasks-view.fxml");
        setActiveNav(btnNavTasks);

        btnNavTasks.setOnAction(e->{
            loadView("/application/views/tasks-view.fxml");
            setActiveNav(btnNavTasks);
        });

        btnNavCalendar.setOnAction(e->{
            loadView("/application/views/calendar-view.fxml");
            setActiveNav(btnNavCalendar);
        });

        btnNavLabels.setOnAction(e->{
            loadView("/application/views/labels-view.fxml");
            setActiveNav(btnNavLabels);
        });

        if(btnLogout!=null){
            btnLogout.setOnAction(e->handleLogout());
        }

    }

    private void loadView(String viewPath){
        try{
            Parent view = FXMLLoader.load(getClass().getResource(viewPath));
            contentArea.getChildren().clear();
            contentArea.getChildren().add(view);
        } catch(Exception e){
            e.printStackTrace();
            System.err.println("ERROR: Could not load "+viewPath+" !!");
        }
    }

    private void setActiveNav(Button activeBtn){
        btnNavTasks.getStyleClass().remove("nav-button-active");
        btnNavCalendar.getStyleClass().remove("nav-button-active");
        btnNavLabels.getStyleClass().remove("nav-button-active");
        activeBtn.getStyleClass().add("nav-button-active");
    }

    @FXML
    private void handleLogout(){
        UserSession.clearSession();
        CredentialStore.clearCredentials();
        try{
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/application/views/login-view.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) contentArea.getScene().getWindow();

            Scene loginScence = new Scene(root);
            stage.setScene(loginScence);
            stage.setTitle("Task Flow - Login");

            stage.centerOnScreen();
            stage.setResizable(true);
            stage.show();
        } catch(Exception e){
            e.printStackTrace();
            System.err.println("ERROR: Could not handle Logout !!");
        }
    }
}
