package com.dentalclinic.ui;

import com.dentalclinic.model.Appointment;
import com.dentalclinic.model.DashboardStats;
import com.dentalclinic.model.User;
import com.dentalclinic.service.AppointmentService;
import com.dentalclinic.service.DashboardService;
import com.dentalclinic.utils.Fmt;
import com.dentalclinic.utils.Session;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.util.List;

final class DashboardView extends VBox {

    DashboardView() {
        setSpacing(18);
        setPadding(new Insets(24));

        User user = Session.getUser();
        DashboardStats stats = new DashboardService().loadStats();
        LocalDate today = LocalDate.now();

        Label welcome = Ui.muted("Welcome, " + user.getFullName() + "  -  " + Fmt.date(today));

        HBox cards = new HBox(16,
                card("Patients", String.valueOf(stats.totalPatients)),
                card("Appointments today", String.valueOf(stats.appointmentsToday)),
                card("Scheduled, next 7 days", String.valueOf(stats.appointmentsNextSevenDays)),
                card("Revenue this month", Fmt.money(stats.revenueThisMonth)),
                card("Outstanding balance", Fmt.money(stats.outstandingBalance)));

        TableView<Appointment> table = Ui.table();
        Ui.addColumn(table, "Time", 130, a -> Fmt.time(a.getStartTime()) + " - " + Fmt.time(a.getEndTime()));
        Ui.addColumn(table, "Patient", 220, Appointment::getPatientName);
        Ui.addColumn(table, "Dentist", 200, Appointment::getDentistName);
        Ui.addColumn(table, "Reason", 220, a -> Fmt.nullToEmpty(a.getReason()));
        Ui.addColumn(table, "Status", 110, a -> Ui.pretty(a.getStatus()));
        List<Appointment> todays = new AppointmentService().findAppointmentsByDateRange(today, today);
        table.setItems(Ui.items(todays));
        VBox.setVgrow(table, Priority.ALWAYS);

        getChildren().addAll(Ui.pageTitle("Dashboard"), welcome, cards,
                Ui.sectionTitle("Today's appointments"), table);
    }

    private static VBox card(String label, String value) {
        Label v = new Label(value);
        v.getStyleClass().add("stat-value");
        Label l = new Label(label);
        l.getStyleClass().add("stat-label");
        VBox box = new VBox(4, v, l);
        box.getStyleClass().addAll("card", "stat-card");
        box.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(box, Priority.ALWAYS);
        return box;
    }
}
