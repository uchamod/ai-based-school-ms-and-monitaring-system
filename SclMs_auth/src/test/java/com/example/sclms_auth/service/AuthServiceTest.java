package com.example.sclms_auth.service;

import com.example.sclms_auth.DTO.NotificationEvent;
import com.example.sclms_auth.DTO.UserAuthLoginDTO;
import com.example.sclms_auth.DTO.UserAuthResponseDTO;
import com.example.sclms_auth.Model.AccountStatus;
import com.example.sclms_auth.Model.Role;
import com.example.sclms_auth.Model.User;
import com.example.sclms_auth.Provider.Notification_Provider;
import com.example.sclms_auth.Reposotory.UserReposotory;
import com.example.sclms_auth.Service.UserAuthService;
import com.example.sclms_auth.Util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    User user;
    private UserAuthLoginDTO loginDto;
    @Mock
    private UserReposotory userAuthRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private Notification_Provider notificationProvider;

    @InjectMocks
    private UserAuthService userAuthService;

    @BeforeEach
    void setUp(){
        user=new User();
        loginDto=new UserAuthLoginDTO();
        user.setId(UUID.randomUUID());
        user.setEmail("uchamod52@gmail.com");
        user.setPasswordHash("123456");
        user.setPhoneNumber("0788396921");
        user.setRole(Role.SCHOOL);
        user.setAccountStatus(AccountStatus.PENDING);
        user.setEmailVerified(false);
        user.setPhoneVerified(false);

        loginDto.setEmail(user.getEmail());
        loginDto.setPassword("123456");
        loginDto.setPhoneNumber(user.getPhoneNumber());

    }

    @Test
    void registerUserTest(){


        when(userAuthRepository.existsByEmail(user.getEmail())).thenReturn(false);

        when(passwordEncoder.encode(user.getPasswordHash())).thenReturn("Encoded_password");



        when(jwtUtil.generateToken(user.getId(),user.getEmail(),user.getRole().name())).thenReturn("JWT_TOKEN");

        doNothing().when(notificationProvider).sendNotification(any(NotificationEvent.class));
        when(userAuthRepository.save(user)).thenReturn(user);

        ResponseEntity<UserAuthResponseDTO> response=userAuthService.registerUser(user);

        //assert the result
        assertEquals(HttpStatus.CREATED,response.getStatusCode());
        assertEquals("JWT_TOKEN",response.getBody().getToken());
        assertEquals(user.getEmail(),response.getBody().getEmail());
        assertEquals(user.getPhoneNumber(),response.getBody().getPhoneNumber());
        assertEquals(user.getRole().name(),response.getBody().getRole());
        assertNotNull(response.getBody());

        //verify the dependencies
        verify(userAuthRepository,times(1)).save(user);
        verify(userAuthRepository).existsByEmail(user.getEmail());
        verify(passwordEncoder).encode("123456");
        verify(jwtUtil).generateToken(user.getId(),user.getEmail(),user.getRole().name());
        verify(notificationProvider).sendNotification(any(NotificationEvent.class));

        System.out.println("user registered");
    }

    @Test
    void loginUserTest(){


        when(userAuthRepository.findByEmail(loginDto.getEmail())).then(invocationOnMock -> Optional.of(user));
       // when(user.equals(null)).thenReturn(true);
        when(passwordEncoder.matches(loginDto.getPassword(),user.getPasswordHash())).thenReturn(true);

        when(jwtUtil.generateToken(user.getId(),user.getEmail(),user.getRole().name())).thenReturn("JWT_TOKEN");


        ResponseEntity<UserAuthResponseDTO> response = userAuthService.loginUser(loginDto);

        //assert the result
        assertEquals(HttpStatus.OK,response.getStatusCode());
        assertEquals("JWT_TOKEN",response.getBody().getToken());
        assertEquals(user.getEmail(),response.getBody().getEmail());
        assertEquals(user.getPhoneNumber(),response.getBody().getPhoneNumber());
        assertEquals(user.getRole().name(),response.getBody().getRole());
        assertNotNull(response.getBody());

        //verify the dependencies
        verify(userAuthRepository,times(1)).findByEmail(loginDto.getEmail());
        verify(userAuthRepository).findByEmail(loginDto.getEmail());
        verify(jwtUtil).generateToken(user.getId(),user.getEmail(),user.getRole().name());


        System.out.println("user login");
    }
}
