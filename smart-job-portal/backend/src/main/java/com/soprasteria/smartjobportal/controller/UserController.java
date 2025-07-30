package com.soprasteria.smartjobportal.controller;

import com.soprasteria.smartjobportal.dto.AuthDTO.UserResponse;
import com.soprasteria.smartjobportal.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/profile")
    public ResponseEntity<UserResponse> getUserProfile() {
        UserResponse userResponse = userService.getCurrentUserProfile();
        return ResponseEntity.ok(userResponse);
    }

    @PutMapping("/profile")
    public ResponseEntity<UserResponse> updateUserProfile(@RequestBody Map<String, Object> profileData) {
        UserResponse userResponse = userService.updateUserProfile(profileData);
        return ResponseEntity.ok(userResponse);
    }

    @PutMapping("/skills")
    public ResponseEntity<UserResponse> updateUserSkills(@RequestBody Map<String, String> request) {
        String skills = request.get("skills");
        UserResponse userResponse = userService.updateUserSkills(skills);
        return ResponseEntity.ok(userResponse);
    }
}
