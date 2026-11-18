package com.dentalclinic.utils;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Small JDBC helper used by every DAO. Each call opens its own connection and
 * closes it afterwards (the original shared connection was closed after the
 * first query, which broke every following operation).
 */
public final class Jdbc {
    private Jdbc() {
    }

    @FunctionalInterface
    public interface Binder {
        void bind(PreparedStatement ps) throws SQLException;
    }

    @FunctionalInterface
    public interface Mapper<T> {
        T map(ResultSet rs) throws SQLException;
    }

    public static final Binder NONE = ps -> {
    };

    /** Binds positional parameters; enums are stored by name, null becomes SQL NULL. */
    public static Binder params(Object... values) {
        return ps -> {
            for (int i = 0; i < values.length; i++) {
                Object v = values[i];
                if (v instanceof Enum<?> e) {
                    v = e.name();
                }
                ps.setObject(i + 1, v);
            }
        };
    }

    public static <T> List<T> query(String sql, Binder binder, Mapper<T> mapper) {
        try (Connection c = DatabaseConnection.getConnection()) {
            return query(c, sql, binder, mapper);
        } catch (SQLException e) {
            throw wrap(e);
        }
    }

    public static <T> List<T> query(Connection c, String sql, Binder binder, Mapper<T> mapper) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            binder.bind(ps);
            try (ResultSet rs = ps.executeQuery()) {
                List<T> out = new ArrayList<>();
                while (rs.next()) {
                    out.add(mapper.map(rs));
                }
                return out;
            }
        }
    }

    public static <T> Optional<T> queryOne(String sql, Binder binder, Mapper<T> mapper) {
        List<T> rows = query(sql, binder, mapper);
        return rows.isEmpty() ? Optional.empty() : Optional.ofNullable(rows.get(0));
    }

    public static int update(String sql, Binder binder) {
        try (Connection c = DatabaseConnection.getConnection()) {
            return update(c, sql, binder);
        } catch (SQLException e) {
            throw wrap(e);
        }
    }

    public static int update(Connection c, String sql, Binder binder) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            binder.bind(ps);
            return ps.executeUpdate();
        }
    }

    /** Executes an INSERT and returns the generated auto-increment key. */
    public static int insert(String sql, Binder binder) {
        try (Connection c = DatabaseConnection.getConnection()) {
            return insert(c, sql, binder);
        } catch (SQLException e) {
            throw wrap(e);
        }
    }

    public static int insert(Connection c, String sql, Binder binder) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            binder.bind(ps);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
            throw new SQLException("No generated key returned.");
        }
    }

    public static long count(String sql, Binder binder) {
        List<Long> rows = query(sql, binder, rs -> rs.getLong(1));
        return rows.isEmpty() ? 0L : rows.get(0);
    }

    public static BigDecimal sum(String sql, Binder binder) {
        List<BigDecimal> rows = query(sql, binder, rs -> rs.getBigDecimal(1));
        return rows.isEmpty() || rows.get(0) == null ? BigDecimal.ZERO : rows.get(0);
    }

    public static LocalDateTime timestamp(ResultSet rs, String column) throws SQLException {
        Timestamp t = rs.getTimestamp(column);
        return t == null ? null : t.toLocalDateTime();
    }

    public static Integer nullableInt(ResultSet rs, String column) throws SQLException {
        int v = rs.getInt(column);
        return rs.wasNull() ? null : v;
    }

    public static DataAccessException wrap(SQLException e) {
        return new DataAccessException("Database error: " + e.getMessage(), e);
    }
}
