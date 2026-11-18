package com.dentalclinic.ui;

import com.dentalclinic.model.InvoiceItem;
import com.dentalclinic.model.Patient;
import com.dentalclinic.model.TreatmentRecord;
import com.dentalclinic.service.BillingService;
import com.dentalclinic.service.PatientService;
import com.dentalclinic.service.TreatmentService;
import com.dentalclinic.utils.Fmt;
import com.dentalclinic.utils.Session;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/** New invoice: choose a patient, pick the treatments to bill and optionally add extra lines. */
final class InvoiceFormDialog {
    private InvoiceFormDialog() {
    }

    /** @return true when the invoice was created */
    static boolean show() {
        TreatmentService treatments = new TreatmentService();
        BillingService billing = new BillingService();
        List<Patient> patients = new PatientService().findAllPatients();
        if (patients.isEmpty()) {
            throw new IllegalStateException("There are no patients yet. Add a patient first.");
        }

        ComboBox<Patient> patient = Ui.combo(patients, Patient::getFullName);

        TableView<TreatmentRecord> unbilled = Ui.table();
        unbilled.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        unbilled.setPrefHeight(150);
        unbilled.setPlaceholder(new Label("Choose a patient to see the treatments that are not invoiced yet."));
        Ui.addColumn(unbilled, "Date", 100, r -> Fmt.date(r.getPerformedOn()));
        Ui.addColumn(unbilled, "Treatment", 250, TreatmentRecord::getTreatmentName);
        Ui.addColumn(unbilled, "Tooth", 70, r -> Fmt.nullToEmpty(r.getToothNumber()));
        Ui.addColumn(unbilled, "Price", 120, r -> Fmt.money(r.getPrice()));

        ObservableList<InvoiceItem> extraItems = FXCollections.observableArrayList();
        TableView<InvoiceItem> extras = Ui.table();
        extras.setItems(extraItems);
        extras.setPrefHeight(110);
        extras.setPlaceholder(new Label("No additional line."));
        Ui.addColumn(extras, "Description", 280, InvoiceItem::getDescription);
        Ui.addColumn(extras, "Qty", 60, i -> String.valueOf(i.getQuantity()));
        Ui.addColumn(extras, "Unit price", 110, i -> Fmt.money(i.getUnitPrice()));
        Ui.addColumn(extras, "Total", 110, i -> Fmt.money(i.getLineTotal()));

        TextField lineDescription = new TextField();
        lineDescription.setPromptText("Description");
        lineDescription.setPrefWidth(260);
        TextField lineQuantity = new TextField("1");
        lineQuantity.setPrefWidth(50);
        TextField linePrice = new TextField();
        linePrice.setPromptText("Unit price");
        linePrice.setPrefWidth(110);
        TextField notes = new TextField();
        notes.setPromptText("Notes (optional)");

        Label total = Ui.sectionTitle("");
        Runnable updateTotal = () -> {
            BigDecimal sum = BigDecimal.ZERO;
            for (TreatmentRecord r : unbilled.getSelectionModel().getSelectedItems()) {
                sum = sum.add(r.getPrice());
            }
            for (InvoiceItem item : extraItems) {
                sum = sum.add(item.getLineTotal());
            }
            total.setText("Invoice total: " + Fmt.money(sum));
        };
        updateTotal.run();

        unbilled.getSelectionModel().getSelectedItems()
                .addListener((ListChangeListener<TreatmentRecord>) change -> updateTotal.run());
        patient.setOnAction(e -> Ui.run(() -> {
            Patient p = patient.getValue();
            List<TreatmentRecord> rows = new ArrayList<>();
            if (p != null) {
                rows = treatments.findUnbilledRecords(p.getPatientId());
            }
            unbilled.setItems(Ui.items(rows));
            updateTotal.run();
        }));

        Button addLine = Ui.button("Add line", "secondary", () -> {
            String description = lineDescription.getText().trim();
            if (description.isEmpty()) {
                throw new IllegalArgumentException("Enter a description for the line.");
            }
            int quantity;
            try {
                quantity = Integer.parseInt(lineQuantity.getText().trim());
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException("The quantity must be a whole number.");
            }
            if (quantity <= 0) {
                throw new IllegalArgumentException("The quantity must be at least 1.");
            }
            BigDecimal price = Fmt.parseMoney(linePrice.getText());
            if (price.signum() < 0) {
                throw new IllegalArgumentException("The price cannot be negative.");
            }
            extraItems.add(new InvoiceItem(description, quantity, price));
            lineDescription.clear();
            lineQuantity.setText("1");
            linePrice.clear();
            updateTotal.run();
        });
        Button removeLine = Ui.button("Remove selected line", "secondary", () -> {
            InvoiceItem selected = extras.getSelectionModel().getSelectedItem();
            if (selected != null) {
                extraItems.remove(selected);
                updateTotal.run();
            }
        });
        HBox lineInputs = new HBox(8, lineDescription, lineQuantity, linePrice, addLine, removeLine);
        lineInputs.setAlignment(Pos.CENTER_LEFT);

        VBox content = new VBox(8,
                Ui.sectionTitle("Patient"), patient,
                Ui.sectionTitle("Treatments to bill (Ctrl/Shift-click to select several)"), unbilled,
                Ui.sectionTitle("Additional lines"), extras, lineInputs,
                notes, total);
        content.setPrefWidth(700);

        return Ui.showForm("New invoice", "Create invoice", content, () -> {
            Patient p = patient.getValue();
            if (p == null) {
                throw new IllegalArgumentException("Please select a patient.");
            }
            billing.createInvoice(p.getPatientId(),
                    new ArrayList<>(unbilled.getSelectionModel().getSelectedItems()),
                    new ArrayList<>(extraItems), notes.getText(), Session.getUser().getUserId());
        });
    }
}
