package com.dentalclinic.ui;

import com.dentalclinic.model.User;
import com.dentalclinic.service.UserService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/** User accounts (administrator only). */
final class StaffView extends BorderPane {
    private final UserService service = new UserService();
    private final TableView<User> table = Ui.table();

    StaffView() {
        setPadding(new Insets(24));

        VBox top = new VBox(6, Ui.pageTitle("Staff accounts"),
                Ui.muted("Administrators manage everything, dentists record treatments, "
                        + "staff members handle patients, appointments and billing."));
        top.setPadding(new Insets(0, 0, 14, 0));
        setTop(top);

        Ui.addColumn(table, "Username", 160, User::getUsername);
        Ui.addColumn(table, "Full name", 260, User::getFullName);
        Ui.addColumn(table, "Role", 120, u -> Ui.pretty(u.getRole()));
        Ui.addColumn(table, "Status", 100, u -> u.isActive() ? "Active" : "Inactive");
        setCenter(table);

        HBox actions = new HBox(10,
                Ui.button("+ New user", "primary", () -> {
                    if (StaffDialog.show(null)) {
                        refresh();
                    }
                }),
                Ui.button("Edit", "secondary", () -> {
                    if (StaffDialog.show(selected())) {
                        refresh();
                    }
                }),
                Ui.button("Reset password", "secondary", this::resetPassword));
        actions.setAlignment(Pos.CENTER_LEFT);
        actions.setPadding(new Insets(14, 0, 0, 0));
        setBottom(actions);

        refresh();
    }

    private void refresh() {
        Ui.run(() -> table.setItems(Ui.items(service.findAllUsers())));
    }

    private User selected() {
        User u = table.getSelectionModel().getSelectedItem();
        if (u == null) {
            throw new IllegalStateException("Please select a user in the list first.");
        }
        return u;
    }

    private void resetPassword() {
        User user = selected();
        PasswordField password = new PasswordField();
        PasswordField confirm = new PasswordField();
        GridPane grid = Ui.form();
        Ui.addRow(grid, 0, "New password *", password);
        Ui.addRow(grid, 1, "Confirm *", confirm);
        grid.setPrefWidth(360);
        boolean saved = Ui.showForm("Reset password - " + user.getUsername(), "Save", grid, () -> {
            if (!password.getText().equals(confirm.getText())) {
                throw new IllegalArgumentException("The passwords do not match.");
            }
            service.resetPassword(user, password.getText());
        });
        if (saved) {
            Ui.info("The password of " + user.getUsername() + " has been changed.");
        }
    }
}
