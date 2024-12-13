package com.barun.ChatApp.services;

import com.barun.ChatApp.dto.UserRegistrationDto;
import com.barun.ChatApp.models.User;
import com.barun.ChatApp.models.ChatMessage;
import com.barun.ChatApp.repositories.UserRepository;
import com.barun.ChatApp.repositories.ChatMessageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
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
    public User registerNewUser(UserRegistrationDto registrationDto) {
        // Check for existing username
        if (userRepository.existsByUsername(registrationDto.getUsername())) {
            logger.warn("Registration attempt with existing username: {}", registrationDto.getUsername());
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already taken");
        }

        // Check for existing email
        if (userRepository.existsByEmail(registrationDto.getEmail())) {
            logger.warn("Registration attempt with existing email: {}", registrationDto.getEmail());
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already taken");
        }

        // Create and save the new user
        logger.info("Registering new user: {}", registrationDto.getUsername());
        User newUser = new User();
        newUser.setUsername(registrationDto.getUsername());
        newUser.setEmail(registrationDto.getEmail());
        newUser.setPassword(passwordEncoder.encode(registrationDto.getPassword()));
        newUser.setRole(User.UserRole.ROLE_USER);
        newUser.setEnabled(true);

        return userRepository.save(newUser);
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

