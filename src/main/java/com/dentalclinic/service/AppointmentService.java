package com.dentalclinic.service;

import com.dentalclinic.dao.AppointmentDAO;
import com.dentalclinic.dao.AppointmentDAOImpl;
import com.dentalclinic.model.Appointment;
import com.dentalclinic.model.Appointment.AppointmentStatus;

import java.time.LocalDate;
import java.util.List;

public class AppointmentService {
    private final AppointmentDAO appointmentDAO;

    public AppointmentService() {
        this(new AppointmentDAOImpl());
    }

    public AppointmentService(AppointmentDAO appointmentDAO) {
        this.appointmentDAO = appointmentDAO;
    }

    public Appointment findAppointmentById(int appointmentId) {
        return appointmentDAO.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found with ID: " + appointmentId));
    }

    public List<Appointment> findAllAppointments() {
        return appointmentDAO.findAll();
    }

    public List<Appointment> findAppointmentsByDateRange(LocalDate startDate, LocalDate endDate) {
        return appointmentDAO.findByDateRange(startDate, endDate);
    }

    public List<Appointment> findAppointmentsByPatientId(int patientId) {
        return appointmentDAO.findByPatientId(patientId);
    }

    public List<Appointment> findAppointmentsByDentistId(int dentistId) {
        return appointmentDAO.findByDentistId(dentistId);
    }

    public Appointment saveAppointment(Appointment appointment) {
        validate(appointment);
        return appointmentDAO.save(appointment);
    }

    public void updateAppointment(Appointment appointment) {
        validate(appointment);
        appointmentDAO.update(appointment);
    }

    public void updateStatus(int appointmentId, AppointmentStatus status) {
        appointmentDAO.updateStatus(appointmentId, status);
    }

    public void deleteAppointment(int appointmentId) {
        appointmentDAO.delete(appointmentId);
    }

    private void validate(Appointment a) {
        if (a.getPatientId() == 0) {
            throw new IllegalArgumentException("Please select a patient.");
        }
        if (a.getUserId() == 0) {
            throw new IllegalArgumentException("Please select a dentist.");
        }
        if (a.getAppointmentDate() == null || a.getStartTime() == null || a.getEndTime() == null) {
            throw new IllegalArgumentException("Date, start time and end time are required.");
        }
        if (!a.getEndTime().isAfter(a.getStartTime())) {
            throw new IllegalArgumentException("The end time must be after the start time.");
        }
        boolean takesSlot = a.getStatus() == AppointmentStatus.SCHEDULED
                || a.getStatus() == AppointmentStatus.COMPLETED;
        if (takesSlot && appointmentDAO.hasAppointmentConflict(a.getUserId(), a.getAppointmentDate(),
                a.getStartTime(), a.getEndTime(), a.getAppointmentId())) {
            throw new IllegalStateException(
                    "This dentist already has an appointment that overlaps this time slot.");
        }
    }
}
