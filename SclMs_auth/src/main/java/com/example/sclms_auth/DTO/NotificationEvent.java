package com.example.sclms_auth.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificationEvent {

    private String type;
    private String recipient;
    private Map<String, String> payload;
}
