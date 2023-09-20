package com.tarento.upsmf.examsAndAdmissions.controller;

import com.tarento.upsmf.examsAndAdmissions.model.dto.StudentExamRegistrationDTO;
import com.tarento.upsmf.examsAndAdmissions.service.StudentExamRegistrationService;
import com.tarento.upsmf.examsAndAdmissions.util.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/studentExamRegistration")
public class StudentExamRegistrationController {

    @Autowired
    private StudentExamRegistrationService registrationService;

    @PostMapping("/register")
    public ResponseEntity<?> registerStudentForExam(@RequestBody List<StudentExamRegistrationDTO> registrationDetails, @RequestAttribute(Constants.Parameters.USER_ID) String userId) {

        ResponseEntity<?> registration = registrationService.registerStudentsForExams(registrationDetails, userId);
        return new ResponseEntity<>(registration, HttpStatus.CREATED);
    }

    @GetMapping("/getAll")
    public ResponseEntity<Page<StudentExamRegistrationDTO>> getAllRegistrations(Pageable pageable) {
        return registrationService.getAllRegistrations(pageable);
    }
    @GetMapping("/{examCycleId}")
    public ResponseEntity<List<StudentExamRegistrationDTO>> getAllRegistrationsByExamCycle(
            @PathVariable Long examCycleId) {
        List<StudentExamRegistrationDTO> registrationDTOs = registrationService.getAllRegistrationsByExamCycle(examCycleId);
        return ResponseEntity.ok(registrationDTOs);
    }
}