package com.example.sclms_school_service.Reposotory;

import com.example.sclms_school_service.Model.School;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface SchoolReposotory extends JpaRepository<School, UUID>, JpaSpecificationExecutor<School> {

    School findBySchoolId(UUID schoolId);
}
