package com.dentalclinic.ui;

import com.dentalclinic.model.User;
import com.dentalclinic.utils.AppConfig;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/** Main window: navigation bar on top, the selected screen in the centre. */
final class MainView extends BorderPane {
    private final Stage stage;
    private final HBox nav = new HBox(6);
    private final List<Button> navButtons = new ArrayList<>();

    MainView(Stage stage, User user) {
        this.stage = stage;

        Label title = new Label(AppConfig.clinicName());
        title.getStyleClass().add("app-title");

        Button first = addNav("Dashboard", DashboardView::new);
        addNav("Patients", PatientsView::new);
        addNav("Appointments", AppointmentsView::new);
        addNav("Treatments", TreatmentsView::new);
        addNav("Billing", BillingView::new);
        if (user.isAdmin()) {
            addNav("Staff", StaffView::new);
        }
        addNav("Settings", () -> new SettingsView(user));

        Label who = new Label(user.getFullName() + "  |  " + Ui.pretty(user.getRole()));
        Button logout = Ui.button("Logout", "danger", this::logout);

        HBox bar = new HBox(20, title, nav, Ui.spacer(), who, logout);
        bar.getStyleClass().add("top-bar");
        bar.setAlignment(Pos.CENTER_LEFT);
        setTop(bar);

        select(first, DashboardView::new);
    }

    private Button addNav(String text, Supplier<Node> factory) {
        Button b = new Button(text);
        b.getStyleClass().add("nav-button");
        b.setOnAction(e -> select(b, factory));
        nav.getChildren().add(b);
        navButtons.add(b);
        return b;
    }

    private void select(Button selected, Supplier<Node> factory) {
        Ui.run(() -> {
            Node view = factory.get();
            for (Button b : navButtons) {
                b.getStyleClass().remove("nav-selected");
            }
            selected.getStyleClass().add("nav-selected");
            setCenter(view);
        });
    }

    private void logout() {
        if (Ui.confirm("Logout", "Do you want to log out?")) {
            AppWindow.showLogin(stage);
        }
    }
}
