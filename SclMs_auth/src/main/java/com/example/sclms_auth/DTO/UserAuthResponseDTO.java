package com.example.sclms_auth.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserAuthResponseDTO {
    private String token;
    private String type="Bearer";
    private UUID id;
    private String role;
    private String email;
    private String phoneNumber;
}
