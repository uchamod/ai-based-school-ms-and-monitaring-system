package com.example.sclms_auth.DTO;

import lombok.Data;

@Data
public class PasswordResetDTO {
    private String email;

    private String newPassword;
}
