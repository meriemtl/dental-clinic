package com.dentalclinic.service;

import com.dentalclinic.dao.InvoiceDAO;
import com.dentalclinic.dao.InvoiceDAOImpl;
import com.dentalclinic.model.Invoice;
import com.dentalclinic.model.InvoiceItem;
import com.dentalclinic.model.Payment;
import com.dentalclinic.model.Payment.PaymentMethod;
import com.dentalclinic.model.TreatmentRecord;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class BillingService {
    private final InvoiceDAO invoiceDAO;

    public BillingService() {
        this(new InvoiceDAOImpl());
    }

    public BillingService(InvoiceDAO invoiceDAO) {
        this.invoiceDAO = invoiceDAO;
    }

    public List<Invoice> findAllInvoices() {
        return invoiceDAO.findAll();
    }

    public List<Invoice> findInvoicesByPatient(int patientId) {
        return invoiceDAO.findByPatient(patientId);
    }

    public Invoice findInvoice(int invoiceId) {
        return invoiceDAO.findById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Invoice not found with ID: " + invoiceId));
    }

    /**
     * Creates an invoice from treatments already recorded for the patient plus optional extra lines.
     * The treatments are marked as billed so they cannot be invoiced twice.
     */
    public Invoice createInvoice(int patientId, List<TreatmentRecord> treatments,
                                 List<InvoiceItem> extraItems, String notes, Integer createdBy) {
        if (patientId == 0) {
            throw new IllegalArgumentException("Please select a patient.");
        }
        List<InvoiceItem> items = new ArrayList<>();
        List<Integer> recordIds = new ArrayList<>();
        for (TreatmentRecord r : treatments) {
            String label = r.getToothNumber() == null || r.getToothNumber().isBlank()
                    ? r.getTreatmentName()
                    : r.getTreatmentName() + " (tooth " + r.getToothNumber() + ")";
            items.add(new InvoiceItem(label, 1, r.getPrice()));
            recordIds.add(r.getRecordId());
        }
        items.addAll(extraItems);
        if (items.isEmpty()) {
            throw new IllegalArgumentException("Select at least one treatment or add a line to the invoice.");
        }
        BigDecimal total = BigDecimal.ZERO;
        for (InvoiceItem item : items) {
            total = total.add(item.getLineTotal());
        }

        Invoice invoice = new Invoice();
        invoice.setPatientId(patientId);
        invoice.setInvoiceDate(LocalDate.now());
        invoice.setItems(items);
        invoice.setTotalAmount(total);
        invoice.setNotes(notes == null || notes.isBlank() ? null : notes.trim());
        invoice.setCreatedBy(createdBy);
        return invoiceDAO.create(invoice, recordIds);
    }

    public void deleteInvoice(int invoiceId) {
        invoiceDAO.delete(invoiceId);
    }

    public Payment recordPayment(int invoiceId, BigDecimal amount, PaymentMethod method,
                                 LocalDate date, String note) {
        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException("The amount must be greater than zero.");
        }
        if (method == null || date == null) {
            throw new IllegalArgumentException("Payment method and date are required.");
        }
        Invoice invoice = findInvoice(invoiceId);
        if (amount.compareTo(invoice.getBalance()) > 0) {
            throw new IllegalArgumentException(
                    "The amount is higher than the remaining balance of this invoice.");
        }
        Payment payment = new Payment();
        payment.setInvoiceId(invoiceId);
        payment.setAmount(amount);
        payment.setMethod(method);
        payment.setPaidOn(date);
        payment.setNote(note == null || note.isBlank() ? null : note.trim());
        return invoiceDAO.addPayment(payment);
    }

    public void deletePayment(int paymentId) {
        invoiceDAO.deletePayment(paymentId);
    }
}
