package com.dentalclinic.utils;

import com.dentalclinic.dao.PatientDAOImpl;
import com.dentalclinic.dao.TreatmentTypeDAO;
import com.dentalclinic.dao.TreatmentTypeDAOImpl;
import com.dentalclinic.dao.UserDAO;
import com.dentalclinic.dao.UserDAOImpl;
import com.dentalclinic.model.Appointment;
import com.dentalclinic.model.Patient;
import com.dentalclinic.model.Patient.Gender;
import com.dentalclinic.model.TreatmentRecord;
import com.dentalclinic.model.TreatmentType;
import com.dentalclinic.model.User;
import com.dentalclinic.model.User.UserRole;
import com.dentalclinic.service.AppointmentService;
import com.dentalclinic.service.AuthenticationService;
import com.dentalclinic.service.PatientService;
import com.dentalclinic.service.TreatmentService;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Prepares the database on start-up: creates the database and the tables when they are
 * missing, upgrades tables created by older versions, and inserts the default accounts
 * and the price list on the very first run.
 */
final class DatabaseInitializer {
    /** Hash shipped in the original SQL script; replaced because it was never verified. */
    private static final String LEGACY_ADMIN_HASH = "$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBpwTTiC7FQ3ri";

    private DatabaseInitializer() {
    }

    static void run() {
        createDatabase();
        try (Connection c = DatabaseConnection.getConnection()) {
            createTables(c);
            upgradeOldTables(c);
        } catch (SQLException e) {
            throw new DataAccessException("Could not prepare the database tables: " + e.getMessage(), e);
        }
        seedDefaults();
    }

