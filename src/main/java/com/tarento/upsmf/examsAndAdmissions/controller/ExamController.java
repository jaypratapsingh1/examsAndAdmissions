package com.tarento.upsmf.examsAndAdmissions.controller;

import com.tarento.upsmf.examsAndAdmissions.model.Exam;
import com.tarento.upsmf.examsAndAdmissions.model.ResponseDto;
import com.tarento.upsmf.examsAndAdmissions.service.ExamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/exam")
public class ExamController {

    @Autowired
    private ExamService examService;

    @PostMapping("/create")
    public ResponseEntity<?> createExam(@RequestBody Exam exam) {
        ResponseDto response = examService.createExam(exam);
        return new ResponseEntity<>(response, response.getResponseCode());
    }
    @GetMapping("/list")
    public ResponseEntity<?> getAllExams() {
        ResponseDto response = examService.getAllExams();
        return new ResponseEntity<>(response, response.getResponseCode());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getExamById(@PathVariable Long id) {
        ResponseDto response = examService.getExamById(id);
        return new ResponseEntity<>(response, response.getResponseCode());
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteExam(@PathVariable Long id) {
        ResponseDto response = examService.deleteExam(id);
        return new ResponseEntity<>(response, response.getResponseCode());
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateExam(@PathVariable Long id, @RequestBody Exam exam) {
        ResponseDto response = examService.updateExam(id, exam);
        return new ResponseEntity<>(response, response.getResponseCode());
    }

    @PutMapping("/{id}/restore")
    public ResponseEntity<?> restoreExam(@PathVariable Long id) {
        ResponseDto response = examService.restoreExam(id);
        return new ResponseEntity<>(response, response.getResponseCode());
    }
/*    @PostMapping("/admin/publishResults/{examId}")
    public ResponseEntity<String> publishExamResults(@PathVariable Long examId) {
        try {
            examService.publishExamResults(examId);
            return ResponseEntity.ok("Exam results published successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error publishing results: " + e.getMessage());
        }
    }*/
}
