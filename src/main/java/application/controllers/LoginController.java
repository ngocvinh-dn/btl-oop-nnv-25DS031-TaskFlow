package application.controllers;

import application.DAO.UserDAO;
import application.DAOImpl.UserDAOImpl;
import application.models.User;
import application.utils.CredentialStore;
import application.utils.SecurityUtils;
import application.utils.UserSession;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private TextField passwordVisibleField;
    @FXML private Region eyeIcon;
    @FXML private CheckBox chkRemember;
    @FXML private Button loginButton;
    @FXML private Hyperlink registerLink;

    private boolean isPasswordVisible = false;

    private final UserDAO userDAO = new UserDAOImpl();

    @FXML
    public void initialize(){
        passwordField.textProperty().addListener((observable, oldValue, newValue) -> {
            if(!isPasswordVisible) passwordVisibleField.setText(newValue);
        });
        passwordVisibleField.textProperty().addListener((observable, oldValue, newValue) -> {
            if(isPasswordVisible) passwordField.setText(newValue);
        });
    }

    @FXML
    public void togglePasswordVisibility() {
        isPasswordVisible = !isPasswordVisible;

        if(isPasswordVisible){
            passwordVisibleField.setVisible(true);
            passwordVisibleField.setManaged(true);
            passwordField.setVisible(false);
            passwordField.setManaged(false);
            eyeIcon.getStyleClass().remove("icon-eye-closed");
            eyeIcon.getStyleClass().add("icon-eye-opened");
        } else {
            passwordField.setVisible(true);
            passwordField.setManaged(true);
            passwordVisibleField.setVisible(false);
            passwordVisibleField.setManaged(false);
            eyeIcon.getStyleClass().remove("icon-eye-opened");
            eyeIcon.getStyleClass().add("icon-eye-closed");
        }
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText();
        String password = isPasswordVisible ? passwordVisibleField.getText() : passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Form Error", "Please enter both Username and Password!");
            return;
        }

        User user = userDAO.login(username, password);

        if (user != null) {
            UserSession.createSession(user);
            if (chkRemember != null && chkRemember.isSelected()) {
                String hashedPassword = SecurityUtils.hashPassword(password);
                CredentialStore.saveCredentials(username, hashedPassword);
            } else {
                CredentialStore.clearCredentials();
            }
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/application/views/main-view.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) loginButton.getScene().getWindow();

            Scene scene = new Scene(root);
            stage.setScene(scene);

            stage.centerOnScreen();
            stage.setResizable(true);
            stage.setTitle("Task Flow");
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "System Error", "Could not load Main View: " + e.getMessage());
        }
    }

    @FXML
    private void showRegisterView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/application/views/register-view.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) registerLink.getScene().getWindow();

            stage.getIcons().add(new Image(getClass().getResourceAsStream("/application/images/logo.png")));
            stage.setTitle("Task Flow - Register");

            stage.getScene().setRoot(root);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Failed to load Register View in Loginn Controller!");
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

}
