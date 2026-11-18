package com.dentalclinic.dao;

import com.dentalclinic.model.Appointment;
import com.dentalclinic.model.Appointment.AppointmentStatus;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface AppointmentDAO {
    Optional<Appointment> findById(int appointmentId);
    List<Appointment> findAll();
    List<Appointment> findByDateRange(LocalDate startDate, LocalDate endDate);
    List<Appointment> findByPatientId(int patientId);
    List<Appointment> findByDentistId(int dentistId);
    Appointment save(Appointment appointment);
    void update(Appointment appointment);
    void updateStatus(int appointmentId, AppointmentStatus status);
    void delete(int appointmentId);
    boolean hasAppointmentConflict(int dentistId, LocalDate date, LocalTime startTime,
                                   LocalTime endTime, int excludeAppointmentId);
}
