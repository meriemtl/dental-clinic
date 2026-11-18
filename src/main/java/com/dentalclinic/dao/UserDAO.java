package com.dentalclinic.dao;

import com.dentalclinic.model.User;
import java.util.List;
import java.util.Optional;

public interface UserDAO {
    Optional<User> findByUsername(String username);
    Optional<User> findById(int userId);
    List<User> findAll();
    List<User> findActiveDentists();
    long count();
    User save(User user);
    void update(User user);
    void delete(int userId);
}
