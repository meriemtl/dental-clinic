package com.dentalclinic.dao;

import com.dentalclinic.model.DashboardStats;
import com.dentalclinic.utils.Jdbc;

import java.math.BigDecimal;
import java.time.LocalDate;

public class StatsDAOImpl implements StatsDAO {

    private static final String COUNT_PATIENTS = "SELECT COUNT(*) FROM patients";
    private static final String COUNT_SCHEDULED_BETWEEN = """
            SELECT COUNT(*) FROM appointments
            WHERE appointment_date BETWEEN ? AND ? AND status = 'SCHEDULED'
            """;
    private static final String SUM_PAYMENTS_BETWEEN =
            "SELECT COALESCE(SUM(amount), 0) FROM payments WHERE paid_on BETWEEN ? AND ?";
    private static final String SUM_INVOICED = "SELECT COALESCE(SUM(total_amount), 0) FROM invoices";
    private static final String SUM_PAYMENTS = "SELECT COALESCE(SUM(amount), 0) FROM payments";

    @Override
    public DashboardStats load(LocalDate today) {
        DashboardStats s = new DashboardStats();
        s.totalPatients = Jdbc.count(COUNT_PATIENTS, Jdbc.NONE);
        s.appointmentsToday = Jdbc.count(COUNT_SCHEDULED_BETWEEN, Jdbc.params(today, today));
        s.appointmentsNextSevenDays =
                Jdbc.count(COUNT_SCHEDULED_BETWEEN, Jdbc.params(today, today.plusDays(7)));
        s.revenueThisMonth = Jdbc.sum(SUM_PAYMENTS_BETWEEN,
                Jdbc.params(today.withDayOfMonth(1), today.withDayOfMonth(today.lengthOfMonth())));
        BigDecimal invoiced = Jdbc.sum(SUM_INVOICED, Jdbc.NONE);
        BigDecimal paid = Jdbc.sum(SUM_PAYMENTS, Jdbc.NONE);
        s.outstandingBalance = invoiced.subtract(paid);
        return s;
    }
}
