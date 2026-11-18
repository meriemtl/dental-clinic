package com.dentalclinic.ui;

import com.dentalclinic.model.Patient;
import com.dentalclinic.service.PatientService;
import com.dentalclinic.utils.Fmt;
import com.dentalclinic.utils.Session;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.List;

final class PatientsView extends BorderPane {
    private final PatientService service = new PatientService();
    private final TableView<Patient> table = Ui.table();
    private final TextField search = new TextField();
    private final Label count = Ui.muted("");

    PatientsView() {
        setPadding(new Insets(24));

        Button add = Ui.button("+ New patient", "primary", () -> {
            if (PatientFormDialog.show(null)) {
                refresh();
            }
        });
        HBox header = new HBox(12, Ui.pageTitle("Patients"), Ui.spacer(), add);
        header.setAlignment(Pos.CENTER_LEFT);

        search.setPromptText("Search by name, phone or e-mail...");
        search.setPrefWidth(360);
        search.textProperty().addListener((obs, oldValue, newValue) -> refresh());

        VBox top = new VBox(12, header, search);
        top.setPadding(new Insets(0, 0, 14, 0));
        setTop(top);

        Ui.addColumn(table, "ID", 50, p -> String.valueOf(p.getPatientId()));
        Ui.addColumn(table, "Name", 200, Patient::getFullName);
        Ui.addColumn(table, "Date of birth", 110, p -> Fmt.date(p.getDateOfBirth()));
        Ui.addColumn(table, "Age", 50, p -> String.valueOf(p.getAge()));
        Ui.addColumn(table, "Gender", 80, p -> Ui.pretty(p.getGender()));
        Ui.addColumn(table, "Phone", 130, p -> Fmt.nullToEmpty(p.getPhoneNumber()));
        Ui.addColumn(table, "E-mail", 210, p -> Fmt.nullToEmpty(p.getEmail()));
        Ui.addColumn(table, "Blood", 60, p -> Fmt.nullToEmpty(p.getBloodType()));
        Ui.addColumn(table, "Allergies", 160, p -> Fmt.nullToEmpty(p.getAllergies()));
        setCenter(table);

        Button open = Ui.button("Open patient file", "primary", () -> new PatientFileDialog(selected()).show());
        Button edit = Ui.button("Edit", "secondary", () -> {
            if (PatientFormDialog.show(selected())) {
                refresh();
            }
        });
        HBox actions = new HBox(10, open, edit);
        if (Session.isAdmin()) {
            actions.getChildren().add(Ui.button("Delete", "danger", this::deleteSelected));
        }
        actions.getChildren().addAll(Ui.spacer(), count);
        actions.setAlignment(Pos.CENTER_LEFT);
        actions.setPadding(new Insets(14, 0, 0, 0));
        setBottom(actions);

        refresh();
    }

    private void refresh() {
        Ui.run(() -> {
            List<Patient> rows = service.searchPatients(search.getText());
            table.setItems(Ui.items(rows));
            count.setText(rows.size() + " patient(s)");
        });
    }

    private Patient selected() {
        Patient p = table.getSelectionModel().getSelectedItem();
        if (p == null) {
            throw new IllegalStateException("Please select a patient in the list first.");
        }
        return p;
    }

    private void deleteSelected() {
        Patient p = selected();
        if (Ui.confirm("Delete patient", "Delete " + p.getFullName() + "?\n\n"
                + "Their appointments, treatments and invoices will be deleted too. "
                + "This cannot be undone.")) {
            service.deletePatient(p.getPatientId());
            refresh();
        }
    }
}
