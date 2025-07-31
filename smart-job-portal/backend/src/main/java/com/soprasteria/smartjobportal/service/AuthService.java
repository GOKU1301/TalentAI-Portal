package com.soprasteria.smartjobportal.service;

import com.soprasteria.smartjobportal.dto.AuthDTO.JwtResponse;
import com.soprasteria.smartjobportal.dto.AuthDTO.LoginRequest;
import com.soprasteria.smartjobportal.dto.AuthDTO.RegisterRequest;
import com.soprasteria.smartjobportal.model.User;
import com.soprasteria.smartjobportal.repository.UserRepository;
import com.soprasteria.smartjobportal.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;

    public JwtResponse login(LoginRequest loginRequest) {
        try {
            System.out.println("Attempting to authenticate user: " + loginRequest.getUsername());
            
            // Check if user exists in database
            User userCheck = userRepository.findByUsername(loginRequest.getUsername())
                    .orElse(null);
            
            if (userCheck == null) {
                System.out.println("User not found in database: " + loginRequest.getUsername());
                throw new RuntimeException("User not found");
            }
            
            System.out.println("User found in database: " + userCheck.getUsername());
            System.out.println("Stored password hash length: " + userCheck.getPassword().length());
            
            // Attempt authentication
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );
            
            System.out.println("Authentication successful for user: " + loginRequest.getUsername());
            
            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtTokenProvider.generateToken((UserDetails) authentication.getPrincipal());
            
            System.out.println("JWT token generated successfully");
            
            User user = userRepository.findByUsername(loginRequest.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            return JwtResponse.builder()
                    .token(jwt)
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .role(user.getRole().name())
                    .build();
        } catch (Exception e) {
            System.out.println("Login error: " + e.getClass().getName() + ": " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    public JwtResponse register(RegisterRequest registerRequest) {
        // Check if username is already taken
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            throw new RuntimeException("Username is already taken");
        }

        // Check if email is already in use
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new RuntimeException("Email is already in use");
        }

        // Create new user
        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setEmail(registerRequest.getEmail());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setFullName(registerRequest.getFullName());
        
        // Set role
        try {
            if (registerRequest.getRole() != null) {
                user.setRole(User.Role.valueOf(registerRequest.getRole().toUpperCase()));
            }
        } catch (IllegalArgumentException e) {
            // Default to JOBSEEKER if invalid role provided
            user.setRole(User.Role.JOBSEEKER);
        }

        userRepository.save(user);

        // Generate JWT token
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        registerRequest.getUsername(),
                        registerRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtTokenProvider.generateToken((UserDetails) authentication.getPrincipal());

        return JwtResponse.builder()
                .token(jwt)
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();
    }
}
