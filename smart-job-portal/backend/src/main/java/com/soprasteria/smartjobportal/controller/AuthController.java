package com.soprasteria.smartjobportal.controller;

import com.soprasteria.smartjobportal.dto.AuthDTO.JwtResponse;
import com.soprasteria.smartjobportal.dto.AuthDTO.LoginRequest;
import com.soprasteria.smartjobportal.dto.AuthDTO.RegisterRequest;
import com.soprasteria.smartjobportal.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@RequestBody LoginRequest loginRequest) {
        JwtResponse jwtResponse = authService.login(loginRequest);
        return ResponseEntity.ok(jwtResponse);
    }

    @PostMapping("/register")
    public ResponseEntity<JwtResponse> register(@RequestBody RegisterRequest registerRequest) {
        JwtResponse jwtResponse = authService.register(registerRequest);
        return ResponseEntity.ok(jwtResponse);
    }
}
