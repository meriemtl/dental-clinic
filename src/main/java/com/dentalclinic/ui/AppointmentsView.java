package com.dentalclinic.ui;

import com.dentalclinic.model.Appointment;
import com.dentalclinic.model.Appointment.AppointmentStatus;
import com.dentalclinic.model.User;
import com.dentalclinic.service.AppointmentService;
import com.dentalclinic.service.UserService;
import com.dentalclinic.utils.Fmt;
import com.dentalclinic.utils.Session;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

final class AppointmentsView extends BorderPane {
    private final AppointmentService service = new AppointmentService();
    private final TableView<Appointment> table = Ui.table();
    private final DatePicker date = new DatePicker(LocalDate.now());
    private final CheckBox allDates = new CheckBox("All dates");
    private final ComboBox<User> dentistFilter;
    private final Label count = Ui.muted("");

    AppointmentsView() {
        setPadding(new Insets(24));

        User everyone = new User();
        everyone.setFullName("All dentists");
        List<User> choices = new ArrayList<>();
        choices.add(everyone);
        choices.addAll(new UserService().findActiveDentists());
        dentistFilter = Ui.combo(choices, User::getFullName);
        User me = Session.getUser();
        dentistFilter.setValue(choices.stream()
                .filter(u -> me != null && me.isDentist() && u.getUserId() == me.getUserId())
                .findFirst().orElse(everyone));

        Button add = Ui.button("+ New appointment", "primary", () -> {
            User preset = dentistFilter.getValue();
            LocalDate day = date.getValue() == null ? LocalDate.now() : date.getValue();
            if (AppointmentFormDialog.show(null, day, preset)) {
                refresh();
            }
        });
        HBox header = new HBox(12, Ui.pageTitle("Appointments"), Ui.spacer(), add);
        header.setAlignment(Pos.CENTER_LEFT);

        Button previous = Ui.button("<", "secondary", () -> shiftDay(-1));
        Button next = Ui.button(">", "secondary", () -> shiftDay(1));
        Button today = Ui.button("Today", "secondary", () -> {
            date.setValue(LocalDate.now());
            refresh();
        });
        HBox filters = new HBox(10, previous, date, next, today, allDates, Ui.spacer(),
                new Label("Dentist:"), dentistFilter);
        filters.setAlignment(Pos.CENTER_LEFT);
        VBox top = new VBox(12, header, filters);
        top.setPadding(new Insets(0, 0, 14, 0));
        setTop(top);

        date.setOnAction(e -> refresh());
        allDates.setOnAction(e -> {
            date.setDisable(allDates.isSelected());
            refresh();
        });
        dentistFilter.setOnAction(e -> refresh());

        Ui.addColumn(table, "Date", 95, a -> Fmt.date(a.getAppointmentDate()));
        Ui.addColumn(table, "Start", 65, a -> Fmt.time(a.getStartTime()));
        Ui.addColumn(table, "End", 65, a -> Fmt.time(a.getEndTime()));
        Ui.addColumn(table, "Patient", 200, Appointment::getPatientName);
        Ui.addColumn(table, "Dentist", 170, Appointment::getDentistName);
        Ui.addColumn(table, "Reason", 190, a -> Fmt.nullToEmpty(a.getReason()));
        Ui.addColumn(table, "Status", 95, a -> Ui.pretty(a.getStatus()));
        Ui.addColumn(table, "Notes", 200, a -> Fmt.nullToEmpty(a.getNotes()));
        setCenter(table);

        HBox actions = new HBox(10,
                Ui.button("Edit / reschedule", "secondary", this::editSelected),
                Ui.button("Completed", "success", () -> setStatus(AppointmentStatus.COMPLETED)),
                Ui.button("No-show", "secondary", () -> setStatus(AppointmentStatus.NO_SHOW)),
                Ui.button("Cancel appointment", "secondary", () -> setStatus(AppointmentStatus.CANCELLED)),
                Ui.button("Delete", "danger", this::deleteSelected),
                Ui.spacer(), count);
        actions.setAlignment(Pos.CENTER_LEFT);
        actions.setPadding(new Insets(14, 0, 0, 0));
        setBottom(actions);

        refresh();
    }

    private void shiftDay(int days) {
        LocalDate base = date.getValue() == null ? LocalDate.now() : date.getValue();
        date.setValue(base.plusDays(days));
        refresh();
    }

    private void refresh() {
        Ui.run(() -> {
            if (date.getValue() == null) {
                date.setValue(LocalDate.now());
            }
            List<Appointment> rows = allDates.isSelected()
                    ? service.findAllAppointments()
                    : service.findAppointmentsByDateRange(date.getValue(), date.getValue());
            User filter = dentistFilter.getValue();
            if (filter != null && filter.getUserId() != 0) {
                rows = rows.stream().filter(a -> a.getUserId() == filter.getUserId()).toList();
            }
            table.setItems(Ui.items(rows));
            count.setText(rows.size() + " appointment(s)");
        });
    }

    private Appointment selected() {
        Appointment a = table.getSelectionModel().getSelectedItem();
        if (a == null) {
            throw new IllegalStateException("Please select an appointment in the list first.");
        }
        return a;
    }

    private void editSelected() {
        if (AppointmentFormDialog.show(selected(), null, null)) {
            refresh();
        }
    }

    private void setStatus(AppointmentStatus status) {
        Appointment a = selected();
        service.updateStatus(a.getAppointmentId(), status);
        refresh();
    }

    private void deleteSelected() {
        Appointment a = selected();
        if (Ui.confirm("Delete appointment", "Delete the appointment of " + a.getPatientName() + " on "
                + Fmt.date(a.getAppointmentDate()) + " at " + Fmt.time(a.getStartTime()) + "?")) {
            service.deleteAppointment(a.getAppointmentId());
            refresh();
        }
    }
}
