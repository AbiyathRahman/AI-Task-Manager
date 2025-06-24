package com.insightpulse.InsightPulse.repository;

import com.insightpulse.InsightPulse.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByName(String name);
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);

}
