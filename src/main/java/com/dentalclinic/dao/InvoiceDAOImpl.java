package com.dentalclinic.dao;

import com.dentalclinic.model.Invoice;
import com.dentalclinic.model.InvoiceItem;
import com.dentalclinic.model.Payment;
import com.dentalclinic.utils.DatabaseConnection;
import com.dentalclinic.utils.Jdbc;
import com.dentalclinic.utils.Jdbc.Mapper;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class InvoiceDAOImpl implements InvoiceDAO {

    private static final String SELECT = """
            SELECT i.invoice_id, i.patient_id, i.invoice_date, i.total_amount, i.notes, i.created_by,
                   p.first_name, p.last_name,
                   COALESCE((SELECT SUM(pay.amount) FROM payments pay
                             WHERE pay.invoice_id = i.invoice_id), 0) AS paid_amount
            FROM invoices i
            JOIN patients p ON i.patient_id = p.patient_id
            """;
    private static final String FIND_ALL = SELECT + "ORDER BY i.invoice_date DESC, i.invoice_id DESC";
    private static final String FIND_BY_PATIENT = SELECT
            + "WHERE i.patient_id = ? ORDER BY i.invoice_date DESC, i.invoice_id DESC";
    private static final String FIND_BY_ID = SELECT + "WHERE i.invoice_id = ?";

    private static final String FIND_ITEMS = """
            SELECT item_id, invoice_id, description, quantity, unit_price
            FROM invoice_items WHERE invoice_id = ? ORDER BY item_id
            """;
    private static final String FIND_PAYMENTS = """
            SELECT payment_id, invoice_id, amount, method, paid_on, note
            FROM payments WHERE invoice_id = ? ORDER BY paid_on, payment_id
            """;

    private static final String INSERT_INVOICE = """
            INSERT INTO invoices (patient_id, invoice_date, total_amount, notes, created_by)
            VALUES (?, ?, ?, ?, ?)
            """;
    private static final String INSERT_ITEM = """
            INSERT INTO invoice_items (invoice_id, description, quantity, unit_price)
            VALUES (?, ?, ?, ?)
            """;
    private static final String MARK_RECORD_BILLED =
            "UPDATE treatment_records SET invoice_id = ? WHERE record_id = ? AND invoice_id IS NULL";
    private static final String DELETE_INVOICE = "DELETE FROM invoices WHERE invoice_id = ?";
    private static final String INSERT_PAYMENT = """
            INSERT INTO payments (invoice_id, amount, method, paid_on, note) VALUES (?, ?, ?, ?, ?)
            """;
    private static final String DELETE_PAYMENT = "DELETE FROM payments WHERE payment_id = ?";

    private static final Mapper<Invoice> INVOICE_MAPPER = rs -> {
        Invoice inv = new Invoice();
        inv.setInvoiceId(rs.getInt("invoice_id"));
        inv.setPatientId(rs.getInt("patient_id"));
        inv.setInvoiceDate(rs.getObject("invoice_date", LocalDate.class));
        inv.setTotalAmount(rs.getBigDecimal("total_amount"));
        inv.setPaidAmount(rs.getBigDecimal("paid_amount"));
        inv.setNotes(rs.getString("notes"));
        inv.setCreatedBy(Jdbc.nullableInt(rs, "created_by"));
        inv.setPatientName(rs.getString("last_name") + " " + rs.getString("first_name"));
        return inv;
    };

    private static final Mapper<InvoiceItem> ITEM_MAPPER = rs -> {
        InvoiceItem item = new InvoiceItem();
        item.setItemId(rs.getInt("item_id"));
        item.setInvoiceId(rs.getInt("invoice_id"));
        item.setDescription(rs.getString("description"));
        item.setQuantity(rs.getInt("quantity"));
        item.setUnitPrice(rs.getBigDecimal("unit_price"));
        return item;
    };

    private static final Mapper<Payment> PAYMENT_MAPPER = rs -> {
        Payment p = new Payment();
        p.setPaymentId(rs.getInt("payment_id"));
        p.setInvoiceId(rs.getInt("invoice_id"));
        p.setAmount(rs.getBigDecimal("amount"));
        p.setMethod(Payment.PaymentMethod.valueOf(rs.getString("method")));
        p.setPaidOn(rs.getObject("paid_on", LocalDate.class));
        p.setNote(rs.getString("note"));
        return p;
    };

    @Override
    public List<Invoice> findAll() {
        return Jdbc.query(FIND_ALL, Jdbc.NONE, INVOICE_MAPPER);
    }

    @Override
    public List<Invoice> findByPatient(int patientId) {
        return Jdbc.query(FIND_BY_PATIENT, Jdbc.params(patientId), INVOICE_MAPPER);
    }

    @Override
    public Optional<Invoice> findById(int invoiceId) {
        Optional<Invoice> found = Jdbc.queryOne(FIND_BY_ID, Jdbc.params(invoiceId), INVOICE_MAPPER);
        found.ifPresent(inv -> {
            inv.setItems(Jdbc.query(FIND_ITEMS, Jdbc.params(invoiceId), ITEM_MAPPER));
            inv.setPayments(Jdbc.query(FIND_PAYMENTS, Jdbc.params(invoiceId), PAYMENT_MAPPER));
        });
        return found;
    }

    @Override
    public Invoice create(Invoice invoice, List<Integer> treatmentRecordIds) {
        try (Connection c = DatabaseConnection.getConnection()) {
            c.setAutoCommit(false);
            try {
                int id = Jdbc.insert(c, INSERT_INVOICE, Jdbc.params(invoice.getPatientId(),
                        invoice.getInvoiceDate(), invoice.getTotalAmount(), invoice.getNotes(),
                        invoice.getCreatedBy()));
                invoice.setInvoiceId(id);
                for (InvoiceItem item : invoice.getItems()) {
                    Jdbc.insert(c, INSERT_ITEM, Jdbc.params(id, item.getDescription(), item.getQuantity(),
                            item.getUnitPrice()));
                }
                for (Integer recordId : treatmentRecordIds) {
                    Jdbc.update(c, MARK_RECORD_BILLED, Jdbc.params(id, recordId));
                }
                c.commit();
            } catch (SQLException | RuntimeException e) {
                c.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw Jdbc.wrap(e);
        }
        return invoice;
    }

    @Override
    public void delete(int invoiceId) {
        Jdbc.update(DELETE_INVOICE, Jdbc.params(invoiceId));
    }

    @Override
    public Payment addPayment(Payment p) {
        int id = Jdbc.insert(INSERT_PAYMENT, Jdbc.params(p.getInvoiceId(), p.getAmount(), p.getMethod(),
                p.getPaidOn(), p.getNote()));
        p.setPaymentId(id);
        return p;
    }

    @Override
    public void deletePayment(int paymentId) {
        Jdbc.update(DELETE_PAYMENT, Jdbc.params(paymentId));
    }
}
