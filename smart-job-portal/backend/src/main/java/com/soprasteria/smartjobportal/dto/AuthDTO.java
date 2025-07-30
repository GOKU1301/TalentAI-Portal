package com.soprasteria.smartjobportal.dto;

import com.soprasteria.smartjobportal.model.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class AuthDTO {

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class LoginRequest {
        private String username;
        private String password;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RegisterRequest {
        private String username;
        private String email;
        private String password;
        private String fullName;
        private String role;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class JwtResponse {
        private String token;
        private String username;
        private String email;
        private String role;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UserResponse {
        private Integer id;
        private String username;
        private String email;
        private String fullName;
        private String role;
        private String skills;
        private String company;
        private String position;
        private String education;
        private Integer experience;

        public static UserResponse fromEntity(User user) {
            return UserResponse.builder()
                    .id(user.getId())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .fullName(user.getFullName())
                    .role(user.getRole().name())
                    .skills(user.getSkills())
                    .company(user.getCompany())
                    .position(user.getPosition())
                    .education(user.getEducation())
                    .experience(user.getExperience())
                    .build();
        }
    }
}
