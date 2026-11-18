package com.dentalclinic;

import com.dentalclinic.ui.AppWindow;
import com.dentalclinic.utils.AppConfig;
import com.dentalclinic.utils.DatabaseConnection;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // Creates the database, the tables and the default data when needed
            DatabaseConnection.initialize();
        } catch (RuntimeException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Database error");
            alert.setHeaderText("The application could not start");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
            Platform.exit();
            return;
        }

        primaryStage.setTitle(AppConfig.clinicName() + " - Management System");
        AppWindow.showLogin(primaryStage);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
