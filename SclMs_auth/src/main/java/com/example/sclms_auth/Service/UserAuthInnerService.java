package com.example.sclms_auth.Service;

import com.example.sclms_auth.DTO.AccountDeleteDTO;
import com.example.sclms_auth.DTO.PasswordResetDTO;
import com.example.sclms_auth.DTO.UserAuthResponseDTO;
import com.example.sclms_auth.Model.User;
import com.example.sclms_auth.Reposotory.UserReposotory;
import com.example.sclms_auth.Util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserAuthInnerService {

    private final UserReposotory userAuthRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;


    public ResponseEntity<String> logout(){
        return ResponseEntity.ok("Logout successful");
    }


    public ResponseEntity<String> resetPassword(PasswordResetDTO resetDto){
        try {
            Optional<User> userOptional = userAuthRepository.findByEmail(resetDto.getEmail());
            if (userOptional.isEmpty()) {
                System.out.println("User not found for email: " + resetDto.getEmail());
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
            }

            User user = userOptional.get();

            //add extar otp layer

            /*if (!passwordEncoder.matches(resetDto.getOldPassword(), user.getPasswordHash())) {
                System.out.println("Invalid old password for user: " + user.getEmail());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid old password");
            }*/

            user.setPasswordHash(passwordEncoder.encode(resetDto.getNewPassword()));
            userAuthRepository.save(user);

            return ResponseEntity.status(HttpStatus.OK).body("Password reset successful");
        } catch (Exception e) {
            System.out.println("An error occurred during password reset: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred during password reset");
        }
    }


    public ResponseEntity<String> deleteAccount(AccountDeleteDTO deleteDto){
        try {
            Optional<User> userOptional = userAuthRepository.findByEmail(deleteDto.getEmail());
            if (userOptional.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
            }

            User user = userOptional.get();
            if (!passwordEncoder.matches(deleteDto.getPassword(), user.getPasswordHash())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid password");
            }

            userAuthRepository.delete(user);
            return ResponseEntity.ok("Account deletion successful");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred during account deletion");
        }
    }


    public ResponseEntity<UserAuthResponseDTO> refresh(String token){
        try {
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            if (token == null || !jwtUtil.isTokenValid(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            String email = jwtUtil.extractEmail(token);
            Optional<User> userOptional = userAuthRepository.findByEmail(email);

            if (userOptional.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            User user = userOptional.get();
            String newToken = jwtUtil.generateToken(user.getId(), user.getEmail(), user.getRole().name());

            UserAuthResponseDTO response = new UserAuthResponseDTO();
            response.setToken(newToken);
            response.setId(user.getId());
            response.setRole(user.getRole().name());
            response.setEmail(user.getEmail());
            response.setPhoneNumber(user.getPhoneNumber());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
