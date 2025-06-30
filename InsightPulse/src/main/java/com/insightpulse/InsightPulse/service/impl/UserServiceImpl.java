package com.insightpulse.InsightPulse.service.impl;

import com.insightpulse.InsightPulse.model.User;
import com.insightpulse.InsightPulse.repository.UserRepository;
import com.insightpulse.InsightPulse.service.UserService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Override
    @Transactional
    public User createUser(User user){
        return userRepository.save(user);
    }
    @Override
    public User getUserByUsername(String username){
        return userRepository.findByUsername(username).orElse(null);
    }
    @Override
    public boolean existsByUsername(String username){
        return userRepository.existsByUsername(username);
    }
    @Override
    public boolean existsById(Long id){
        return userRepository.existsById(id);
    }
    @Override
    public void deleteUserById(Long id){
        userRepository.deleteById(id);
    }
    @Override
    public Optional<User> getUserById(Long id){
        return Optional.ofNullable(userRepository.findById(id).orElse(null));
    }
    @Override
    public void updateUserName(User user){
        userRepository.save(user);
    }

}
