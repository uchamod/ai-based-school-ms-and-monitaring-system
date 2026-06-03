package com.example.sclms_auth.Controller;

import com.example.sclms_auth.DTO.UserAuthLoginDTO;
import com.example.sclms_auth.DTO.UserAuthResponseDTO;
import com.example.sclms_auth.Model.User;
import com.example.sclms_auth.Service.UserAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class UserAuthController {
    private final UserAuthService userServices;

    @GetMapping("/isexist/{userId}")
    public ResponseEntity<Boolean> getUserById(@PathVariable UUID userId){
        return userServices.getUserById(userId);
    }

    @PostMapping("/register")
    public ResponseEntity<UserAuthResponseDTO> register(@RequestBody User user){
        return userServices.registerUser(user);
    }

    @PostMapping("/login")
    public ResponseEntity<UserAuthResponseDTO> login(@RequestBody UserAuthLoginDTO loginDto){
        return userServices.loginUser(loginDto);
    }


}
