package com.example.sclms_auth.Service;


import com.example.sclms_auth.DTO.NotificationEvent;
import com.example.sclms_auth.DTO.UserAuthLoginDTO;
import com.example.sclms_auth.DTO.UserAuthResponseDTO;
import com.example.sclms_auth.Model.AccountStatus;
import com.example.sclms_auth.Model.User;
import com.example.sclms_auth.Provider.Notification_Provider;
import com.example.sclms_auth.Reposotory.UserReposotory;
import com.example.sclms_auth.Util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserAuthService {
    private final UserReposotory userAuthRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final Notification_Provider notificationProvider;
    private  NotificationEvent notificationEvent;
    //register new user(school,gov or public view)
    public ResponseEntity<UserAuthResponseDTO> registerUser(User user){
        try{
            if (userAuthRepository.existsByEmail(user.getEmail())) {
                return ResponseEntity.status(HttpStatus.CONFLICT).build();
            }

            user.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));
            user.setAccountStatus(AccountStatus.PENDING);
            user.setEmailVerified(false);
            user.setPhoneVerified(false);
            User savedUser = userAuthRepository.save(user);
            //send email
            notificationEvent = new  NotificationEvent("SCHOOL_REGISTERED",savedUser.getEmail(), Map.of("schoolEmail",savedUser.getEmail()));
            notificationProvider.sendNotification(notificationEvent);

            String token = jwtUtil.generateToken(savedUser.getId(), savedUser.getEmail(), savedUser.getRole().name());

            UserAuthResponseDTO response = new UserAuthResponseDTO();
            response.setToken(token);
            response.setId(savedUser.getId());
            response.setRole(savedUser.getRole().name());
            response.setEmail(savedUser.getEmail());
            response.setPhoneNumber(savedUser.getPhoneNumber());

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        }catch (Exception e){
            System.out.println(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    //log into portal
    public ResponseEntity<UserAuthResponseDTO> loginUser(UserAuthLoginDTO loginDto){
        try{
            User user = userAuthRepository.findByEmail(loginDto.getEmail())
                    .orElse(null);

            if (user == null || !passwordEncoder.matches(loginDto.getPassword(), user.getPasswordHash())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            String token = jwtUtil.generateToken(user.getId(), user.getEmail(), user.getRole().name());

            UserAuthResponseDTO response = new UserAuthResponseDTO();
            response.setToken(token);
            response.setId(user.getId());
            response.setRole(user.getRole().name());
            response.setEmail(user.getEmail());
            response.setPhoneNumber(user.getPhoneNumber());

            return ResponseEntity.ok(response);
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    public ResponseEntity<Boolean> getUserById(UUID userId){
        try{
            Optional<User> user = userAuthRepository.findById(userId);
            if(user.isPresent()){
                return ResponseEntity.ok(true);
            }else{
                return ResponseEntity.ok(false);
            }
        } catch (Exception e) {
            System.out.println("internal server error");
            throw new RuntimeException(e);
        }
    }
}
