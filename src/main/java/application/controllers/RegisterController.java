package application.controllers;

import application.DAO.UserDAO;
import application.DAOImpl.UserDAOImpl;
import application.models.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.IOException;

public class RegisterController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private TextField passwordVisibleField;
    @FXML private Region eyeIconPass;
    @FXML private PasswordField confirmPasswordField;
    @FXML private TextField confirmPasswordVisibleField;
    @FXML private Region eyeIconConfirm;
    @FXML private Button registerButton;
    @FXML private Hyperlink loginLink;

    private boolean isPasswordVisible = false;
    private boolean isConfirmPasswordVisible = false;

    private final UserDAO userDAO = new UserDAOImpl();

    @FXML
    private void initialize() {
        passwordField.textProperty().addListener((observable, oldValue, newValue) -> {
            if(!isPasswordVisible) passwordVisibleField.setText(newValue);
        });
        passwordVisibleField.textProperty().addListener((observable, oldValue, newValue) -> {
            if(isPasswordVisible) passwordField.setText(newValue);
        });
        confirmPasswordField.textProperty().addListener((observable, oldValue, newValue) -> {
            if(!isConfirmPasswordVisible) confirmPasswordVisibleField.setText(newValue);
        });
        confirmPasswordVisibleField.textProperty().addListener((observable, oldValue, newValue) -> {
            if(isConfirmPasswordVisible) confirmPasswordField.setText(newValue);
        });
    }

    @FXML
    private void togglePasswordVisibility() {
        isPasswordVisible = !isPasswordVisible;
        if(isPasswordVisible){
            passwordVisibleField.setVisible(true);
            passwordVisibleField.setManaged(true);
            passwordField.setVisible(false);
            passwordField.setManaged(false);
            eyeIconPass.getStyleClass().remove("icon-eye-closed");
            eyeIconPass.getStyleClass().add("icon-eye-opened");
        } else {
            passwordField.setVisible(true);
            passwordField.setManaged(true);
            passwordVisibleField.setVisible(false);
            passwordVisibleField.setManaged(false);
            eyeIconPass.getStyleClass().remove("icon-eye-opened");
            eyeIconPass.getStyleClass().add("icon-eye-closed");
        }
    }

    @FXML
    private void toggleConfirmPasswordVisibility(){
        isConfirmPasswordVisible = !isConfirmPasswordVisible;
        if(isConfirmPasswordVisible){
            confirmPasswordVisibleField.setVisible(true);
            confirmPasswordVisibleField.setManaged(true);
            confirmPasswordField.setVisible(false);
            confirmPasswordField.setManaged(false);
            eyeIconConfirm.getStyleClass().remove("icon-eye-closed");
            eyeIconConfirm.getStyleClass().add("icon-eye-opened");
        } else {
            confirmPasswordField.setVisible(true);
            confirmPasswordField.setManaged(true);
            confirmPasswordVisibleField.setVisible(false);
            confirmPasswordVisibleField.setManaged(false);
            eyeIconConfirm.getStyleClass().remove("icon-eye-opened");
            eyeIconConfirm.getStyleClass().add("icon-eye-closed");
        }
    }

    @FXML
    private void handleRegister() {
        String username = usernameField.getText();
        String password= isPasswordVisible ? passwordVisibleField.getText() : passwordField.getText();
        String confirmPassword = isConfirmPasswordVisible ? confirmPasswordVisibleField.getText() : passwordVisibleField.getText();

        if(username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()){
            showAlert(Alert.AlertType.ERROR, "Form Error", "Please fill in all required information!");
            return;
        }

        if(password.length()<6){
            showAlert(Alert.AlertType.ERROR, "Form Error", "Password too short! Please enter at least 6 characters!");
            return;
        }

        if(!password.equals(confirmPassword)){
            showAlert(Alert.AlertType.ERROR, "Form Error", "Passwords do not match!");
            return;
        }

        if(userDAO.checkUsernameExists(username)){
            showAlert(Alert.AlertType.ERROR, "Form Error", "Username exists!");
            return;
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(password);

        boolean success = userDAO.register(user);

        if(success) {
            showAlert(Alert.AlertType.INFORMATION, "Success", "Account registered successfully! Please log in.");
            showLoginView();
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Account could not be registered! Please try again.");
        }
    }

    @FXML
    private void showLoginView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/application/views/login-view.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) loginLink.getScene().getWindow();

            stage.setTitle("Task Flow - Login");
            stage.getIcons().add(new Image(getClass().getResourceAsStream("/application/images/logo.png")));

            stage.getScene().setRoot(root);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("ERROR: Could not load login view in RegisterController!!");
        }
    }

    private void showAlert(Alert.AlertType type, String title, String text) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(text);
        alert.showAndWait();
    }
}
