package com.dentalclinic.service;

import com.dentalclinic.dao.UserDAO;
import com.dentalclinic.dao.UserDAOImpl;
import com.dentalclinic.model.User;
import org.mindrot.jbcrypt.BCrypt;

import java.util.Optional;

public class AuthenticationService {
    public static final int MIN_PASSWORD_LENGTH = 6;

    private final UserDAO userDAO;

    public AuthenticationService() {
        this(new UserDAOImpl());
    }

    public AuthenticationService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    /** Returns the user when the credentials are valid, otherwise throws SecurityException. */
    public User authenticate(String username, String password) {
        Optional<User> found = userDAO.findByUsername(username);
        if (found.isEmpty() || !found.get().isActive() || !passwordMatches(password, found.get().getPassword())) {
            throw new SecurityException("Invalid username or password.");
        }
        return found.get();
    }

    /** Changes the password of a user after checking the current one against the stored hash. */
    public void changePassword(User user, String currentPassword, String newPassword) {
        User stored = userDAO.findById(user.getUserId())
                .orElseThrow(() -> new SecurityException("This account no longer exists."));
        if (!passwordMatches(currentPassword, stored.getPassword())) {
            throw new SecurityException("The current password is incorrect.");
        }
        validateNewPassword(newPassword);
        stored.setPassword(hash(newPassword));
        userDAO.update(stored);
        user.setPassword(stored.getPassword());
    }

    public static String hash(String rawPassword) {
        return BCrypt.hashpw(rawPassword, BCrypt.gensalt());
    }

    public static void validateNewPassword(String password) {
        if (password == null || password.length() < MIN_PASSWORD_LENGTH) {
            throw new IllegalArgumentException(
                    "The password must contain at least " + MIN_PASSWORD_LENGTH + " characters.");
        }
    }

    private static boolean passwordMatches(String raw, String hash) {
        try {
            return raw != null && hash != null && BCrypt.checkpw(raw, hash);
        } catch (IllegalArgumentException e) {
            return false; // stored value is not a valid BCrypt hash
        }
    }
}
