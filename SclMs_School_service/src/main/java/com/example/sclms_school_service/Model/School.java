package com.example.sclms_school_service.Model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Table(name="school")
public class School {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private UUID schoolId;
    private String name;
    private String address;
    private String province;
    private String district;
    private String discription;
    private String type;
    private String principal;
    private int stCount;
    private int techCount;
    private int labCount;
    private int buildingCount;
    private int comCount;
    private Boolean isSportSchool;
    private Boolean isPrimarySchool;
    private Boolean isPoshkaSchool;
    private Double lat;
    private Double lng;

    @OneToMany(mappedBy = "school", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<SchoolImage> images = new ArrayList<>();

}
