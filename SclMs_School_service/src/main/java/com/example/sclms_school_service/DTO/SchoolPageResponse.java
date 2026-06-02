package com.example.sclms_school_service.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SchoolPageResponse {

    private List<AllSchoolDataDTO> schools;

    private int currentPage;

    private int totalPages;

    private long totalElements;

    private boolean hasNext;
}
