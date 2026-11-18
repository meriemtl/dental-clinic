package com.dentalclinic.service;

import com.dentalclinic.dao.StatsDAO;
import com.dentalclinic.dao.StatsDAOImpl;
import com.dentalclinic.model.DashboardStats;

import java.time.LocalDate;

public class DashboardService {
    private final StatsDAO statsDAO;

    public DashboardService() {
        this(new StatsDAOImpl());
    }

    public DashboardService(StatsDAO statsDAO) {
        this.statsDAO = statsDAO;
    }

    public DashboardStats loadStats() {
        return statsDAO.load(LocalDate.now());
    }
}
