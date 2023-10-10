package com.tarento.upsmf.examsAndAdmissions.controller;

import com.tarento.upsmf.examsAndAdmissions.model.ResponseDto;
import com.tarento.upsmf.examsAndAdmissions.model.dto.StudentExamRegistrationDTO;
import com.tarento.upsmf.examsAndAdmissions.service.StudentExamRegistrationService;
import com.tarento.upsmf.examsAndAdmissions.util.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/studentExamRegistration")
public class StudentExamRegistrationController {

    @Autowired
    private StudentExamRegistrationService registrationService;

    @PostMapping("/register")
    public ResponseEntity<ResponseDto> registerStudentForExam(@RequestBody List<StudentExamRegistrationDTO> registrationDetails, @RequestAttribute(Constants.Parameters.USER_ID) String userId) {
        ResponseDto response = registrationService.registerStudentsForExams(registrationDetails, userId);
        return ResponseEntity.status(response.getResponseCode()).body(response);
    }

    @GetMapping("/getAll")
    public ResponseEntity<ResponseDto> getAllRegistrations(Pageable pageable) {
        ResponseDto response = registrationService.getAllRegistrations(pageable);
        return ResponseEntity.status(response.getResponseCode()).body(response);
    }

    @GetMapping("/{examCycleId}")
    public ResponseEntity<ResponseDto> getAllRegistrationsByExamCycle(@PathVariable Long examCycleId) {
        ResponseDto response = registrationService.getAllRegistrationsByExamCycle(examCycleId);
        return ResponseEntity.status(response.getResponseCode()).body(response);
    }
}
