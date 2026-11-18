package com.dentalclinic.dao;

import com.dentalclinic.model.Patient;
import java.util.List;
import java.util.Optional;

public interface PatientDAO {
    Optional<Patient> findById(int patientId);
    List<Patient> findAll();
    List<Patient> search(String query);
    long count();
    Patient save(Patient patient);
    void update(Patient patient);
    void delete(int patientId);
}
