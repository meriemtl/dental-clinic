package com.dentalclinic.dao;

import com.dentalclinic.model.Invoice;
import com.dentalclinic.model.Payment;

import java.util.List;
import java.util.Optional;

public interface InvoiceDAO {
    List<Invoice> findAll();
    List<Invoice> findByPatient(int patientId);
    /** Loads one invoice together with its items and payments. */
    Optional<Invoice> findById(int invoiceId);
    /** Saves the invoice and its items, and marks the given treatment records as billed (one transaction). */
    Invoice create(Invoice invoice, List<Integer> treatmentRecordIds);
    void delete(int invoiceId);
    Payment addPayment(Payment payment);
    void deletePayment(int paymentId);
}
