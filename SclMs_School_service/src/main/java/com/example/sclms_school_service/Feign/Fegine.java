package com.example.sclms_school_service.Feign;



import org.springframework.cloud.openfeign.FeignClient;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient("SCLMS-AUTH-SERVICE")
public interface Fegine {
    @GetMapping("/api/auth/isexist/{userId}")
    ResponseEntity<Boolean> getUserById(@PathVariable UUID userId);
}
