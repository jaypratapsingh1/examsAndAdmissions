package com.tarento.upsmf.examsAndAdmissions.controller;

import com.tarento.upsmf.examsAndAdmissions.model.ResponseDto;
import com.tarento.upsmf.examsAndAdmissions.model.Subject;
import com.tarento.upsmf.examsAndAdmissions.service.SubjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/subject")
public class SubjectController {

    @Autowired
    private SubjectService subjectService;

    @PostMapping("/create")
    public ResponseEntity<ResponseDto> createSubject(@RequestBody Subject subject) {
        ResponseDto response = subjectService.createSubject(subject);
        return new ResponseEntity<>(response, response.getResponseCode());
    }

    @GetMapping("/list")
    public ResponseEntity<ResponseDto> getAllSubjects() {
        ResponseDto response = subjectService.getAllSubjects();
        return new ResponseEntity<>(response, response.getResponseCode());
    }

    @GetMapping("/list/{id}")
    public ResponseEntity<ResponseDto> getSubjectById(@PathVariable Long id) {
        ResponseDto response = subjectService.getSubjectById(id);
        return new ResponseEntity<>(response, response.getResponseCode());
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ResponseDto> deleteSubjectById(@PathVariable Long id) {
        ResponseDto response = subjectService.deleteSubjectById(id);
        return new ResponseEntity<>(response, response.getResponseCode());
    }
}
