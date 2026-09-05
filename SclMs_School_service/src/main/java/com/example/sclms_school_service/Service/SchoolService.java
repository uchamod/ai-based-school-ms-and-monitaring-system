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
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
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
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SchoolService {

    private final SchoolReposotory schoolReposotory;
    private final SchoolImageReposotory schoolImageReposotory;
    private final Fegine fegineClient;
    private static final String IMAGE_UPLOAD_DIR = "uploads/school-images/";

    private final cacheIndexService cache_index_service;
    /**
     * Complete school profile with caching
     */

    @Transactional
    public ResponseEntity<String>  completeSchool(School school, List<MultipartFile> images,UUID schoolId) {
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
            cache_index_service.afterCommit(() ->
                    cache_index_service.evictCacheNames("school-list", "school-search"));

            return ResponseEntity.status(HttpStatus.CREATED).body("School profile completed successfully");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to complete school profile: " + e.getMessage());
        }
    }

    /**
     * Get all schools with pagination - Cached.
     * Returns the plain payload (or null) so that Redis can serialize/deserialize it correctly.
     * The controller is responsible for wrapping it into a ResponseEntity.
     */
    @Cacheable(value = "school-list", key = "#page", unless = "#result == null")
    public SchoolPageResponse getAllSchool(int page) {
        try {
            Pageable pageable = PageRequest.of(page, 15);
            Page<School> schoolPage = schoolReposotory.findAll(pageable);

            if (schoolPage.isEmpty())
                return null;

            List<AllSchoolDataDTO> dtoList = schoolPage.getContent().stream()
                    .map(school -> new AllSchoolDataDTO(
                            school.getId(),
                            school.getName(),
                            school.getAddress(),
                            school.getDistrict(),
                            school.getType()
                    ))
                    .collect(Collectors.toList());
            // Reverse-index tracking: this body only runs on cache MISS.
            // Registers this page as "references school X" for every school on the page,
            // so updateSchool/deleteSchool can evict this page surgically.
            List<UUID> pageSchoolIds = schoolPage.getContent().stream()
                    .map(School::getSchoolId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            cache_index_service.trackForSchools(pageSchoolIds, "school-list::" + page);
            //"school-list::" + page
            return new SchoolPageResponse(
                    dtoList,
                    schoolPage.getNumber(),
                    schoolPage.getTotalPages(),
                    schoolPage.getTotalElements(),
                    schoolPage.hasNext());

        } catch (Exception e) {
            System.out.println("Error fetching schools: " + e.getMessage());
            return null;
        }
    }
    /**
     * Get school by ID - Cached.
     * Returns the plain School (or null); the controller wraps it into a ResponseEntity.
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "schools", key = "#id", unless = "#result == null")
    public School getSchoolById(UUID id) {
        try {
            School school = schoolReposotory.findBySchoolId(id);
            if (school == null) return null;

            // Replace Hibernate's PersistentBag with a plain ArrayList so that
            // Redis (Jackson default typing) does not try to serialize
            // org.hibernate.collection.spi.PersistentBag as the collection @class.
            if (school.getImages() != null) {
                school.setImages(new java.util.ArrayList<>(school.getImages()));
            }
            cache_index_service.trackForSchool(id, "schools::" + id);
            return school;
        } catch (Exception e) {
            System.out.println("Error fetching school by id: " + e.getMessage());
            return null;
        }
    }
    /**
     * Update school - Evicts all related caches
     */
    @Transactional
    public ResponseEntity<String> updateSchool(School school, List<MultipartFile> images,UUID schoolId) {
        try {
            if(!fegineClient.getUserById(schoolId).getBody())
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("School does not exist.Register the school first");
            School existingSchool = schoolReposotory.findBySchoolId(schoolId);
            if (existingSchool == null)
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("School not found");

            // Update fields
            updateSchoolFields(existingSchool, school);
            School updatedSchool = schoolReposotory.save(existingSchool);

            if (images != null && !images.isEmpty()) {
                saveImages(updatedSchool, images);
            }
            cache_index_service.afterCommit(() ->
                    cache_index_service.invalidateSchoolEverywhere(schoolId));

            return ResponseEntity.status(HttpStatus.OK).body("School updated successfully");
        } catch (Exception e) {
            System.out.println("Failed to update school: " +e.getMessage());
            return ResponseEntity.internalServerError().body("Failed to update school: " + e.getMessage());
        }
    }

    /**
     * Delete school - Evicts all related caches
     */
    @Transactional
    public ResponseEntity<String> deleteSchool(UUID id) {
        try {
            if (!schoolReposotory.existsById(id))
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("School not found");

            schoolReposotory.deleteById(id);
            cache_index_service.afterCommit(() ->
                    cache_index_service.invalidateSchoolEverywhere(id));


            return ResponseEntity.ok("School deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to delete school: " + e.getMessage());
        }
    }
    /**
     * Filter schools with caching
     */
    @Cacheable(
            value = "school-search",
            key = "#filterSchoolDTO.toString() + '-' + #page",
            unless = "#result == null"
    )
    public SchoolPageResponse filterSchool(FilterSchoolDTO filterSchoolDTO,int page) {
        try {
            Pageable pageable = PageRequest.of(page, 15);

            Specification<School> spec = SchoolSpecification.filterBy(filterSchoolDTO);
            Page<School> schools = schoolReposotory.findAll(spec,pageable);

            if (schools.isEmpty())
                return null;

            List<AllSchoolDataDTO> dtoList = schools.getContent().stream()
                    .map(school -> new AllSchoolDataDTO(
                            school.getId(),
                            school.getName(),
                            school.getAddress(),
                            school.getDistrict(),
                            school.getType()
                    ))
                    .collect(Collectors.toList());
            List<UUID> pageSchoolIds = schools.getContent().stream()
                    .map(School::getSchoolId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            cache_index_service.trackForSchools(pageSchoolIds,"school-search::"+filterSchoolDTO.toString() + '-' + page);
            return new SchoolPageResponse(
                    dtoList,
                    schools.getNumber(),
                    schools.getTotalPages(),
                    schools.getTotalElements(),
                    schools.hasNext());
        } catch (Exception e) {
            System.out.println("Error filtering schools: " + e.getMessage());
            return null;
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
    /**
     * Helper method to update school fields
     */
    private void updateSchoolFields(School existing, School updates) {
        if (updates.getName() != null) existing.setName(updates.getName());
        if (updates.getAddress() != null) existing.setAddress(updates.getAddress());
        if (updates.getProvince() != null) existing.setProvince(updates.getProvince());
        if (updates.getDistrict() != null) existing.setDistrict(updates.getDistrict());
        if (updates.getDiscription() != null) existing.setDiscription(updates.getDiscription());
        if (updates.getType() != null) existing.setType(updates.getType());
        if (updates.getPrincipal() != null) existing.setPrincipal(updates.getPrincipal());
        if (updates.getStCount() != 0) existing.setStCount(updates.getStCount());
        if (updates.getTechCount() != 0) existing.setTechCount(updates.getTechCount());
        if (updates.getLabCount() != 0) existing.setLabCount(updates.getLabCount());
        if (updates.getBuildingCount() != 0) existing.setBuildingCount(updates.getBuildingCount());
        if (updates.getComCount() != 0) existing.setComCount(updates.getComCount());
        if (updates.getIsSportSchool() != null) existing.setIsSportSchool(updates.getIsSportSchool());
        if (updates.getIsPrimarySchool() != null) existing.setIsPrimarySchool(updates.getIsPrimarySchool());
        if (updates.getIsPoshkaSchool() != null) existing.setIsPoshkaSchool(updates.getIsPoshkaSchool());
        if (updates.getLat() != null) existing.setLat(updates.getLat());
        if (updates.getLng() != null) existing.setLng(updates.getLng());
    }
    /**
     * Manual cache eviction for specific use cases
     */
    @Caching(
            evict = {
                    @CacheEvict(value = "schools", key = "#id"),
                    @CacheEvict(value = "school-list", allEntries = true),
                    @CacheEvict(value = "school-search", allEntries = true)
            }
    )
    public void evictSchoolCache(UUID id) {
        log.info("Evicting cache for school: {}", id);
    }

    /**
     * Evict all school caches
     */
    @Caching(
            evict = {
                    @CacheEvict(value = "schools", allEntries = true),
                    @CacheEvict(value = "school-list", allEntries = true),
                    @CacheEvict(value = "school-search", allEntries = true),
                    @CacheEvict(value = "school-index", allEntries = true),
                    @CacheEvict(value = "jwt:token", allEntries = true)
            }
    )
    public void evictAllSchoolCaches() {
        log.info("Evicting all school caches");
    }
}
