package com.dentalclinic.utils;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalTime;
import java.util.Properties;

/**
 * Application settings. Values come from "config.properties" in the folder the
 * application is started from; missing keys fall back to the defaults below.
 */
public final class AppConfig {
    private static final Properties PROPS = new Properties();

    static {
        PROPS.setProperty("db.host", "localhost");
        PROPS.setProperty("db.port", "3306");
        PROPS.setProperty("db.name", "dental_clinic");
        PROPS.setProperty("db.user", "root");
        PROPS.setProperty("db.password", "password");
        PROPS.setProperty("clinic.name", "Dental Clinic");
        PROPS.setProperty("currency", "DA");
        PROPS.setProperty("agenda.start", "08:00");
        PROPS.setProperty("agenda.end", "18:00");
        PROPS.setProperty("agenda.slot", "15");
        PROPS.setProperty("app.sample-data", "true");

        Path file = Path.of("config.properties");
        if (Files.isRegularFile(file)) {
            try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
                PROPS.load(reader);
            } catch (IOException e) {
                System.err.println("Could not read config.properties: " + e.getMessage());
            }
        }
    }

    private AppConfig() {
    }

    public static String get(String key) {
        return PROPS.getProperty(key, "").trim();
    }

    public static String dbHost() { return get("db.host"); }
    public static String dbPort() { return get("db.port"); }
    public static String dbName() { return get("db.name"); }
    public static String dbUser() { return get("db.user"); }
    public static String dbPassword() { return PROPS.getProperty("db.password", ""); }
    public static String clinicName() { return get("clinic.name"); }
    public static String currency() { return get("currency"); }
    public static boolean sampleData() { return Boolean.parseBoolean(get("app.sample-data")); }

    public static LocalTime agendaStart() { return parseTime(get("agenda.start"), LocalTime.of(8, 0)); }
    public static LocalTime agendaEnd() { return parseTime(get("agenda.end"), LocalTime.of(18, 0)); }

    public static int agendaSlotMinutes() {
        try {
            int v = Integer.parseInt(get("agenda.slot"));
            return v >= 5 && v <= 120 ? v : 15;
        } catch (NumberFormatException e) {
            return 15;
        }
    }

    /** JDBC URL, with or without the database name (the latter is used to create the database). */
    public static String jdbcUrl(boolean withDatabase) {
        return "jdbc:mysql://" + dbHost() + ":" + dbPort() + "/" + (withDatabase ? dbName() : "")
                + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&characterEncoding=UTF-8";
    }

    private static LocalTime parseTime(String text, LocalTime fallback) {
        try {
            return LocalTime.parse(text);
        } catch (RuntimeException e) {
            return fallback;
        }
    }
}
