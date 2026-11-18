package com.dentalclinic.ui;

import com.dentalclinic.model.User;
import com.dentalclinic.service.AuthenticationService;
import com.dentalclinic.utils.AppConfig;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

final class LoginView extends StackPane {
    private final Stage stage;
    private final AuthenticationService auth = new AuthenticationService();
    private final TextField username = new TextField();
    private final PasswordField password = new PasswordField();
    private final Label error = new Label();

    LoginView(Stage stage) {
        this.stage = stage;
        getStyleClass().add("login-root");

        Label clinic = new Label(AppConfig.clinicName());
        clinic.getStyleClass().add("login-title");
        Label subtitle = Ui.muted("Management System - please sign in");

        username.setPromptText("Username");
        password.setPromptText("Password");
        password.setOnAction(e -> login());

        error.getStyleClass().add("error-text");
        error.setWrapText(true);

        Button signIn = new Button("Sign in");
        signIn.getStyleClass().add("primary");
        signIn.setDefaultButton(true);
        signIn.setMaxWidth(Double.MAX_VALUE);
        signIn.setOnAction(e -> login());

        Label userLabel = new Label("Username");
        userLabel.getStyleClass().add("form-label");
        Label passLabel = new Label("Password");
        passLabel.getStyleClass().add("form-label");

        VBox card = new VBox(10, clinic, subtitle, userLabel, username, passLabel, password, error, signIn);
        card.getStyleClass().add("card");
        card.setPadding(new Insets(32));
        card.setMaxWidth(380);
        card.setMaxHeight(VBox.USE_PREF_SIZE);
        getChildren().add(card);
    }

    private void login() {
        String name = username.getText().trim();
        if (name.isEmpty() || password.getText().isEmpty()) {
            error.setText("Please enter your username and password.");
            return;
        }
        try {
            User user = auth.authenticate(name, password.getText());
            AppWindow.showMain(stage, user);
        } catch (SecurityException e) {
            error.setText(e.getMessage());
            password.clear();
        } catch (RuntimeException e) {
            error.setText(Ui.message(e));
        }
    }
}
