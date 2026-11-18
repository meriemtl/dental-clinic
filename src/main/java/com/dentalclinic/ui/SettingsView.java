package com.dentalclinic.ui;

import com.dentalclinic.model.User;
import com.dentalclinic.service.AuthenticationService;
import com.dentalclinic.utils.AppConfig;
import com.dentalclinic.utils.Fmt;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

final class SettingsView extends VBox {

    SettingsView(User user) {
        setSpacing(18);
        setPadding(new Insets(24));

        PasswordField current = new PasswordField();
        PasswordField next = new PasswordField();
        PasswordField confirm = new PasswordField();
        GridPane grid = Ui.form();
        Ui.addRow(grid, 0, "Current password", current);
        Ui.addRow(grid, 1, "New password", next);
        Ui.addRow(grid, 2, "Confirm new password", confirm);

        Button change = Ui.button("Change password", "primary", () -> {
            if (!next.getText().equals(confirm.getText())) {
                throw new IllegalArgumentException("The new passwords do not match.");
            }
            try {
                new AuthenticationService().changePassword(user, current.getText(), next.getText());
            } catch (SecurityException e) {
                throw new IllegalArgumentException(e.getMessage());
            }
            current.clear();
            next.clear();
            confirm.clear();
            Ui.info("Your password has been changed.");
        });
        VBox passwordCard = new VBox(12, Ui.sectionTitle("Change my password"), grid, change);
        passwordCard.getStyleClass().add("card");
        passwordCard.setPadding(new Insets(20));
        passwordCard.setMaxWidth(520);

        VBox infoCard = new VBox(8,
                Ui.sectionTitle("Application"),
                new Label("Signed in as: " + user.getUsername() + " (" + Ui.pretty(user.getRole()) + ")"),
                new Label("Database: " + AppConfig.dbHost() + ":" + AppConfig.dbPort() + " / " + AppConfig.dbName()),
                new Label("Currency: " + AppConfig.currency()),
                new Label("Agenda: " + Fmt.time(AppConfig.agendaStart()) + " - " + Fmt.time(AppConfig.agendaEnd())
                        + ", slots of " + AppConfig.agendaSlotMinutes() + " minutes"),
                Ui.muted("These values come from config.properties. Edit that file and restart the application to change them."));
        infoCard.getStyleClass().add("card");
        infoCard.setPadding(new Insets(20));
        infoCard.setMaxWidth(520);

        getChildren().addAll(Ui.pageTitle("Settings"), passwordCard, infoCard);
    }
}
