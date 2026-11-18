package com.dentalclinic.dao;

import com.dentalclinic.model.User;
import com.dentalclinic.utils.Jdbc;
import com.dentalclinic.utils.Jdbc.Mapper;

import java.util.List;
import java.util.Optional;

public class UserDAOImpl implements UserDAO {

    private static final String SELECT = """
            SELECT user_id, username, password, full_name, role, active, created_at, updated_at
            FROM users
            """;
    private static final String FIND_BY_USERNAME = SELECT + "WHERE username = ?";
    private static final String FIND_BY_ID = SELECT + "WHERE user_id = ?";
    private static final String FIND_ALL = SELECT + "ORDER BY full_name";
    private static final String FIND_ACTIVE_DENTISTS =
            SELECT + "WHERE role = 'DENTIST' AND active = 1 ORDER BY full_name";
    private static final String COUNT = "SELECT COUNT(*) FROM users";
    private static final String INSERT =
            "INSERT INTO users (username, password, full_name, role, active) VALUES (?, ?, ?, ?, ?)";
    private static final String UPDATE =
            "UPDATE users SET username = ?, password = ?, full_name = ?, role = ?, active = ? WHERE user_id = ?";
    private static final String DELETE = "DELETE FROM users WHERE user_id = ?";

    private static final Mapper<User> MAPPER = rs -> {
        User u = new User();
        u.setUserId(rs.getInt("user_id"));
        u.setUsername(rs.getString("username"));
        u.setPassword(rs.getString("password"));
        u.setFullName(rs.getString("full_name"));
        u.setRole(User.UserRole.valueOf(rs.getString("role")));
        u.setActive(rs.getInt("active") != 0);
        u.setCreatedAt(Jdbc.timestamp(rs, "created_at"));
        u.setUpdatedAt(Jdbc.timestamp(rs, "updated_at"));
        return u;
    };

    @Override
    public Optional<User> findByUsername(String username) {
        return Jdbc.queryOne(FIND_BY_USERNAME, Jdbc.params(username), MAPPER);
    }

    @Override
    public Optional<User> findById(int userId) {
        return Jdbc.queryOne(FIND_BY_ID, Jdbc.params(userId), MAPPER);
    }

    @Override
    public List<User> findAll() {
        return Jdbc.query(FIND_ALL, Jdbc.NONE, MAPPER);
    }

    @Override
    public List<User> findActiveDentists() {
        return Jdbc.query(FIND_ACTIVE_DENTISTS, Jdbc.NONE, MAPPER);
    }

    @Override
    public long count() {
        return Jdbc.count(COUNT, Jdbc.NONE);
    }

    @Override
    public User save(User user) {
        if (user.getUserId() == 0) {
            int id = Jdbc.insert(INSERT, Jdbc.params(user.getUsername(), user.getPassword(),
                    user.getFullName(), user.getRole(), user.isActive() ? 1 : 0));
            user.setUserId(id);
        } else {
            update(user);
        }
        return user;
    }

    @Override
    public void update(User user) {
        if (user.getUserId() == 0) {
            throw new IllegalArgumentException("User ID cannot be 0 for update operation.");
        }
        Jdbc.update(UPDATE, Jdbc.params(user.getUsername(), user.getPassword(), user.getFullName(),
                user.getRole(), user.isActive() ? 1 : 0, user.getUserId()));
    }

    @Override
    public void delete(int userId) {
        Jdbc.update(DELETE, Jdbc.params(userId));
    }
}
