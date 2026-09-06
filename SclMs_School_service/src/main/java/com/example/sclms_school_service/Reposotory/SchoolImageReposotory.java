package com.example.sclms_school_service.Reposotory;

import com.example.sclms_school_service.Model.SchoolImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


import java.util.UUID;

@Repository
public interface SchoolImageReposotory extends JpaRepository<SchoolImage, UUID> {

}
