package com.example.sclms_school_service.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FilterSchoolDTO {


    private String name;
    private String address;
    private String province;
    private String district;
    private String type;
    private Integer minStudentCount;
    private Integer maxStudentCount;
    private Integer minTeacherCount;
    private Integer maxTeacherCount;
    private Boolean isSportSchool;
    private Boolean isPrimarySchool;
    private Boolean isPoshkaSchool;

    @Override
    public String toString() {
        return String.format("%s-%s-%s-%s-%s-%s-%s",
                Objects.toString(name, ""),
                Objects.toString(district, ""),
                Objects.toString(province, ""),
                Objects.toString(type, ""),
                Objects.toString(isSportSchool, ""),
                Objects.toString(isPrimarySchool, ""),
                Objects.toString(isPoshkaSchool, "")
        );
    }
}
