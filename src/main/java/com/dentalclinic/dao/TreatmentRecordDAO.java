package com.dentalclinic.dao;

import com.dentalclinic.model.TreatmentRecord;
import java.util.List;

public interface TreatmentRecordDAO {
    List<TreatmentRecord> findByPatient(int patientId);
    List<TreatmentRecord> findUnbilledByPatient(int patientId);
    TreatmentRecord save(TreatmentRecord record);
    void delete(int recordId);
}
