package com.dentalclinic.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/** Opens JDBC connections using the settings from config.properties. */
public final class DatabaseConnection {
    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC driver not found on the classpath.");
        }
    }

    private DatabaseConnection() {
    }

    private static Properties credentials() {
        Properties props = new Properties();
        props.setProperty("user", AppConfig.dbUser());
        props.setProperty("password", AppConfig.dbPassword());
        return props;
    }

    /** A new connection to the clinic database. Callers must close it. */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(AppConfig.jdbcUrl(true), credentials());
    }

    /** A new connection to the MySQL server itself (no database selected). */
    static Connection getServerConnection() throws SQLException {
        return DriverManager.getConnection(AppConfig.jdbcUrl(false), credentials());
    }

    /** Creates the database, the tables and the default data when they do not exist yet. */
    public static void initialize() {
        DatabaseInitializer.run();
    }
}
