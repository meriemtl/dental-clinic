package com.dentalclinic.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Payment {
    public enum PaymentMethod {
        CASH, CARD, CHECK, TRANSFER
    }

    private int paymentId;
    private int invoiceId;
    private BigDecimal amount = BigDecimal.ZERO;
    private PaymentMethod method = PaymentMethod.CASH;
    private LocalDate paidOn;
    private String note;

    public Payment() {
    }

    public int getPaymentId() { return paymentId; }
    public void setPaymentId(int paymentId) { this.paymentId = paymentId; }
    public int getInvoiceId() { return invoiceId; }
    public void setInvoiceId(int invoiceId) { this.invoiceId = invoiceId; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public PaymentMethod getMethod() { return method; }
    public void setMethod(PaymentMethod method) { this.method = method; }
    public LocalDate getPaidOn() { return paidOn; }
    public void setPaidOn(LocalDate paidOn) { this.paidOn = paidOn; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
