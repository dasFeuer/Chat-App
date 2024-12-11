package com.barun.ChatApp.services;

import com.barun.ChatApp.dto.UserRegistrationDto;
import com.barun.ChatApp.models.User;
import com.barun.ChatApp.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional
    public User registerNewUser(UserRegistrationDto registrationDto){
        if(userRepository.existsByUsername(registrationDto.getUsername())){
            throw new RuntimeException("Username already taken");
        }

        if(userRepository.existsByEmail(registrationDto.getUsername())){
            throw new RuntimeException("Email already taken");
        }

        User newUser = new User();
        newUser.setUsername(registrationDto.getUsername());
        newUser.setEmail(registrationDto.getEmail());
        newUser.setPassword(passwordEncoder.encode(registrationDto.getPassword()));
        newUser.setRole(User.UserRole.ROLE_USER);
        newUser.setEnabled(true);

        return userRepository.save(newUser);
    }

    public Optional<User> findByUsername(String username){
        return userRepository.findByUsername(username);
    }
}
