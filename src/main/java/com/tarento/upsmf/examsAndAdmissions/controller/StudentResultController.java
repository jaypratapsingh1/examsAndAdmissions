package com.tarento.upsmf.examsAndAdmissions.controller;

import com.tarento.upsmf.examsAndAdmissions.enums.RetotallingStatus;
import com.tarento.upsmf.examsAndAdmissions.model.RetotallingRequest;
import com.tarento.upsmf.examsAndAdmissions.model.Student;
import com.tarento.upsmf.examsAndAdmissions.model.Exam;
import com.tarento.upsmf.examsAndAdmissions.model.StudentResult;
import com.tarento.upsmf.examsAndAdmissions.model.dao.Payment;
import com.tarento.upsmf.examsAndAdmissions.service.RetotallingService;
import com.tarento.upsmf.examsAndAdmissions.service.StudentResultService;
import com.tarento.upsmf.examsAndAdmissions.service.StudentService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/v1/studentResults")
@Slf4j
public class StudentResultController {

    @Autowired
    private StudentResultService studentResultService;
    @Autowired
    private RetotallingService retotallingService;
    @Autowired
    private StudentService studentService;

    @PostMapping("/upload/internal")
    public ResponseEntity<String> uploadInternalMarks(@RequestParam("file") MultipartFile file) {
        try {
            studentResultService.importInternalMarksFromExcel(file);
            return ResponseEntity.ok("Internal marks uploaded successfully!");
        } catch (IOException e) {
            log.error("Failed to upload the internal marks", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to upload the internal marks");
        }
    }

    @PostMapping("/upload/external")
    public ResponseEntity<String> uploadExternalMarks(@RequestParam("file") MultipartFile file) {
        try {
            studentResultService.importExternalMarksFromExcel(file);
            return ResponseEntity.ok("External marks uploaded successfully!");
        } catch (IOException e) {
            log.error("Failed to upload the external marks", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to upload the external marks");
        }
    }

    @PostMapping("/publish")
    public ResponseEntity<String> publishExamResults(
            @RequestParam(required = false) Long examCycleId,
            @RequestParam(required = false) Long courseId) {

        try {
            if(courseId != null && examCycleId != null) {
                studentResultService.publishResultsForCourseWithinCycle(courseId, examCycleId);
            }
            else {
                return ResponseEntity.badRequest().body("Please provide a valid parameter to publish results.");
            }

            return ResponseEntity.ok("Results published successfully.");
        } catch (Exception e) {
            log.error("Error while publishing results", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to publish results.");
        }
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception e) {
        log.error("Unexpected error", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("An unexpected error occurred.");
    }

    @GetMapping("/results")
    public ResponseEntity<StudentResult> viewResults(@RequestParam String enrolmentNumber, @RequestParam String dateOfBirth) {
        try {
            LocalDate dob = LocalDate.parse(dateOfBirth);
            StudentResult result = studentResultService.findByEnrollmentNumberAndDateOfBirth(enrolmentNumber, dob);
            if (result == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error retrieving results for enrollment number: " + enrolmentNumber, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PostMapping("/requestRetotalling")
    public ResponseEntity<?> requestRetotalling(@RequestBody RetotallingRequest request) {
        try {
            RetotallingRequest result = retotallingService.requestRetotalling(request);
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to process re-totalling request.");
        }
    }

    @GetMapping("/retotallingRequests")
    public ResponseEntity<List<RetotallingRequest>> viewAllRetotallingRequests() {
        try {
            List<RetotallingRequest> requests = retotallingService.getAllPendingRequests();
            return ResponseEntity.ok(requests);
        } catch (Exception e) {
            log.error("Error retrieving re-totalling requests", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PutMapping("/update")
    public ResponseEntity<String> updateResultsAfterRetotalling(@RequestBody StudentResult updatedResult) {
        try {
            studentResultService.updateResultAfterRetotalling(updatedResult);
            return ResponseEntity.ok("Results updated successfully after re-totalling.");
        } catch (Exception e) {
            log.error("Error updating results after re-totalling", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to update results.");
        }
    }
}
