package com.dentalclinic.ui;

import com.dentalclinic.model.User;
import com.dentalclinic.utils.Session;
import javafx.scene.Scene;
import javafx.stage.Stage;

/** Switches the main window between the login screen and the application. */
public final class AppWindow {
    private AppWindow() {
    }

    public static void showLogin(Stage stage) {
        Session.clear();
        stage.setMaximized(false);
        Scene scene = new Scene(new LoginView(stage), 900, 640);
        scene.getStylesheets().add(Ui.CSS);
        stage.setScene(scene);
        stage.setMinWidth(600);
        stage.setMinHeight(500);
        stage.centerOnScreen();
    }

    public static void showMain(Stage stage, User user) {
        Session.setUser(user);
        Scene scene = new Scene(new MainView(stage, user), 1280, 780);
        scene.getStylesheets().add(Ui.CSS);
        stage.setScene(scene);
        stage.setMinWidth(1000);
        stage.setMinHeight(650);
        stage.setMaximized(true);
    }
}
