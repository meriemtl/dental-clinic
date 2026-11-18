package com.dentalclinic.dao;

import com.dentalclinic.model.TreatmentType;
import com.dentalclinic.utils.Jdbc;
import com.dentalclinic.utils.Jdbc.Mapper;

import java.util.List;
import java.util.Optional;

public class TreatmentTypeDAOImpl implements TreatmentTypeDAO {

    private static final String SELECT =
            "SELECT type_id, name, description, default_price, active FROM treatment_types ";
    private static final String FIND_BY_ID = SELECT + "WHERE type_id = ?";
    private static final String FIND_ALL = SELECT + "ORDER BY name";
    private static final String FIND_ACTIVE = SELECT + "WHERE active = 1 ORDER BY name";
    private static final String COUNT = "SELECT COUNT(*) FROM treatment_types";
    private static final String INSERT =
            "INSERT INTO treatment_types (name, description, default_price, active) VALUES (?, ?, ?, ?)";
    private static final String UPDATE =
            "UPDATE treatment_types SET name = ?, description = ?, default_price = ?, active = ? WHERE type_id = ?";

    private static final Mapper<TreatmentType> MAPPER = rs -> {
        TreatmentType t = new TreatmentType();
        t.setTypeId(rs.getInt("type_id"));
        t.setName(rs.getString("name"));
        t.setDescription(rs.getString("description"));
        t.setDefaultPrice(rs.getBigDecimal("default_price"));
        t.setActive(rs.getInt("active") != 0);
        return t;
    };

    @Override
    public Optional<TreatmentType> findById(int typeId) {
        return Jdbc.queryOne(FIND_BY_ID, Jdbc.params(typeId), MAPPER);
    }

    @Override
    public List<TreatmentType> findAll() {
        return Jdbc.query(FIND_ALL, Jdbc.NONE, MAPPER);
    }

    @Override
    public List<TreatmentType> findActive() {
        return Jdbc.query(FIND_ACTIVE, Jdbc.NONE, MAPPER);
    }

    @Override
    public long count() {
        return Jdbc.count(COUNT, Jdbc.NONE);
    }

    @Override
    public TreatmentType save(TreatmentType t) {
        if (t.getTypeId() == 0) {
            int id = Jdbc.insert(INSERT, Jdbc.params(t.getName(), t.getDescription(),
                    t.getDefaultPrice(), t.isActive() ? 1 : 0));
            t.setTypeId(id);
        } else {
            update(t);
        }
        return t;
    }

    @Override
    public void update(TreatmentType t) {
        Jdbc.update(UPDATE, Jdbc.params(t.getName(), t.getDescription(), t.getDefaultPrice(),
                t.isActive() ? 1 : 0, t.getTypeId()));
    }
}
