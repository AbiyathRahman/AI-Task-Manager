package com.insightpulse.InsightPulse.service;

import com.insightpulse.InsightPulse.model.User;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public interface UserService {

    User createUser(User user);
    User getUserByUsername(String username);
    Optional<User> getUserById(Long id);
    boolean existsByUsername(String username);
    boolean existsById(Long id);
    void deleteUserById(Long id);
    void updateUserName(User user);


}
