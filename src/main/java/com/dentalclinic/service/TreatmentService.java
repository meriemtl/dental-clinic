package com.dentalclinic.service;

import com.dentalclinic.dao.TreatmentRecordDAO;
import com.dentalclinic.dao.TreatmentRecordDAOImpl;
import com.dentalclinic.dao.TreatmentTypeDAO;
import com.dentalclinic.dao.TreatmentTypeDAOImpl;
import com.dentalclinic.model.TreatmentRecord;
import com.dentalclinic.model.TreatmentType;

import java.math.BigDecimal;
import java.util.List;

/** Price list (treatment types) and the treatments performed on each patient. */
public class TreatmentService {
    private final TreatmentTypeDAO typeDAO;
    private final TreatmentRecordDAO recordDAO;

    public TreatmentService() {
        this(new TreatmentTypeDAOImpl(), new TreatmentRecordDAOImpl());
    }

    public TreatmentService(TreatmentTypeDAO typeDAO, TreatmentRecordDAO recordDAO) {
        this.typeDAO = typeDAO;
        this.recordDAO = recordDAO;
    }

    public List<TreatmentType> findAllTypes() {
        return typeDAO.findAll();
    }

    public List<TreatmentType> findActiveTypes() {
        return typeDAO.findActive();
    }

    public TreatmentType saveType(TreatmentType type) {
        if (type.getName() == null || type.getName().isBlank()) {
            throw new IllegalArgumentException("The treatment name is required.");
        }
        if (type.getDefaultPrice() == null || type.getDefaultPrice().signum() < 0) {
            throw new IllegalArgumentException("The price cannot be negative.");
        }
        return typeDAO.save(type);
    }

    public List<TreatmentRecord> findRecordsByPatient(int patientId) {
        return recordDAO.findByPatient(patientId);
    }

    public List<TreatmentRecord> findUnbilledRecords(int patientId) {
        return recordDAO.findUnbilledByPatient(patientId);
    }

    public TreatmentRecord saveRecord(TreatmentRecord record) {
        if (record.getPatientId() == 0 || record.getUserId() == 0) {
            throw new IllegalArgumentException("A patient and a dentist are required.");
        }
        if (record.getTreatmentName() == null || record.getTreatmentName().isBlank()) {
            throw new IllegalArgumentException("Please choose a treatment.");
        }
        if (record.getPerformedOn() == null) {
            throw new IllegalArgumentException("The date is required.");
        }
        if (record.getPrice() == null || record.getPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("The price cannot be negative.");
        }
        return recordDAO.save(record);
    }

    public void deleteRecord(TreatmentRecord record) {
        if (record.isInvoiced()) {
            throw new IllegalStateException("This treatment is already on an invoice and cannot be deleted.");
        }
        recordDAO.delete(record.getRecordId());
    }
}
