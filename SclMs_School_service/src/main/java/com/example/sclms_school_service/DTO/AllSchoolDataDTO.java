package com.example.sclms_school_service.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AllSchoolDataDTO {

    private UUID id;
    private String name;
    private String address;
    private String district;
    private String type;
}
