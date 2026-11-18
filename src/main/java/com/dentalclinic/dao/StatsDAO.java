package com.dentalclinic.dao;

import com.dentalclinic.model.DashboardStats;
import java.time.LocalDate;

public interface StatsDAO {
    DashboardStats load(LocalDate today);
}
