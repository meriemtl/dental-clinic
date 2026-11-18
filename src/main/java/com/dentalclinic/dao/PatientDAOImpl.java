package com.dentalclinic.dao;

import com.dentalclinic.model.Patient;
import com.dentalclinic.utils.Jdbc;
import com.dentalclinic.utils.Jdbc.Binder;
import com.dentalclinic.utils.Jdbc.Mapper;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class PatientDAOImpl implements PatientDAO {

    private static final String SELECT = """
            SELECT patient_id, first_name, last_name, date_of_birth, gender, phone_number, email,
                   address, blood_type, allergies, medical_history, registration_date, updated_at
            FROM patients
            """;
    private static final String FIND_BY_ID = SELECT + "WHERE patient_id = ?";
    private static final String FIND_ALL = SELECT + "ORDER BY last_name, first_name";
    // Up to two search words (e.g. "haddad amine"): every word must match one of the columns.
    private static final String SEARCH = SELECT + """
            WHERE (first_name LIKE ? OR last_name LIKE ? OR phone_number LIKE ? OR email LIKE ?)
              AND (first_name LIKE ? OR last_name LIKE ? OR phone_number LIKE ? OR email LIKE ?)
            ORDER BY last_name, first_name
            """;
    private static final String COUNT = "SELECT COUNT(*) FROM patients";
    private static final String INSERT = """
            INSERT INTO patients (first_name, last_name, date_of_birth, gender, phone_number, email,
                                  address, blood_type, allergies, medical_history)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
    private static final String UPDATE = """
            UPDATE patients SET first_name = ?, last_name = ?, date_of_birth = ?, gender = ?,
                   phone_number = ?, email = ?, address = ?, blood_type = ?, allergies = ?,
                   medical_history = ?
            WHERE patient_id = ?
            """;
    private static final String DELETE = "DELETE FROM patients WHERE patient_id = ?";

    private static final Mapper<Patient> MAPPER = rs -> {
        Patient p = new Patient();
        p.setPatientId(rs.getInt("patient_id"));
        p.setFirstName(rs.getString("first_name"));
        p.setLastName(rs.getString("last_name"));
        p.setDateOfBirth(rs.getObject("date_of_birth", LocalDate.class));
        p.setGender(Patient.Gender.valueOf(rs.getString("gender")));
        p.setPhoneNumber(rs.getString("phone_number"));
        p.setEmail(rs.getString("email"));
        p.setAddress(rs.getString("address"));
        p.setBloodType(rs.getString("blood_type"));
        p.setAllergies(rs.getString("allergies"));
        p.setMedicalHistory(rs.getString("medical_history"));
        p.setRegistrationDate(Jdbc.timestamp(rs, "registration_date"));
        p.setUpdatedAt(Jdbc.timestamp(rs, "updated_at"));
        return p;
    };

    @Override
    public Optional<Patient> findById(int patientId) {
        return Jdbc.queryOne(FIND_BY_ID, Jdbc.params(patientId), MAPPER);
    }

    @Override
    public List<Patient> findAll() {
        return Jdbc.query(FIND_ALL, Jdbc.NONE, MAPPER);
    }

    @Override
    public List<Patient> search(String query) {
        String[] words = query.trim().split("\\s+");
        String first = "%" + words[0] + "%";
        String second = words.length > 1 ? "%" + words[1] + "%" : "%";
        Binder binder = Jdbc.params(first, first, first, first, second, second, second, second);
        return Jdbc.query(SEARCH, binder, MAPPER);
    }

    @Override
    public long count() {
        return Jdbc.count(COUNT, Jdbc.NONE);
    }

    @Override
    public Patient save(Patient p) {
        if (p.getPatientId() == 0) {
            int id = Jdbc.insert(INSERT, Jdbc.params(p.getFirstName(), p.getLastName(), p.getDateOfBirth(),
                    p.getGender(), p.getPhoneNumber(), p.getEmail(), p.getAddress(), p.getBloodType(),
                    p.getAllergies(), p.getMedicalHistory()));
            p.setPatientId(id);
        } else {
            update(p);
        }
        return p;
    }

    @Override
    public void update(Patient p) {
        if (p.getPatientId() == 0) {
            throw new IllegalArgumentException("Patient ID cannot be 0 for update operation.");
        }
        Jdbc.update(UPDATE, Jdbc.params(p.getFirstName(), p.getLastName(), p.getDateOfBirth(), p.getGender(),
                p.getPhoneNumber(), p.getEmail(), p.getAddress(), p.getBloodType(), p.getAllergies(),
                p.getMedicalHistory(), p.getPatientId()));
    }

    @Override
    public void delete(int patientId) {
        Jdbc.update(DELETE, Jdbc.params(patientId));
    }
}
