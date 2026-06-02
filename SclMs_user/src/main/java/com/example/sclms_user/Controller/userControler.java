package com.example.sclms_user.Controller;

import com.example.sclms_user.DTO.userResponseDto;
import com.example.sclms_user.Model.enums.Role;
import com.example.sclms_user.Service.userService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@CrossOrigin
@RequestMapping("/api/user")
public class userControler {

    private final userService userService;

    @GetMapping("/getAllUser")
    public ResponseEntity<List<userResponseDto>> getAllUserByRole(@RequestParam Role role) {
        return userService.getAllUserByRole(role);
    }

    @GetMapping("/getUserById")
    public ResponseEntity<userResponseDto> getUserById(@RequestParam UUID id) {
        return userService.getUserById(id);
    }

    @GetMapping("/getUserByEmail")
    public ResponseEntity<userResponseDto> getUserByEmail(@RequestParam String email) {
        return userService.getUserByEmail(email);
    }
}
