package com.dentalclinic.ui;

import com.dentalclinic.model.Appointment;
import com.dentalclinic.model.Appointment.AppointmentStatus;
import com.dentalclinic.model.Patient;
import com.dentalclinic.model.User;
import com.dentalclinic.service.AppointmentService;
import com.dentalclinic.service.PatientService;
import com.dentalclinic.service.UserService;
import com.dentalclinic.utils.AppConfig;
import com.dentalclinic.utils.Fmt;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Create / edit form for an appointment. */
final class AppointmentFormDialog {
    private AppointmentFormDialog() {
    }

    /**
     * @param existing      the appointment to edit, or null to create one
     * @param defaultDate   date proposed for a new appointment
     * @param defaultDentist dentist proposed for a new appointment (may be null or the "all" placeholder)
     * @return true when the appointment was saved
     */
    static boolean show(Appointment existing, LocalDate defaultDate, User defaultDentist) {
        AppointmentService service = new AppointmentService();
        UserService userService = new UserService();
        List<Patient> patients = new PatientService().findAllPatients();
        List<User> dentists = new ArrayList<>(userService.findActiveDentists());
        if (existing != null && dentists.stream().noneMatch(d -> d.getUserId() == existing.getUserId())) {
            userService.findAllUsers().stream()
                    .filter(u -> u.getUserId() == existing.getUserId())
                    .findFirst().ifPresent(dentists::add);
        }
        if (patients.isEmpty()) {
            throw new IllegalStateException("There are no patients yet. Add a patient first.");
        }
        if (dentists.isEmpty()) {
            throw new IllegalStateException(
                    "No active dentist found. An administrator must create one in the Staff screen.");
        }

        ComboBox<Patient> patient = Ui.combo(patients, Patient::getFullName);
        ComboBox<User> dentist = Ui.combo(dentists, User::getFullName);
        DatePicker date = new DatePicker();
        ComboBox<LocalTime> start = Ui.combo(startTimes(existing), Fmt::time);
        ComboBox<Integer> duration = Ui.combo(durations(existing), d -> d + " min");
        ComboBox<AppointmentStatus> status = Ui.combo(List.of(AppointmentStatus.values()), Ui::pretty);
        TextField reason = new TextField();
        TextArea notes = Ui.textArea(3);

        if (existing != null) {
            patients.stream().filter(p -> p.getPatientId() == existing.getPatientId())
                    .findFirst().ifPresent(patient::setValue);
            dentists.stream().filter(d -> d.getUserId() == existing.getUserId())
                    .findFirst().ifPresent(dentist::setValue);
            date.setValue(existing.getAppointmentDate());
            start.setValue(existing.getStartTime());
            duration.setValue(existing.getDurationMinutes());
            status.setValue(existing.getStatus());
            reason.setText(Fmt.nullToEmpty(existing.getReason()));
            notes.setText(Fmt.nullToEmpty(existing.getNotes()));
        } else {
            date.setValue(defaultDate == null ? LocalDate.now() : defaultDate);
            start.setValue(start.getItems().get(0));
            duration.setValue(30);
            status.setValue(AppointmentStatus.SCHEDULED);
            User preset = defaultDentist == null ? dentists.get(0)
                    : dentists.stream().filter(d -> d.getUserId() == defaultDentist.getUserId())
                            .findFirst().orElse(dentists.get(0));
            dentist.setValue(preset);
        }

        GridPane grid = Ui.form();
        int row = 0;
        Ui.addRow(grid, row++, "Patient *", patient);
        Ui.addRow(grid, row++, "Dentist *", dentist);
        Ui.addRow(grid, row++, "Date *", date);
        Ui.addRow(grid, row++, "Start time *", start);
        Ui.addRow(grid, row++, "Duration *", duration);
        if (existing != null) {
            Ui.addRow(grid, row++, "Status", status);
        }
        Ui.addRow(grid, row++, "Reason", reason);
        Ui.addRow(grid, row, "Notes", notes);
        grid.setPrefWidth(440);

        String title = existing == null ? "New appointment" : "Edit appointment";
        return Ui.showForm(title, "Save", grid, () -> {
            Patient p = patient.getValue();
            User d = dentist.getValue();
            if (p == null) {
                throw new IllegalArgumentException("Please select a patient.");
            }
            if (d == null) {
                throw new IllegalArgumentException("Please select a dentist.");
            }
            if (date.getValue() == null || start.getValue() == null || duration.getValue() == null) {
                throw new IllegalArgumentException("Date, start time and duration are required.");
            }
            Appointment a = new Appointment();
            if (existing != null) {
                a.setAppointmentId(existing.getAppointmentId());
            }
            a.setPatientId(p.getPatientId());
            a.setUserId(d.getUserId());
            a.setAppointmentDate(date.getValue());
            a.setStartTime(start.getValue());
            a.setEndTime(start.getValue().plusMinutes(duration.getValue()));
            a.setStatus(status.getValue() == null ? AppointmentStatus.SCHEDULED : status.getValue());
            a.setReason(Fmt.blankToNull(reason.getText()));
            a.setNotes(Fmt.blankToNull(notes.getText()));
            service.saveAppointment(a);
        });
    }

    /** Start times every slot between the opening and closing time of the agenda. */
    private static List<LocalTime> startTimes(Appointment existing) {
        List<LocalTime> slots = new ArrayList<>();
        int step = AppConfig.agendaSlotMinutes();
        LocalTime end = AppConfig.agendaEnd();
        LocalTime t = AppConfig.agendaStart();
        while (t.isBefore(end)) {
            slots.add(t);
            LocalTime next = t.plusMinutes(step);
            if (!next.isAfter(t)) {
                break; // passed midnight
            }
            t = next;
        }
        if (existing != null && !slots.contains(existing.getStartTime())) {
            slots.add(existing.getStartTime());
        }
        if (slots.isEmpty()) {
            slots.add(LocalTime.of(8, 0));
        }
        Collections.sort(slots);
        return slots;
    }

    private static List<Integer> durations(Appointment existing) {
        List<Integer> values = new ArrayList<>(List.of(15, 30, 45, 60, 90, 120));
        if (existing != null && !values.contains(existing.getDurationMinutes())) {
            values.add(existing.getDurationMinutes());
        }
        Collections.sort(values);
        return values;
    }
}
