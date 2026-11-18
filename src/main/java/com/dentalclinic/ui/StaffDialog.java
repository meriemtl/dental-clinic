package com.dentalclinic.ui;

import com.dentalclinic.model.User;
import com.dentalclinic.model.User.UserRole;
import com.dentalclinic.service.UserService;
import com.dentalclinic.utils.Session;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

import java.util.List;

/** Create / edit form for a user account. */
final class StaffDialog {
    private StaffDialog() {
    }

    /** @return true when the account was saved */
    static boolean show(User existing) {
        UserService service = new UserService();
        TextField username = new TextField();
        TextField fullName = new TextField();
        ComboBox<UserRole> role = Ui.combo(List.of(UserRole.values()), Ui::pretty);
        PasswordField password = new PasswordField();
        PasswordField confirm = new PasswordField();
        CheckBox active = new CheckBox("Account is active (can sign in)");
        active.setSelected(true);

        GridPane grid = Ui.form();
        int row = 0;
        Ui.addRow(grid, row++, "Username *", username);
        Ui.addRow(grid, row++, "Full name *", fullName);
        Ui.addRow(grid, row++, "Role *", role);
        if (existing == null) {
            role.setValue(UserRole.STAFF);
            Ui.addRow(grid, row++, "Password *", password);
            Ui.addRow(grid, row, "Confirm *", confirm);
        } else {
            username.setText(existing.getUsername());
            username.setDisable(true);
            fullName.setText(existing.getFullName());
            role.setValue(existing.getRole());
            active.setSelected(existing.isActive());
            Ui.addRow(grid, row, "", active);
        }
        grid.setPrefWidth(420);

        return Ui.showForm(existing == null ? "New user" : "Edit user", "Save", grid, () -> {
            if (existing == null) {
                if (!password.getText().equals(confirm.getText())) {
                    throw new IllegalArgumentException("The passwords do not match.");
                }
                service.createUser(username.getText(), fullName.getText(), role.getValue(), password.getText());
                return;
            }
            User me = Session.getUser();
            boolean editingMyself = me != null && me.getUserId() == existing.getUserId();
            if (editingMyself && (role.getValue() != UserRole.ADMIN || !active.isSelected())) {
                throw new IllegalArgumentException(
                        "You cannot remove your own administrator access or deactivate your own account.");
            }
            service.updateUser(existing, fullName.getText(), role.getValue(), active.isSelected());
        });
    }
}
