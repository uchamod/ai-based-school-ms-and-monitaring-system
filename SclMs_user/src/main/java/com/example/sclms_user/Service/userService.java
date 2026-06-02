package com.example.sclms_user.Service;

import com.example.sclms_user.DTO.userResponseDto;
import com.example.sclms_user.Model.SchoolModel;
import com.example.sclms_user.Model.enums.Role;
import com.example.sclms_user.Reposotory.userReposotory;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class userService {

    private final userReposotory userAuthRepository;

    public ResponseEntity<List<userResponseDto>> getAllUserByRole(Role role) {
        try {
            List<SchoolModel> users = userAuthRepository.findAllByRole(role);
            List<userResponseDto> response = users.stream()
                    .map(this::mapToResponseDto)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    public ResponseEntity<userResponseDto> getUserById(UUID id) {
        try {
            return userAuthRepository.findById(id)
                    .map(user -> ResponseEntity.ok(mapToResponseDto(user)))
                    .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    public ResponseEntity<userResponseDto> getUserByEmail(String email) {
        try {
            return userAuthRepository.findByEmail(email)
                    .map(user -> ResponseEntity.ok(mapToResponseDto(user)))
                    .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private userResponseDto mapToResponseDto(SchoolModel school) {
        return userResponseDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .role(user.getRole())
                .accountStatus(user.getAccountStatus())
                .emailVerified(user.isEmailVerified())
                .phoneVerified(user.isPhoneVerified())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
