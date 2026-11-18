package com.dentalclinic.ui;

import com.dentalclinic.model.Invoice;
import com.dentalclinic.service.BillingService;
import com.dentalclinic.utils.Fmt;
import com.dentalclinic.utils.Session;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.math.BigDecimal;
import java.util.List;

final class BillingView extends BorderPane {
    private final BillingService service = new BillingService();
    private final TableView<Invoice> table = Ui.table();
    private final ComboBox<String> statusFilter = Ui.combo(List.of("All", "Unpaid", "Partial", "Paid"), s -> s);
    private final TextField search = new TextField();
    private final Label summary = Ui.muted("");

    BillingView() {
        setPadding(new Insets(24));

        Button newInvoice = Ui.button("+ New invoice", "primary", () -> {
            if (InvoiceFormDialog.show()) {
                refresh();
            }
        });
        HBox header = new HBox(12, Ui.pageTitle("Billing"), Ui.spacer(), newInvoice);
        header.setAlignment(Pos.CENTER_LEFT);

        search.setPromptText("Search by patient or invoice number...");
        search.setPrefWidth(320);
        statusFilter.setValue("All");
        HBox filters = new HBox(10, search, new Label("Status:"), statusFilter);
        filters.setAlignment(Pos.CENTER_LEFT);
        VBox top = new VBox(12, header, filters);
        top.setPadding(new Insets(0, 0, 14, 0));
        setTop(top);

        search.textProperty().addListener((obs, oldValue, newValue) -> refresh());
        statusFilter.setOnAction(e -> refresh());

        Ui.addColumn(table, "Number", 110, Invoice::getNumber);
        Ui.addColumn(table, "Date", 100, i -> Fmt.date(i.getInvoiceDate()));
        Ui.addColumn(table, "Patient", 220, Invoice::getPatientName);
        Ui.addColumn(table, "Total", 120, i -> Fmt.money(i.getTotalAmount()));
        Ui.addColumn(table, "Paid", 120, i -> Fmt.money(i.getPaidAmount()));
        Ui.addColumn(table, "Balance", 120, i -> Fmt.money(i.getBalance()));
        Ui.addColumn(table, "Status", 90, i -> Ui.pretty(i.getStatus()));
        setCenter(table);

        HBox actions = new HBox(10, Ui.button("Open invoice / payments", "secondary", () -> {
            InvoiceDetailDialog.show(selected().getInvoiceId());
            refresh();
        }));
        if (Session.isAdmin()) {
            actions.getChildren().add(Ui.button("Delete", "danger", this::deleteSelected));
        }
        actions.getChildren().addAll(Ui.spacer(), summary);
        actions.setAlignment(Pos.CENTER_LEFT);
        actions.setPadding(new Insets(14, 0, 0, 0));
        setBottom(actions);

        refresh();
    }

    private void refresh() {
        Ui.run(() -> {
            String text = search.getText() == null ? "" : search.getText().trim().toLowerCase();
            String status = statusFilter.getValue() == null ? "All" : statusFilter.getValue();
            List<Invoice> rows = service.findAllInvoices().stream()
                    .filter(i -> status.equals("All") || Ui.pretty(i.getStatus()).equalsIgnoreCase(status))
                    .filter(i -> text.isEmpty()
                            || i.getPatientName().toLowerCase().contains(text)
                            || i.getNumber().toLowerCase().contains(text))
                    .toList();
            table.setItems(Ui.items(rows));

            BigDecimal total = BigDecimal.ZERO;
            BigDecimal paid = BigDecimal.ZERO;
            for (Invoice i : rows) {
                total = total.add(i.getTotalAmount());
                paid = paid.add(i.getPaidAmount());
            }
            summary.setText(rows.size() + " invoice(s)   |   Invoiced: " + Fmt.money(total)
                    + "   Paid: " + Fmt.money(paid) + "   Outstanding: " + Fmt.money(total.subtract(paid)));
        });
    }

    private Invoice selected() {
        Invoice i = table.getSelectionModel().getSelectedItem();
        if (i == null) {
            throw new IllegalStateException("Please select an invoice in the list first.");
        }
        return i;
    }

    private void deleteSelected() {
        Invoice i = selected();
        if (Ui.confirm("Delete invoice", "Delete invoice " + i.getNumber() + " and its payments?\n"
                + "The treatments it contained become billable again.")) {
            service.deleteInvoice(i.getInvoiceId());
            refresh();
        }
    }
}