    private static void createDatabase() {
        try (Connection c = DatabaseConnection.getServerConnection(); Statement st = c.createStatement()) {
            st.executeUpdate("CREATE DATABASE IF NOT EXISTS `" + AppConfig.dbName()
                    + "` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
        } catch (SQLException e) {
            throw new DataAccessException("Cannot connect to MySQL at " + AppConfig.dbHost() + ":"
                    + AppConfig.dbPort() + " as user \"" + AppConfig.dbUser() + "\".\n\n"
                    + "Check that the MySQL server is running and that db.user / db.password in "
                    + "config.properties are correct.\n\nDetails: " + e.getMessage(), e);
        }
    }

    private static void createTables(Connection c) throws SQLException {
        for (String sql : readSchemaStatements()) {
            try (Statement st = c.createStatement()) {
                st.execute(sql);
            }
        }
    }

    private static List<String> readSchemaStatements() {
        try (InputStream in = DatabaseInitializer.class.getResourceAsStream("/schema.sql")) {
            if (in == null) {
                throw new DataAccessException("schema.sql was not found inside the application.");
            }
            String script = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            StringBuilder cleaned = new StringBuilder();
            for (String line : script.split("\\R")) {
                if (!line.trim().startsWith("--")) {
                    cleaned.append(line).append('\n');
                }
            }
            return java.util.Arrays.stream(cleaned.toString().split(";"))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toList();
        } catch (IOException e) {
            throw new DataAccessException("Could not read schema.sql: " + e.getMessage(), e);
        }
    }

    /** Adds the columns introduced after the first version of the project (old databases only). */
    private static void upgradeOldTables(Connection c) throws SQLException {
        addColumnIfMissing(c, "users", "active", "TINYINT(1) NOT NULL DEFAULT 1");
        addColumnIfMissing(c, "patients", "medical_history", "TEXT");
        addColumnIfMissing(c, "appointments", "reason", "VARCHAR(150)");
    }

    private static void addColumnIfMissing(Connection c, String table, String column, String definition)
            throws SQLException {
        try (ResultSet rs = c.getMetaData().getColumns(c.getCatalog(), null, table, column)) {
            if (rs.next()) {
                return;
            }
        }
        try (Statement st = c.createStatement()) {
            st.executeUpdate("ALTER TABLE " + table + " ADD COLUMN " + column + " " + definition);
        }
    }

    private static void seedDefaults() {
        UserDAO users = new UserDAOImpl();
        boolean freshDatabase = users.count() == 0;

        if (freshDatabase) {
            insertUser(users, "admin", "Administrator", UserRole.ADMIN, "admin123");
            insertUser(users, "dentist", "Dr. Demo Dentist", UserRole.DENTIST, "dentist123");
            insertUser(users, "reception", "Reception Desk", UserRole.STAFF, "reception123");
        } else {
            users.findByUsername("admin").ifPresent(admin -> {
                if (LEGACY_ADMIN_HASH.equals(admin.getPassword())) {
                    admin.setPassword(AuthenticationService.hash("admin123"));
                    users.update(admin);
                }
            });
        }

        TreatmentTypeDAO types = new TreatmentTypeDAOImpl();
        if (types.count() == 0) {
            insertType(types, "Consultation", "Examination and diagnosis", "1500");
            insertType(types, "X-ray (periapical)", "Single dental X-ray", "800");
            insertType(types, "Panoramic X-ray", "Full panoramic image", "2500");
            insertType(types, "Scaling and polishing", "Tartar removal and polishing", "3000");
            insertType(types, "Filling (composite)", "Tooth-coloured filling", "4000");
            insertType(types, "Root canal treatment", "Endodontic treatment of one tooth", "9000");
            insertType(types, "Tooth extraction", "Simple extraction", "3000");
            insertType(types, "Surgical extraction", "Wisdom tooth or complex extraction", "10000");
            insertType(types, "Ceramic crown", "Crown on one tooth", "25000");
            insertType(types, "Teeth whitening", "In-office whitening session", "20000");
        }

        if (freshDatabase && AppConfig.sampleData()) {
            seedSampleData(users);
        }
    }

    private static void insertUser(UserDAO users, String username, String fullName, UserRole role, String password) {
        User u = new User();
        u.setUsername(username);
        u.setFullName(fullName);
        u.setRole(role);
        u.setActive(true);
        u.setPassword(AuthenticationService.hash(password));
        users.save(u);
    }

    private static void insertType(TreatmentTypeDAO types, String name, String description, String price) {
        TreatmentType t = new TreatmentType();
        t.setName(name);
        t.setDescription(description);
        t.setDefaultPrice(new BigDecimal(price));
        t.setActive(true);
        types.save(t);
    }

    private static void seedSampleData(UserDAO users) {
        if (new PatientDAOImpl().count() > 0) {
            return;
        }
        User dentist = users.findByUsername("dentist").orElse(null);
        if (dentist == null) {
            return;
        }
        PatientService patients = new PatientService();
        Patient amine = samplePatient("Amine", "Haddad", LocalDate.of(1990, 3, 14), Gender.MALE,
                "0550 00 00 01", "amine.haddad@example.com", "O+", "Penicillin");
        Patient lina = samplePatient("Lina", "Benali", LocalDate.of(1985, 7, 22), Gender.FEMALE,
                "0550 00 00 02", "lina.benali@example.com", "A+", null);
        Patient yacine = samplePatient("Yacine", "Mansouri", LocalDate.of(2001, 11, 5), Gender.MALE,
                "0550 00 00 03", "yacine.mansouri@example.com", "B+", null);
        Patient sara = samplePatient("Sara", "Khelifi", LocalDate.of(1978, 1, 30), Gender.FEMALE,
                "0550 00 00 04", "sara.khelifi@example.com", "AB-", "Latex");
        for (Patient p : List.of(amine, lina, yacine, sara)) {
            patients.savePatient(p);
        }

        AppointmentService appointments = new AppointmentService();
        LocalDate today = LocalDate.now();
        appointments.saveAppointment(sampleAppointment(amine, dentist, today, 9, 0, 30, "Check-up"));
        appointments.saveAppointment(sampleAppointment(lina, dentist, today, 10, 0, 45, "Filling"));
        appointments.saveAppointment(sampleAppointment(yacine, dentist, today.plusDays(1), 11, 0, 30, "Scaling"));
        appointments.saveAppointment(sampleAppointment(sara, dentist, today.plusDays(2), 14, 30, 60, "Root canal"));

        TreatmentService treatments = new TreatmentService();
        List<TreatmentType> active = treatments.findActiveTypes();
        if (!active.isEmpty()) {
            TreatmentType consultation = active.stream()
                    .filter(t -> t.getName().equals("Consultation")).findFirst().orElse(active.get(0));
            TreatmentRecord r = new TreatmentRecord();
            r.setPatientId(amine.getPatientId());
            r.setUserId(dentist.getUserId());
            r.setTypeId(consultation.getTypeId());
            r.setTreatmentName(consultation.getName());
            r.setPrice(consultation.getDefaultPrice());
            r.setPerformedOn(today.minusDays(1));
            treatments.saveRecord(r);
        }
    }

    private static Patient samplePatient(String first, String last, LocalDate dob, Gender gender,
                                         String phone, String email, String blood, String allergies) {
        Patient p = new Patient(first, last, dob, gender);
        p.setPhoneNumber(phone);
        p.setEmail(email);
        p.setBloodType(blood);
        p.setAllergies(allergies);
        return p;
    }

    private static Appointment sampleAppointment(Patient patient, User dentist, LocalDate date,
                                                 int hour, int minute, int minutes, String reason) {
        LocalTime start = LocalTime.of(hour, minute);
        Appointment a = new Appointment(patient.getPatientId(), dentist.getUserId(), date, start,
                start.plusMinutes(minutes), null);
        a.setReason(reason);
        return a;
    }
}
