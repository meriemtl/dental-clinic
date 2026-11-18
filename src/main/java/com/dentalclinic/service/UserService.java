package com.dentalclinic.service;

import com.dentalclinic.dao.UserDAO;
import com.dentalclinic.dao.UserDAOImpl;
import com.dentalclinic.model.User;
import com.dentalclinic.model.User.UserRole;

import java.util.List;

/** Staff management (administrator only in the UI). */
public class UserService {
    private final UserDAO userDAO;

    public UserService() {
        this(new UserDAOImpl());
    }

    public UserService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    public List<User> findAllUsers() {
        return userDAO.findAll();
    }

    public List<User> findActiveDentists() {
        return userDAO.findActiveDentists();
    }

    public User createUser(String username, String fullName, UserRole role, String rawPassword) {
        username = clean(username);
        fullName = clean(fullName);
        if (username.isEmpty() || fullName.isEmpty() || role == null) {
            throw new IllegalArgumentException("Username, full name and role are required.");
        }
        AuthenticationService.validateNewPassword(rawPassword);
        if (userDAO.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("The username \"" + username + "\" is already taken.");
        }
        User user = new User();
        user.setUsername(username);
        user.setFullName(fullName);
        user.setRole(role);
        user.setActive(true);
        user.setPassword(AuthenticationService.hash(rawPassword));
        return userDAO.save(user);
    }

    /** Updates name, role and active flag (the username and password are left untouched). */
    public void updateUser(User user, String fullName, UserRole role, boolean active) {
        fullName = clean(fullName);
        if (fullName.isEmpty() || role == null) {
            throw new IllegalArgumentException("Full name and role are required.");
        }
        user.setFullName(fullName);
        user.setRole(role);
        user.setActive(active);
        userDAO.update(user);
    }

    public void resetPassword(User user, String newPassword) {
        AuthenticationService.validateNewPassword(newPassword);
        user.setPassword(AuthenticationService.hash(newPassword));
        userDAO.update(user);
    }

    private static String clean(String s) {
        return s == null ? "" : s.trim();
    }
}
