package com.dentalclinic.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/** A treatment actually performed on a patient (one line of the dental file). */
public class TreatmentRecord {
    private int recordId;
    private int patientId;
    private int userId;
    private Integer typeId;
    private String treatmentName;
    private String toothNumber;
    private String notes;
    private BigDecimal price = BigDecimal.ZERO;
    private LocalDate performedOn;
    private Integer invoiceId;

    // Display-only
    private String dentistName;

    public TreatmentRecord() {
    }

    public int getRecordId() { return recordId; }
    public void setRecordId(int recordId) { this.recordId = recordId; }
    public int getPatientId() { return patientId; }
    public void setPatientId(int patientId) { this.patientId = patientId; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public Integer getTypeId() { return typeId; }
    public void setTypeId(Integer typeId) { this.typeId = typeId; }
    public String getTreatmentName() { return treatmentName; }
    public void setTreatmentName(String treatmentName) { this.treatmentName = treatmentName; }
    public String getToothNumber() { return toothNumber; }
    public void setToothNumber(String toothNumber) { this.toothNumber = toothNumber; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public LocalDate getPerformedOn() { return performedOn; }
    public void setPerformedOn(LocalDate performedOn) { this.performedOn = performedOn; }
    public Integer getInvoiceId() { return invoiceId; }
    public void setInvoiceId(Integer invoiceId) { this.invoiceId = invoiceId; }
    public String getDentistName() { return dentistName; }
    public void setDentistName(String dentistName) { this.dentistName = dentistName; }

    public boolean isInvoiced() {
        return invoiceId != null;
    }
}
