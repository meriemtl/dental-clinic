package com.dentalclinic.dao;

import com.dentalclinic.model.Appointment;
import com.dentalclinic.model.Appointment.AppointmentStatus;
import com.dentalclinic.utils.Jdbc;
import com.dentalclinic.utils.Jdbc.Mapper;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public class AppointmentDAOImpl implements AppointmentDAO {

    private static final String SELECT = """
            SELECT a.appointment_id, a.patient_id, a.user_id, a.appointment_date, a.start_time,
                   a.end_time, a.status, a.reason, a.notes, a.created_at, a.updated_at,
                   p.first_name, p.last_name, u.full_name AS dentist_name
            FROM appointments a
            JOIN patients p ON a.patient_id = p.patient_id
            JOIN users u ON a.user_id = u.user_id
            """;
    private static final String FIND_BY_ID = SELECT + "WHERE a.appointment_id = ?";
    private static final String FIND_ALL =
            SELECT + "ORDER BY a.appointment_date DESC, a.start_time DESC";
    private static final String FIND_BY_DATE_RANGE = SELECT
            + "WHERE a.appointment_date BETWEEN ? AND ? ORDER BY a.appointment_date, a.start_time";
    private static final String FIND_BY_PATIENT_ID = SELECT
            + "WHERE a.patient_id = ? ORDER BY a.appointment_date DESC, a.start_time DESC";
    private static final String FIND_BY_DENTIST_ID = SELECT
            + "WHERE a.user_id = ? ORDER BY a.appointment_date DESC, a.start_time DESC";

    private static final String INSERT = """
            INSERT INTO appointments (patient_id, user_id, appointment_date, start_time, end_time,
                                      status, reason, notes)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;
    private static final String UPDATE = """
            UPDATE appointments SET patient_id = ?, user_id = ?, appointment_date = ?, start_time = ?,
                   end_time = ?, status = ?, reason = ?, notes = ?
            WHERE appointment_id = ?
            """;
    private static final String UPDATE_STATUS = "UPDATE appointments SET status = ? WHERE appointment_id = ?";
    private static final String DELETE = "DELETE FROM appointments WHERE appointment_id = ?";

    // Two appointments of the same dentist overlap when each one starts before the other ends.
    // Cancelled and no-show appointments free their slot.
    private static final String CHECK_CONFLICT = """
            SELECT COUNT(*) FROM appointments
            WHERE user_id = ? AND appointment_date = ?
              AND status IN ('SCHEDULED', 'COMPLETED')
              AND start_time < ? AND end_time > ?
              AND appointment_id <> ?
            """;

    private static final Mapper<Appointment> MAPPER = rs -> {
        Appointment a = new Appointment();
        a.setAppointmentId(rs.getInt("appointment_id"));
        a.setPatientId(rs.getInt("patient_id"));
        a.setUserId(rs.getInt("user_id"));
        a.setAppointmentDate(rs.getObject("appointment_date", LocalDate.class));
        a.setStartTime(rs.getObject("start_time", LocalTime.class));
        a.setEndTime(rs.getObject("end_time", LocalTime.class));
        a.setStatus(AppointmentStatus.valueOf(rs.getString("status")));
        a.setReason(rs.getString("reason"));
        a.setNotes(rs.getString("notes"));
        a.setCreatedAt(Jdbc.timestamp(rs, "created_at"));
        a.setUpdatedAt(Jdbc.timestamp(rs, "updated_at"));
        a.setPatientName(rs.getString("last_name") + " " + rs.getString("first_name"));
        a.setDentistName(rs.getString("dentist_name"));
        return a;
    };

    @Override
    public Optional<Appointment> findById(int appointmentId) {
        return Jdbc.queryOne(FIND_BY_ID, Jdbc.params(appointmentId), MAPPER);
    }

    @Override
    public List<Appointment> findAll() {
        return Jdbc.query(FIND_ALL, Jdbc.NONE, MAPPER);
    }

    @Override
    public List<Appointment> findByDateRange(LocalDate startDate, LocalDate endDate) {
        return Jdbc.query(FIND_BY_DATE_RANGE, Jdbc.params(startDate, endDate), MAPPER);
    }

    @Override
    public List<Appointment> findByPatientId(int patientId) {
        return Jdbc.query(FIND_BY_PATIENT_ID, Jdbc.params(patientId), MAPPER);
    }

    @Override
    public List<Appointment> findByDentistId(int dentistId) {
        return Jdbc.query(FIND_BY_DENTIST_ID, Jdbc.params(dentistId), MAPPER);
    }

    @Override
    public Appointment save(Appointment a) {
        if (a.getAppointmentId() == 0) {
            int id = Jdbc.insert(INSERT, Jdbc.params(a.getPatientId(), a.getUserId(), a.getAppointmentDate(),
                    a.getStartTime(), a.getEndTime(), a.getStatus(), a.getReason(), a.getNotes()));
            a.setAppointmentId(id);
        } else {
            update(a);
        }
        return a;
    }

    @Override
    public void update(Appointment a) {
        if (a.getAppointmentId() == 0) {
            throw new IllegalArgumentException("Appointment ID cannot be 0 for update operation.");
        }
        Jdbc.update(UPDATE, Jdbc.params(a.getPatientId(), a.getUserId(), a.getAppointmentDate(),
                a.getStartTime(), a.getEndTime(), a.getStatus(), a.getReason(), a.getNotes(),
                a.getAppointmentId()));
    }

    @Override
    public void updateStatus(int appointmentId, AppointmentStatus status) {
        Jdbc.update(UPDATE_STATUS, Jdbc.params(status, appointmentId));
    }

    @Override
    public void delete(int appointmentId) {
        Jdbc.update(DELETE, Jdbc.params(appointmentId));
    }

    @Override
    public boolean hasAppointmentConflict(int dentistId, LocalDate date, LocalTime startTime,
                                          LocalTime endTime, int excludeAppointmentId) {
        return Jdbc.count(CHECK_CONFLICT,
                Jdbc.params(dentistId, date, endTime, startTime, excludeAppointmentId)) > 0;
    }
}
