package com.dentalclinic.service;

import com.dentalclinic.dao.PatientDAO;
import com.dentalclinic.dao.PatientDAOImpl;
import com.dentalclinic.model.Patient;

import java.time.LocalDate;
import java.util.List;

public class PatientService {
    private final PatientDAO patientDAO;

    public PatientService() {
        this(new PatientDAOImpl());
    }

    public PatientService(PatientDAO patientDAO) {
        this.patientDAO = patientDAO;
    }

    public Patient findPatientById(int patientId) {
        return patientDAO.findById(patientId)
                .orElseThrow(() -> new RuntimeException("Patient not found with ID: " + patientId));
    }

    public List<Patient> findAllPatients() {
        return patientDAO.findAll();
    }

    public List<Patient> searchPatients(String query) {
        if (query == null || query.isBlank()) {
            return patientDAO.findAll();
        }
        return patientDAO.search(query);
    }

    public long countPatients() {
        return patientDAO.count();
    }

    public Patient savePatient(Patient patient) {
        validate(patient);
        return patientDAO.save(patient);
    }

    public void updatePatient(Patient patient) {
        validate(patient);
        patientDAO.update(patient);
    }

    public void deletePatient(int patientId) {
        patientDAO.delete(patientId);
    }

    private void validate(Patient p) {
        if (isBlank(p.getFirstName()) || isBlank(p.getLastName())) {
            throw new IllegalArgumentException("First name and last name are required.");
        }
        if (p.getDateOfBirth() == null) {
            throw new IllegalArgumentException("Date of birth is required.");
        }
        if (p.getDateOfBirth().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Date of birth cannot be in the future.");
        }
        if (p.getGender() == null) {
            throw new IllegalArgumentException("Gender is required.");
        }
        if (!isBlank(p.getEmail()) && !p.getEmail().trim().matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            throw new IllegalArgumentException("The e-mail address is not valid.");
        }
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
