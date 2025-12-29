package application;

import application.utils.CredentialStore;
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
            String viewPath = "/application/views/login-view.fxml";
            FXMLLoader loader = new FXMLLoader(getClass().getResource(viewPath));
            Parent root = loader.load();
            Scene scene = new Scene(root);

            primaryStage.setTitle("Task Flow - Login");
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
