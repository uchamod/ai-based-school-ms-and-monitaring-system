package com.example.sclms_school_service.Controller;

import com.example.sclms_school_service.DTO.AllSchoolDataDTO;
import com.example.sclms_school_service.DTO.FilterSchoolDTO;
import com.example.sclms_school_service.DTO.SchoolPageResponse;
import com.example.sclms_school_service.Model.School;
import com.example.sclms_school_service.Service.SchoolService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@CrossOrigin
@RequiredArgsConstructor
@RequestMapping("/api/school")
public class schoolController {

    private final SchoolService schoolService;

    //complete school profile
    @PostMapping(value = "/profile",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> completeSchool(@RequestPart("school") School request,
                                               @RequestPart("images") List<MultipartFile> images,
                                                 @RequestHeader("X-User-Id") String schoolId){

        return schoolService.completeSchool(request,images,UUID.fromString(schoolId));
    }
    //update profile
    @PutMapping(value = "/updateprofile",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> updateSchool(@RequestPart("school") School request,
                                               @RequestPart("images") List<MultipartFile> images,
                                               @RequestHeader("X-User-Id") String schoolId){

        return schoolService.updateSchool(request,images,UUID.fromString(schoolId));
    }
    //delete profile
    @DeleteMapping("/delete/{schoolId}")
    public ResponseEntity<String> deleteSchool(@PathVariable UUID schoolId){
        return schoolService.deleteSchool(schoolId);
    }
    //get all schools
    @GetMapping("/getAll")
    public ResponseEntity<SchoolPageResponse> getAllSchool(@RequestParam(defaultValue = "0") int page){
        return schoolService.getAllSchool(page);

    }
    //get school by id
    @GetMapping("/getSchoolById/{schoolId}")
    public ResponseEntity<School> getSchoolById(@PathVariable UUID schoolId){
        return schoolService.getSchoolById(schoolId);

    }
    //get schools by given arguments
    @PostMapping("/filterschool")
    public ResponseEntity<SchoolPageResponse> filterSchool(@RequestParam(defaultValue = "0") int page,@RequestBody FilterSchoolDTO filterSchoolDTO){
        return schoolService.filterSchool(filterSchoolDTO,page);
    }

}
