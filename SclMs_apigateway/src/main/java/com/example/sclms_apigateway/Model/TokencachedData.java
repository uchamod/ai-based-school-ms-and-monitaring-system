package com.example.sclms_apigateway.Model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TokencachedData {
    private  String userId;
    private  String email;
    private  String role;
}
