package com.tarento.upsmf.examsAndAdmissions.controller;

import com.tarento.upsmf.examsAndAdmissions.model.Exam;
import com.tarento.upsmf.examsAndAdmissions.model.ExamCycle;
import com.tarento.upsmf.examsAndAdmissions.service.ExamCycleService;
import org.springframework.beans.factory.annotation.Autowired;
import com.tarento.upsmf.examsAndAdmissions.util.Constants;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.tarento.upsmf.examsAndAdmissions.model.dto.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/examCycle")
public class ExamCycleController {

    @Autowired
    private ExamCycleService service;

    @PostMapping("/create")
    public ResponseEntity<?> createExamCycle(@RequestBody ExamCycle examCycle, @RequestAttribute(Constants.Parameters.USER_ID) String userId) {
        try {
            ExamCycle createdExamCycle = service.createExamCycle(examCycle,userId);
            return new ResponseEntity<>(createdExamCycle, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to create ExamCycle.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @GetMapping("/list")
    public ResponseEntity<?> getAllExamCycles() {
        try {
            List<ExamCycle> examCycles = service.getAllExamCycles();
            if (examCycles.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }
            return new ResponseEntity<>(examCycles, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to fetch all ExamCycles.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getExamCycleById(@PathVariable Long id) {
        try {
            ExamCycle examCycle = service.getExamCycleById(id);
            if (examCycle == null) {
                return new ResponseEntity<>("ExamCycle not found with ID: " + id, HttpStatus.NOT_FOUND);
            }
            return new ResponseEntity<>(examCycle, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to fetch ExamCycle with ID: " + id, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateExamCycle(@PathVariable Long id, @RequestBody ExamCycle examCycle, @RequestAttribute(Constants.Parameters.USER_ID) String userId) {
        try {
            ExamCycle updatedExamCycle = service.updateExamCycle(id, examCycle, userId);
            if (updatedExamCycle == null) {
                return new ResponseEntity<>("ExamCycle not found with ID: " + id, HttpStatus.NOT_FOUND);
            }
            return new ResponseEntity<>(updatedExamCycle, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to update ExamCycle with ID: " + id, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteExamCycle(@PathVariable Long id) {
        try {
            service.deleteExamCycle(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to delete ExamCycle with ID: " + id, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    // Restore a soft-deleted exam
    @PutMapping("/{id}/restore")
    public ResponseEntity<?> restoreExamCycle(@PathVariable Long id) {
        try {
            service.restoreExamCycle(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to restore ExamCycle with ID: " + id, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/{id}/addExam")
    public ResponseEntity<?> addExamToCycle(@PathVariable Long id, @RequestBody List<Exam> exams, @RequestAttribute(Constants.Parameters.USER_ID) String userId) {
        try {
            ExamCycle examCycle = service.addExamsToCycle(id, exams, userId);
            if (examCycle == null) {
                return new ResponseEntity<>("ExamCycle not found with ID: " + id, HttpStatus.NOT_FOUND);
            }
            return new ResponseEntity<>(exams, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to add exam to ExamCycle with ID: " + id, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @DeleteMapping("/{id}/removeExam")
    public ResponseEntity<?> removeExamFromCycle(@PathVariable Long id, @RequestBody Exam exam) {
        try {
            ExamCycle examCycle = service.removeExamFromCycle(id, exam);
            if (examCycle == null) {
                return new ResponseEntity<>("ExamCycle not found with ID: " + id, HttpStatus.NOT_FOUND);
            }
            return new ResponseEntity<>(exam, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to remove exam from ExamCycle with ID: " + id, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @PutMapping("/{id}/publish")
    public ResponseEntity<?> publishExamCycle(@PathVariable Long id) {
        try {
            ExamCycle publishedExamCycle = service.publishExamCycle(id);
            if (publishedExamCycle == null) {
                return new ResponseEntity<>("ExamCycle not found with ID: " + id, HttpStatus.NOT_FOUND);
            }
            return new ResponseEntity<>(publishedExamCycle, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to publish ExamCycle with ID: " + id, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @PostMapping("/createExamCycleWithExams")
    public ResponseEntity<?> createExamCycleWithExams(@RequestBody ExamCycleWithExamsDTO examCycleWithExamsDTO, @RequestAttribute(Constants.Parameters.USER_ID) String userId) {
        try {
            // Create the ExamCycle
            ExamCycle createdExamCycle = service.createExamCycle(examCycleWithExamsDTO.getExamCycle(), userId);

            // Add Exams to the ExamCycle
            service.addExamsToCycle(createdExamCycle.getId(), examCycleWithExamsDTO.getExams(), userId);

            return ResponseEntity.ok("ExamCycle with Exams created successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to create ExamCycle with Exams.");
        }
    }
}