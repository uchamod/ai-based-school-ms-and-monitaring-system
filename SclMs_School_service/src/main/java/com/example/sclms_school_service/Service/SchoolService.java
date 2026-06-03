package com.example.sclms_school_service.Service;


import com.example.sclms_school_service.DTO.AllSchoolDataDTO;
import com.example.sclms_school_service.DTO.FilterSchoolDTO;
import com.example.sclms_school_service.DTO.SchoolPageResponse;
import com.example.sclms_school_service.Feign.Fegine;
import com.example.sclms_school_service.Model.School;
import com.example.sclms_school_service.Model.SchoolImage;
import com.example.sclms_school_service.Reposotory.SchoolImageReposotory;
import com.example.sclms_school_service.Reposotory.SchoolReposotory;
import com.example.sclms_school_service.Specification.SchoolSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SchoolService {

    private final SchoolReposotory schoolReposotory;
    private final SchoolImageReposotory schoolImageReposotory;
    private final Fegine fegineClient;
    private static final String IMAGE_UPLOAD_DIR = "uploads/school-images/";

    //complete school profile
    @Transactional
    public ResponseEntity<String> completeSchool(School school, List<MultipartFile> images,UUID schoolId) {
        try {
            System.out.println("Starting school profile completion for schoolId: " + schoolId);
            if(!fegineClient.getUserById(schoolId).getBody())
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("School does not exist.Register the school first");

            if(schoolReposotory.findBySchoolId(schoolId) != null)
                return ResponseEntity.status(HttpStatus.CONFLICT).body("you already have a school profile");


            school.setSchoolId(schoolId);
            School savedSchool = schoolReposotory.save(school);

            if (images != null && !images.isEmpty()) {
                saveImages(savedSchool, images);
            }

            return ResponseEntity.status(HttpStatus.CREATED).body("School profile completed successfully");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to complete school profile: " + e.getMessage());
        }
    }

//get all school by page(per pg-:15)
    public ResponseEntity<SchoolPageResponse> getAllSchool(int page) {
        try {
            Pageable pageable = PageRequest.of(page, 15);
            Page<School> schoolPage = schoolReposotory.findAll(pageable);

            if (schoolPage.isEmpty())
                return ResponseEntity.noContent().build();

            List<AllSchoolDataDTO> dtoList = schoolPage.getContent().stream()
                    .map(school -> new AllSchoolDataDTO(
                            school.getId(),
                            school.getName(),
                            school.getAddress(),
                            school.getDistrict(),
                            school.getType()
                    ))
                    .collect(Collectors.toList());
            SchoolPageResponse response = new SchoolPageResponse(
                    dtoList,
                    schoolPage.getNumber(),
                    schoolPage.getTotalPages(),
                    schoolPage.getTotalElements(),
                    schoolPage.hasNext());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
//get school by id(full profile)
    public ResponseEntity<School> getSchoolById(UUID id) {
        try {
            School school = schoolReposotory.findById(id).orElse(null);
            if (school == null)
                return ResponseEntity.notFound().build();
            return ResponseEntity.ok(school);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
//update school by id(full profile or given varibles)
    @Transactional
    public ResponseEntity<String> updateSchool(School school, List<MultipartFile> images,UUID schoolId) {
        try {
            if(!fegineClient.getUserById(schoolId).getBody())
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("School does not exist.Register the school first");
            School existingSchool = schoolReposotory.findBySchoolId(schoolId);
            if (existingSchool == null)
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("School not found");

            if (school.getName() != null) existingSchool.setName(school.getName());
            if (school.getAddress() != null) existingSchool.setAddress(school.getAddress());
            if (school.getProvince() != null) existingSchool.setProvince(school.getProvince());
            if (school.getDistrict() != null) existingSchool.setDistrict(school.getDistrict());
            if (school.getDiscription() != null) existingSchool.setDiscription(school.getDiscription());
            if (school.getType() != null) existingSchool.setType(school.getType());
            if (school.getPrincipal() != null) existingSchool.setPrincipal(school.getPrincipal());
            if (school.getStCount() != 0) existingSchool.setStCount(school.getStCount());
            if (school.getTechCount() != 0) existingSchool.setTechCount(school.getTechCount());
            if (school.getLabCount() != 0) existingSchool.setLabCount(school.getLabCount());
            if (school.getBuildingCount() != 0) existingSchool.setBuildingCount(school.getBuildingCount());
            if (school.getComCount() != 0) existingSchool.setComCount(school.getComCount());
            if (school.getIsSportSchool() != null) existingSchool.setIsSportSchool(school.getIsSportSchool());
            if (school.getIsPrimarySchool() != null) existingSchool.setIsPrimarySchool(school.getIsPrimarySchool());
            if (school.getIsPoshkaSchool() != null) existingSchool.setIsPoshkaSchool(school.getIsPoshkaSchool());
            if (school.getLat() != null) existingSchool.setLat(school.getLat());
            if (school.getLng() != null) existingSchool.setLng(school.getLng());

            schoolReposotory.save(existingSchool);

            if (images != null && !images.isEmpty()) {
                saveImages(existingSchool, images);
            }

            return ResponseEntity.ok("School updated successfully");
        } catch (Exception e) {
            System.out.println("Failed to update school: " +e.getMessage());
            return ResponseEntity.internalServerError().body("Failed to update school: " + e.getMessage());
        }
    }
//delete school by id
    @Transactional
    public ResponseEntity<String> deleteSchool(UUID id) {
        try {
            if (!schoolReposotory.existsById(id))
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("School not found");

            schoolReposotory.deleteById(id);
            return ResponseEntity.ok("School deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to delete school: " + e.getMessage());
        }
    }
//filter/search school by given arguments in filterSchoolDTO
    public ResponseEntity<SchoolPageResponse> filterSchool(FilterSchoolDTO filterSchoolDTO,int page) {
        try {
            Pageable pageable = PageRequest.of(page, 15);

            Specification<School> spec = SchoolSpecification.filterBy(filterSchoolDTO);
            Page<School> schools = schoolReposotory.findAll(spec,pageable);

            if (schools.isEmpty())
                return ResponseEntity.noContent().build();

            List<AllSchoolDataDTO> dtoList = schools.getContent().stream()
                    .map(school -> new AllSchoolDataDTO(
                            school.getId(),
                            school.getName(),
                            school.getAddress(),
                            school.getDistrict(),
                            school.getType()
                    ))
                    .collect(Collectors.toList());

            SchoolPageResponse response = new SchoolPageResponse(
                    dtoList,
                    schools.getNumber(),
                    schools.getTotalPages(),
                    schools.getTotalElements(),
                    schools.hasNext());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
//save school images in local com. path ="uploads/school-images/"
    private void saveImages(School school, List<MultipartFile> images) throws IOException {
        Path uploadPath = Paths.get(IMAGE_UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        for (MultipartFile image : images) {
            String fileName = UUID.randomUUID() + "_" + image.getOriginalFilename();
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(image.getInputStream(), filePath);

            SchoolImage schoolImage = new SchoolImage();
            schoolImage.setSchool(school);
            schoolImage.setImagePath(filePath.toString());
            schoolImage.setCreatedAt(Timestamp.from(Instant.now()));

            schoolImageReposotory.save(schoolImage);
        }
    }
}
