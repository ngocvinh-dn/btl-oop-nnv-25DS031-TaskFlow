package application;

import application.DAO.UserDAO;
import application.DAOImpl.UserDAOImpl;
import application.models.User;
import application.utils.CredentialStore;
import application.utils.UserSession;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        try {

            String[] creds = CredentialStore.loadCredentials();
            boolean autoLoginSuccess = false;

            if(creds!=null){
                String username = creds[0];
                String password = creds[1];
                UserDAO userDAO = new UserDAOImpl();
                User user = userDAO.loginWithHash(username, password);
                if(user!=null){
                    UserSession.createSession(user);
                    autoLoginSuccess = true;
                }
            }

            String viewPath = autoLoginSuccess ? "/application/views/main-view.fxml" : "/application/views/login-view.fxml";
            FXMLLoader loader = new FXMLLoader(getClass().getResource(viewPath));
            Parent root = loader.load();
            Scene scene = new Scene(root);

            if(autoLoginSuccess){
                primaryStage.setTitle("Task Flow");
            } else {
                primaryStage.setTitle("Task Flow - Login");
            }
            primaryStage.setScene(scene);
            primaryStage.setResizable(true);

            primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/application/images/logo.png")));

            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("ERROR: Could not load view FXML!!");
        }
    }
}
