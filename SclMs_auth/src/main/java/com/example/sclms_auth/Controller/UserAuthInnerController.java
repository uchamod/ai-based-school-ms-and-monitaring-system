package com.example.sclms_auth.Controller;


import com.example.sclms_auth.DTO.AccountDeleteDTO;
import com.example.sclms_auth.DTO.PasswordResetDTO;
import com.example.sclms_auth.DTO.UserAuthResponseDTO;
import com.example.sclms_auth.Service.UserAuthInnerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@CrossOrigin
@RequestMapping("/api/authoption")
public class UserAuthInnerController {
    private final UserAuthInnerService userAuthResetOptServices;

    @PostMapping("/logout")
    public ResponseEntity<String> logout(){
        return userAuthResetOptServices.logout();
    }

    @PutMapping("/password-reset")
    public ResponseEntity<String> resetPassword(@RequestBody PasswordResetDTO resetDto){
        return userAuthResetOptServices.resetPassword(resetDto);
    }

    @DeleteMapping("/delete-account")
    public ResponseEntity<String> deleteAccount(@RequestBody AccountDeleteDTO deleteDto){
        return userAuthResetOptServices.deleteAccount(deleteDto);
    }

    @GetMapping("/refresh")
    public ResponseEntity<UserAuthResponseDTO> refresh(@RequestHeader("Authorization") String token){
        return userAuthResetOptServices.refresh(token);
    }
}
