package com.dentalclinic.ui;

import com.dentalclinic.model.Invoice;
import com.dentalclinic.model.InvoiceItem;
import com.dentalclinic.model.Payment;
import com.dentalclinic.service.BillingService;
import com.dentalclinic.utils.Fmt;
import com.dentalclinic.utils.Session;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/** Shows one invoice with its lines and payments, and lets the user record payments. */
final class InvoiceDetailDialog {
    private final BillingService billing = new BillingService();
    private final int invoiceId;
    private Invoice invoice;

    private final Label title = Ui.sectionTitle("");
    private final Label subtitle = Ui.muted("");
    private final Label totals = new Label();
    private final TableView<InvoiceItem> items = Ui.table();
    private final TableView<Payment> payments = Ui.table();

    private InvoiceDetailDialog(int invoiceId) {
        this.invoiceId = invoiceId;
    }

    static void show(int invoiceId) {
        new InvoiceDetailDialog(invoiceId).open();
    }

    private void open() {
        Ui.addColumn(items, "Description", 330, InvoiceItem::getDescription);
        Ui.addColumn(items, "Qty", 60, i -> String.valueOf(i.getQuantity()));
        Ui.addColumn(items, "Unit price", 120, i -> Fmt.money(i.getUnitPrice()));
        Ui.addColumn(items, "Total", 120, i -> Fmt.money(i.getLineTotal()));
        items.setPrefHeight(170);

        Ui.addColumn(payments, "Date", 100, p -> Fmt.date(p.getPaidOn()));
        Ui.addColumn(payments, "Method", 110, p -> Ui.pretty(p.getMethod()));
        Ui.addColumn(payments, "Amount", 130, p -> Fmt.money(p.getAmount()));
        Ui.addColumn(payments, "Note", 300, p -> Fmt.nullToEmpty(p.getNote()));
        payments.setPrefHeight(130);

        reload();

        HBox buttons = new HBox(10, Ui.button("+ Add payment", "primary", () -> {
            if (PaymentDialog.show(invoice)) {
                reload();
            }
        }));
        if (Session.isAdmin()) {
            buttons.getChildren().add(Ui.button("Delete selected payment", "danger", this::deletePayment));
        }

        VBox content = new VBox(10, title, subtitle,
                Ui.sectionTitle("Lines"), items,
                Ui.sectionTitle("Payments"), payments,
                totals, buttons);
        content.setPadding(new Insets(10));
        content.setPrefWidth(740);
        Ui.showContent("Invoice " + invoice.getNumber(), content);
    }

    private void reload() {
        invoice = billing.findInvoice(invoiceId);
        title.setText(invoice.getNumber() + "  -  " + invoice.getPatientName());
        String notes = invoice.getNotes() == null ? "" : "   |   " + invoice.getNotes();
        subtitle.setText("Issued on " + Fmt.date(invoice.getInvoiceDate()) + notes);
        items.setItems(Ui.items(invoice.getItems()));
        payments.setItems(Ui.items(invoice.getPayments()));
        totals.setText("Total: " + Fmt.money(invoice.getTotalAmount())
                + "     Paid: " + Fmt.money(invoice.getPaidAmount())
                + "     Balance: " + Fmt.money(invoice.getBalance())
                + "     Status: " + Ui.pretty(invoice.getStatus()));
        totals.getStyleClass().add("section-title");
    }

    private void deletePayment() {
        Payment p = payments.getSelectionModel().getSelectedItem();
        if (p == null) {
            throw new IllegalStateException("Please select a payment first.");
        }
        if (Ui.confirm("Delete payment", "Delete the payment of " + Fmt.money(p.getAmount()) + "?")) {
            billing.deletePayment(p.getPaymentId());
            reload();
        }
    }
}
