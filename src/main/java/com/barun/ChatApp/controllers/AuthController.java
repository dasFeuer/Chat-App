package com.barun.ChatApp.controllers;

import com.barun.ChatApp.dto.AuthResponse;
import com.barun.ChatApp.dto.LoginDto;
import com.barun.ChatApp.dto.RegisterDto;
import com.barun.ChatApp.models.User;
import com.barun.ChatApp.security.JwtTokenProvider;
import com.barun.ChatApp.services.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> registerUser(@Valid @RequestBody RegisterDto registrationDto) {
        logger.info("Registering user: {}", registrationDto.getUsername());
        User user = userService.registerNewUser(registrationDto);
        String token = jwtTokenProvider.createToken(user.getUsername(), user.getRole());
        logger.info("User registered successfully: {}", registrationDto.getUsername());
        return ResponseEntity.ok(new AuthResponse(user.getUsername(), token));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> loginUser(@Valid @RequestBody LoginDto loginDto) {
        logger.info("Login attempt for user: {}", loginDto.getUsername());
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginDto.getUsername(),
                        loginDto.getPassword()
                )
        );
        User user = userService.findByUsername(loginDto.getUsername())
                .orElseThrow(() -> {
                    logger.warn("User not found: {}", loginDto.getUsername());
                    return new RuntimeException("User not found");
                });
        String token = jwtTokenProvider.createToken(user.getUsername(), user.getRole());
        logger.info("User logged in successfully: {}", loginDto.getUsername());
        return ResponseEntity.ok(new AuthResponse(user.getUsername(), token));
    }
}