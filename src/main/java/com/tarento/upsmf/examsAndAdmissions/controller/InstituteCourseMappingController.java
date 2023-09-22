package com.tarento.upsmf.examsAndAdmissions.controller;

import com.tarento.upsmf.examsAndAdmissions.model.ResponseDto;
import com.tarento.upsmf.examsAndAdmissions.model.dto.InstituteCourseMappingDTO;
import com.tarento.upsmf.examsAndAdmissions.service.InstituteCourseMappingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/exam/v1/instituteCourseMap")
public class InstituteCourseMappingController {

    @Autowired
    private InstituteCourseMappingService instituteCourseMappingService;

    @PostMapping("/create")
    public ResponseEntity<ResponseDto> createMapping(@RequestBody InstituteCourseMappingDTO instituteCourseMappingDTO) {
        ResponseDto response = instituteCourseMappingService.create(instituteCourseMappingDTO);
        return new ResponseEntity<>(response, response.getResponseCode());
    }

    @GetMapping("/all")
    public ResponseEntity<ResponseDto> getAllMapping() {
        ResponseDto response = instituteCourseMappingService.getAllMapping();
        return new ResponseEntity<>(response, response.getResponseCode());
    }

    @GetMapping("/list")
    public ResponseEntity<ResponseDto> getAllMappingByFilter(@RequestParam Long instituteId) {
        ResponseDto response = instituteCourseMappingService.getAllMappingByFilter(instituteId);
        return new ResponseEntity<>(response, response.getResponseCode());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseDto> getInstituteCourseMappingById(@PathVariable Long id) {
        ResponseDto response = instituteCourseMappingService.getInstituteCourseMappingById(id);
        return new ResponseEntity<>(response, response.getResponseCode());
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ResponseDto> delete(@PathVariable Long id) {
        ResponseDto response = instituteCourseMappingService.deleteInstituteCourseMapping(id);
        return new ResponseEntity<>(response, response.getResponseCode());
    }
}
