package com.dentalclinic.ui;

import com.dentalclinic.model.Appointment;
import com.dentalclinic.model.Invoice;
import com.dentalclinic.model.Patient;
import com.dentalclinic.model.TreatmentRecord;
import com.dentalclinic.service.AppointmentService;
import com.dentalclinic.service.BillingService;
import com.dentalclinic.service.TreatmentService;
import com.dentalclinic.utils.Fmt;
import com.dentalclinic.utils.Session;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/** The complete file of one patient: information, appointments, treatments and invoices. */
final class PatientFileDialog {
    private final Patient patient;
    private final TreatmentService treatments = new TreatmentService();
    private final TableView<TreatmentRecord> treatmentTable = Ui.table();

    PatientFileDialog(Patient patient) {
        this.patient = patient;
    }

    void show() {
        TabPane tabs = new TabPane();
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabs.getTabs().addAll(
                tab("Information", infoPane()),
                tab("Appointments", appointmentsPane()),
                tab("Treatments", treatmentsPane()),
                tab("Invoices", invoicesPane()));
        tabs.setPrefSize(860, 480);
        Ui.showContent("Patient file - " + patient.getFullName(), tabs);
    }

    private static Tab tab(String title, Node content) {
        Tab t = new Tab(title);
        t.setContent(content);
        t.setClosable(false);
        return t;
    }

    // ------------------------------------------------------------------ information

    private Node infoPane() {
        GridPane grid = Ui.form();
        grid.setHgap(20);
        grid.setVgap(12);
        grid.setPadding(new Insets(20));
        int row = 0;
        row = info(grid, row, "Name", patient.getFullName());
        row = info(grid, row, "Date of birth", Fmt.date(patient.getDateOfBirth()) + "  (" + patient.getAge() + " years)");
        row = info(grid, row, "Gender", Ui.pretty(patient.getGender()));
        row = info(grid, row, "Phone", Fmt.nullToEmpty(patient.getPhoneNumber()));
        row = info(grid, row, "E-mail", Fmt.nullToEmpty(patient.getEmail()));
        row = info(grid, row, "Address", Fmt.nullToEmpty(patient.getAddress()));
        row = info(grid, row, "Blood type", Fmt.nullToEmpty(patient.getBloodType()));
        row = info(grid, row, "Allergies", Fmt.nullToEmpty(patient.getAllergies()));
        info(grid, row, "Medical history", Fmt.nullToEmpty(patient.getMedicalHistory()));
        return grid;
    }

    private static int info(GridPane grid, int row, String label, String value) {
        Label l = new Label(label);
        l.getStyleClass().add("form-label");
        l.setMinWidth(110);
        Label v = new Label(value);
        v.setWrapText(true);
        v.setMaxWidth(640);
        grid.add(l, 0, row);
        grid.add(v, 1, row);
        return row + 1;
    }

    // ------------------------------------------------------------------ appointments

    private Node appointmentsPane() {
        TableView<Appointment> table = Ui.table();
        Ui.addColumn(table, "Date", 100, a -> Fmt.date(a.getAppointmentDate()));
        Ui.addColumn(table, "Time", 120, a -> Fmt.time(a.getStartTime()) + " - " + Fmt.time(a.getEndTime()));
        Ui.addColumn(table, "Dentist", 180, Appointment::getDentistName);
        Ui.addColumn(table, "Reason", 200, a -> Fmt.nullToEmpty(a.getReason()));
        Ui.addColumn(table, "Status", 100, a -> Ui.pretty(a.getStatus()));
        table.setItems(Ui.items(new AppointmentService().findAppointmentsByPatientId(patient.getPatientId())));
        return wrap(table, null);
    }

    // ------------------------------------------------------------------ treatments

    private Node treatmentsPane() {
        Ui.addColumn(treatmentTable, "Date", 100, r -> Fmt.date(r.getPerformedOn()));
        Ui.addColumn(treatmentTable, "Tooth", 60, r -> Fmt.nullToEmpty(r.getToothNumber()));
        Ui.addColumn(treatmentTable, "Treatment", 200, TreatmentRecord::getTreatmentName);
        Ui.addColumn(treatmentTable, "Dentist", 160, TreatmentRecord::getDentistName);
        Ui.addColumn(treatmentTable, "Price", 110, r -> Fmt.money(r.getPrice()));
        Ui.addColumn(treatmentTable, "Billed", 60, r -> r.isInvoiced() ? "Yes" : "No");
        Ui.addColumn(treatmentTable, "Notes", 200, r -> Fmt.nullToEmpty(r.getNotes()));
        reloadTreatments();

        HBox buttons = new HBox(10);
        if (Session.canRecordTreatments()) {
            buttons.getChildren().add(Ui.button("+ Record treatment", "primary", () -> {
                if (TreatmentRecordDialog.show(patient)) {
                    reloadTreatments();
                }
            }));
            buttons.getChildren().add(Ui.button("Delete", "danger", () -> {
                TreatmentRecord r = treatmentTable.getSelectionModel().getSelectedItem();
                if (r == null) {
                    throw new IllegalStateException("Please select a treatment first.");
                }
                if (Ui.confirm("Delete treatment", "Delete \"" + r.getTreatmentName() + "\" from the file?")) {
                    treatments.deleteRecord(r);
                    reloadTreatments();
                }
            }));
        } else {
            buttons.getChildren().add(Ui.muted("Only dentists and administrators can record treatments."));
        }
        return wrap(treatmentTable, buttons);
    }

    private void reloadTreatments() {
        treatmentTable.setItems(Ui.items(treatments.findRecordsByPatient(patient.getPatientId())));
    }

    // ------------------------------------------------------------------ invoices

    private Node invoicesPane() {
        TableView<Invoice> table = Ui.table();
        BillingService billing = new BillingService();
        Ui.addColumn(table, "Number", 110, Invoice::getNumber);
        Ui.addColumn(table, "Date", 100, i -> Fmt.date(i.getInvoiceDate()));
        Ui.addColumn(table, "Total", 120, i -> Fmt.money(i.getTotalAmount()));
        Ui.addColumn(table, "Paid", 120, i -> Fmt.money(i.getPaidAmount()));
        Ui.addColumn(table, "Balance", 120, i -> Fmt.money(i.getBalance()));
        Ui.addColumn(table, "Status", 90, i -> Ui.pretty(i.getStatus()));
        table.setItems(Ui.items(billing.findInvoicesByPatient(patient.getPatientId())));

        HBox buttons = new HBox(10, Ui.button("View invoice / payments", "secondary", () -> {
            Invoice inv = table.getSelectionModel().getSelectedItem();
            if (inv == null) {
                throw new IllegalStateException("Please select an invoice first.");
            }
            InvoiceDetailDialog.show(inv.getInvoiceId());
            table.setItems(Ui.items(billing.findInvoicesByPatient(patient.getPatientId())));
        }));
        return wrap(table, buttons);
    }

    // ------------------------------------------------------------------ layout helper

    private static Node wrap(TableView<?> table, HBox buttons) {
        VBox box = new VBox(10, table);
        box.setPadding(new Insets(14));
        VBox.setVgrow(table, Priority.ALWAYS);
        if (buttons != null) {
            buttons.setAlignment(Pos.CENTER_LEFT);
            box.getChildren().add(buttons);
        }
        return box;
    }
}
