package com.soprasteria.smartjobportal.service;

import com.soprasteria.smartjobportal.dto.AuthDTO.UserResponse;
import com.soprasteria.smartjobportal.model.User;
import com.soprasteria.smartjobportal.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public UserResponse getCurrentUserProfile() {
        User user = getCurrentUser();
        return UserResponse.fromEntity(user);
    }

    public UserResponse updateUserProfile(Map<String, Object> profileData) {
        User user = getCurrentUser();
        
        if (profileData.containsKey("fullName")) {
            user.setFullName((String) profileData.get("fullName"));
        }
        
        if (profileData.containsKey("email")) {
            String newEmail = (String) profileData.get("email");
            if (!newEmail.equals(user.getEmail()) && userRepository.existsByEmail(newEmail)) {
                throw new RuntimeException("Email is already in use");
            }
            user.setEmail(newEmail);
        }
        
        if (profileData.containsKey("education")) {
            user.setEducation((String) profileData.get("education"));
        }
        
        if (profileData.containsKey("experience")) {
            Object expObj = profileData.get("experience");
            if (expObj != null) {
                if (expObj instanceof Integer) {
                    user.setExperience((Integer) expObj);
                } else if (expObj instanceof String) {
                    try {
                        user.setExperience(Integer.parseInt((String) expObj));
                    } catch (NumberFormatException e) {
                        // Ignore invalid number format
                    }
                }
            }
        }
        
        if (profileData.containsKey("position")) {
            user.setPosition((String) profileData.get("position"));
        }
        
        if (profileData.containsKey("company")) {
            user.setCompany((String) profileData.get("company"));
        }
        
        userRepository.save(user);
        return UserResponse.fromEntity(user);
    }

    public UserResponse updateUserSkills(String skills) {
        User user = getCurrentUser();
        user.setSkills(skills);
        userRepository.save(user);
        return UserResponse.fromEntity(user);
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
    }
}
