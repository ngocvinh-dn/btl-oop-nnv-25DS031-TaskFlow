module application {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires mysql.connector.j;
    requires javafx.graphics;
    requires java.prefs;

    opens application.controllers to javafx.fxml;
    exports application.controllers;

    opens application to javafx.fxml;
    exports application;
}