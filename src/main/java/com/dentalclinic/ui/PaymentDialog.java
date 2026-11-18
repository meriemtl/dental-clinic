package com.dentalclinic.ui;

import com.dentalclinic.model.Invoice;
import com.dentalclinic.model.Payment.PaymentMethod;
import com.dentalclinic.service.BillingService;
import com.dentalclinic.utils.Fmt;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

/** Records a payment on an invoice. */
final class PaymentDialog {
    private PaymentDialog() {
    }

    /** @return true when the payment was saved */
    static boolean show(Invoice invoice) {
        if (invoice.getBalance().signum() <= 0) {
            throw new IllegalStateException("This invoice is already fully paid.");
        }
        BillingService billing = new BillingService();

        TextField amount = new TextField(invoice.getBalance().setScale(2, RoundingMode.HALF_UP).toPlainString());
        ComboBox<PaymentMethod> method = Ui.combo(List.of(PaymentMethod.values()), Ui::pretty);
        method.setValue(PaymentMethod.CASH);
        DatePicker date = new DatePicker(LocalDate.now());
        TextField note = new TextField();

        GridPane grid = Ui.form();
        Ui.addRow(grid, 0, "Remaining balance", new Label(Fmt.money(invoice.getBalance())));
        Ui.addRow(grid, 1, "Amount *", amount);
        Ui.addRow(grid, 2, "Method *", method);
        Ui.addRow(grid, 3, "Date *", date);
        Ui.addRow(grid, 4, "Note", note);
        grid.setPrefWidth(380);

        return Ui.showForm("Add payment - " + invoice.getNumber(), "Save payment", grid,
                () -> billing.recordPayment(invoice.getInvoiceId(), Fmt.parseMoney(amount.getText()),
                        method.getValue(), date.getValue(), note.getText()));
    }
}
