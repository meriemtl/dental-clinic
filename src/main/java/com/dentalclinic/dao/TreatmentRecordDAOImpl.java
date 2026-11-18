package com.dentalclinic.dao;

import com.dentalclinic.model.TreatmentRecord;
import com.dentalclinic.utils.Jdbc;
import com.dentalclinic.utils.Jdbc.Mapper;

import java.time.LocalDate;
import java.util.List;

public class TreatmentRecordDAOImpl implements TreatmentRecordDAO {

    private static final String SELECT = """
            SELECT r.record_id, r.patient_id, r.user_id, r.type_id, r.treatment_name, r.tooth_number,
                   r.notes, r.price, r.performed_on, r.invoice_id, u.full_name AS dentist_name
            FROM treatment_records r
            JOIN users u ON r.user_id = u.user_id
            """;
    private static final String FIND_BY_PATIENT = SELECT
            + "WHERE r.patient_id = ? ORDER BY r.performed_on DESC, r.record_id DESC";
    private static final String FIND_UNBILLED_BY_PATIENT = SELECT
            + "WHERE r.patient_id = ? AND r.invoice_id IS NULL ORDER BY r.performed_on, r.record_id";
    private static final String INSERT = """
            INSERT INTO treatment_records (patient_id, user_id, type_id, treatment_name, tooth_number,
                                           notes, price, performed_on)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;
    private static final String UPDATE = """
            UPDATE treatment_records SET user_id = ?, type_id = ?, treatment_name = ?, tooth_number = ?,
                   notes = ?, price = ?, performed_on = ?
            WHERE record_id = ?
            """;
    private static final String DELETE = "DELETE FROM treatment_records WHERE record_id = ?";

    private static final Mapper<TreatmentRecord> MAPPER = rs -> {
        TreatmentRecord r = new TreatmentRecord();
        r.setRecordId(rs.getInt("record_id"));
        r.setPatientId(rs.getInt("patient_id"));
        r.setUserId(rs.getInt("user_id"));
        r.setTypeId(Jdbc.nullableInt(rs, "type_id"));
        r.setTreatmentName(rs.getString("treatment_name"));
        r.setToothNumber(rs.getString("tooth_number"));
        r.setNotes(rs.getString("notes"));
        r.setPrice(rs.getBigDecimal("price"));
        r.setPerformedOn(rs.getObject("performed_on", LocalDate.class));
        r.setInvoiceId(Jdbc.nullableInt(rs, "invoice_id"));
        r.setDentistName(rs.getString("dentist_name"));
        return r;
    };

    @Override
    public List<TreatmentRecord> findByPatient(int patientId) {
        return Jdbc.query(FIND_BY_PATIENT, Jdbc.params(patientId), MAPPER);
    }

    @Override
    public List<TreatmentRecord> findUnbilledByPatient(int patientId) {
        return Jdbc.query(FIND_UNBILLED_BY_PATIENT, Jdbc.params(patientId), MAPPER);
    }

    @Override
    public TreatmentRecord save(TreatmentRecord r) {
        if (r.getRecordId() == 0) {
            int id = Jdbc.insert(INSERT, Jdbc.params(r.getPatientId(), r.getUserId(), r.getTypeId(),
                    r.getTreatmentName(), r.getToothNumber(), r.getNotes(), r.getPrice(), r.getPerformedOn()));
            r.setRecordId(id);
        } else {
            Jdbc.update(UPDATE, Jdbc.params(r.getUserId(), r.getTypeId(), r.getTreatmentName(),
                    r.getToothNumber(), r.getNotes(), r.getPrice(), r.getPerformedOn(), r.getRecordId()));
        }
        return r;
    }

    @Override
    public void delete(int recordId) {
        Jdbc.update(DELETE, Jdbc.params(recordId));
    }
}
