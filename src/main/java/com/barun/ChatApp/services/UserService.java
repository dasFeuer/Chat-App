package com.barun.ChatApp.services;

import com.barun.ChatApp.dto.RegisterDto;
import com.barun.ChatApp.models.User;
import com.barun.ChatApp.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional
    public User registerNewUser(RegisterDto registerDto) {
        if (userRepository.existsByUsername(registerDto.getUsername())) {
            logger.warn("Username exists: {}", registerDto.getUsername());
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username is already taken");
        }

        if (userRepository.existsByEmail(registerDto.getEmail())) {
            logger.warn("Email exits: {}", registerDto.getEmail());
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email is already taken");
        }

        logger.info("Registering new user: {}", registerDto.getUsername());
        User newUser = new User();
        newUser.setUsername(registerDto.getUsername());
        newUser.setEmail(registerDto.getEmail());
        newUser.setPassword(passwordEncoder.encode(registerDto.getPassword()));
        newUser.setRole(User.UserRole.ROLE_USER);
        newUser.setEnabled(true);

        return userRepository.save(newUser);
    }

    public List<User> getAllUser(){
        return userRepository.findAll();
    }

    public Optional<User> findByUsername(String username) {
        logger.info("Fetching user with username: {}", username);
        return userRepository.findByUsername(username);
    }

    public Optional<User> findByUsernameOrEmail(String identifier) {
        logger.info("Fetching user by username or email: {}", identifier);
        return userRepository.findByUsernameOrEmail(identifier, identifier);
    }
}

