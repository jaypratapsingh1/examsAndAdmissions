package com.tarento.upsmf.examsAndAdmissions.controller;

import com.tarento.upsmf.examsAndAdmissions.model.ResponseDto;
import com.tarento.upsmf.examsAndAdmissions.model.dto.CourseSubjectMappingDTO;
import com.tarento.upsmf.examsAndAdmissions.service.CourseSubjectMappingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/courseSubjectMap")
public class CourseSubjectMappingController {

    @Autowired
    private CourseSubjectMappingService courseSubjectMappingService;

    @PostMapping("/create")
    public ResponseEntity<ResponseDto> createMapping(@RequestBody CourseSubjectMappingDTO courseSubjectMappingDTO) {
        ResponseDto response = courseSubjectMappingService.create(courseSubjectMappingDTO);
        return new ResponseEntity<>(response, response.getResponseCode());
    }

    @GetMapping("/all")
    public ResponseEntity<ResponseDto> getAllMapping() {
        ResponseDto response = courseSubjectMappingService.getAllMapping();
        return new ResponseEntity<>(response, response.getResponseCode());
    }

    @GetMapping("/list")
    public ResponseEntity<ResponseDto> getAllMappingByFilter(@RequestParam Long courseId) {
        ResponseDto response = courseSubjectMappingService.getAllMappingByFilter(courseId);
        return new ResponseEntity<>(response, response.getResponseCode());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseDto> getCourseSubjectMappingById(@PathVariable Long id) {
        ResponseDto response = courseSubjectMappingService.getCourseSubjectMappingById(id);
        return new ResponseEntity<>(response, response.getResponseCode());
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ResponseDto> delete(@PathVariable Long id) {
        ResponseDto response = courseSubjectMappingService.deleteCourseSubjectMapping(id);
        return new ResponseEntity<>(response, response.getResponseCode());
    }
}
