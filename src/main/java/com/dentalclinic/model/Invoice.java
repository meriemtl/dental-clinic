package com.dentalclinic.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Invoice {
    public enum InvoiceStatus {
        UNPAID, PARTIAL, PAID
    }

    private int invoiceId;
    private int patientId;
    private LocalDate invoiceDate;
    private BigDecimal totalAmount = BigDecimal.ZERO;
    private BigDecimal paidAmount = BigDecimal.ZERO;
    private String notes;
    private Integer createdBy;
    private String patientName;
    private List<InvoiceItem> items = new ArrayList<>();
    private List<Payment> payments = new ArrayList<>();

    public Invoice() {
    }

    public int getInvoiceId() { return invoiceId; }
    public void setInvoiceId(int invoiceId) { this.invoiceId = invoiceId; }
    public int getPatientId() { return patientId; }
    public void setPatientId(int patientId) { this.patientId = patientId; }
    public LocalDate getInvoiceDate() { return invoiceDate; }
    public void setInvoiceDate(LocalDate invoiceDate) { this.invoiceDate = invoiceDate; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public BigDecimal getPaidAmount() { return paidAmount; }
    public void setPaidAmount(BigDecimal paidAmount) { this.paidAmount = paidAmount; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public Integer getCreatedBy() { return createdBy; }
    public void setCreatedBy(Integer createdBy) { this.createdBy = createdBy; }
    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }
    public List<InvoiceItem> getItems() { return items; }
    public void setItems(List<InvoiceItem> items) { this.items = items; }
    public List<Payment> getPayments() { return payments; }
    public void setPayments(List<Payment> payments) { this.payments = payments; }

    public BigDecimal getBalance() {
        return totalAmount.subtract(paidAmount);
    }

    public InvoiceStatus getStatus() {
        if (paidAmount.signum() <= 0) {
            return totalAmount.signum() <= 0 ? InvoiceStatus.PAID : InvoiceStatus.UNPAID;
        }
        return getBalance().signum() <= 0 ? InvoiceStatus.PAID : InvoiceStatus.PARTIAL;
    }

    /** Human readable number, e.g. INV-000042. */
    public String getNumber() {
        return String.format("INV-%06d", invoiceId);
    }
}
