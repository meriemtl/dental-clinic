package com.dentalclinic.dao;

import com.dentalclinic.model.TreatmentType;
import java.util.List;
import java.util.Optional;

public interface TreatmentTypeDAO {
    Optional<TreatmentType> findById(int typeId);
    List<TreatmentType> findAll();
    List<TreatmentType> findActive();
    long count();
    TreatmentType save(TreatmentType type);
    void update(TreatmentType type);
}
