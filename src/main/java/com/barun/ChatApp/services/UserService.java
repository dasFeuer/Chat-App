package com.barun.ChatApp.services;

import com.barun.ChatApp.dto.UserRegistrationDto;
import com.barun.ChatApp.models.User;
import com.barun.ChatApp.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        if (userRepository.existsByUsername(registrationDto.getUsername())) {
            logger.warn("Registration attempt with existing username: {}", registrationDto.getUsername());
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already taken");
        }

        if (userRepository.existsByEmail(registrationDto.getEmail())) {
            logger.warn("Registration attempt with existing email: {}", registrationDto.getEmail());
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already taken");
        }

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
        return userRepository.findByUsername(username);
    }
}
