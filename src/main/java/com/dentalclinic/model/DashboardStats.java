package com.dentalclinic.model;

import java.math.BigDecimal;

/** Figures displayed on the dashboard. */
public class DashboardStats {
    public long totalPatients;
    public long appointmentsToday;
    public long appointmentsNextSevenDays;
    public BigDecimal revenueThisMonth = BigDecimal.ZERO;
    public BigDecimal outstandingBalance = BigDecimal.ZERO;
}
