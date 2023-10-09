package com.tarento.upsmf.examsAndAdmissions.controller;

import com.tarento.upsmf.examsAndAdmissions.model.Student;
import com.tarento.upsmf.examsAndAdmissions.model.ResponseDto;
import com.tarento.upsmf.examsAndAdmissions.enums.VerificationStatus;
import com.tarento.upsmf.examsAndAdmissions.model.dto.StudentDto;
import com.tarento.upsmf.examsAndAdmissions.service.StudentService;
import com.tarento.upsmf.examsAndAdmissions.util.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/students")
@Slf4j
public class StudentController {
    @Autowired
    private StudentService studentService;

    @PostMapping("/add")
    public ResponseEntity<ResponseDto> addStudent(@ModelAttribute @Valid StudentDto studentDto) {
        ResponseDto response = studentService.enrollStudent(studentDto);
        if (HttpStatus.OK.equals(response.getResponseCode())) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(response.getResponseCode()).body(response);
        }
    }

    @GetMapping("/find")
    public ResponseEntity<ResponseDto> getFilteredStudents(
            @RequestParam(required = false) Long instituteId,
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) String academicYear,
            @RequestParam(required = false) VerificationStatus verificationStatus) {

        ResponseDto response = studentService.getFilteredStudents(instituteId, courseId, academicYear, verificationStatus);
        return ResponseEntity.status(response.getResponseCode()).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseDto> getStudentById(@PathVariable Long id) {
        ResponseDto response = studentService.getStudentById(id);
        return ResponseEntity.status(response.getResponseCode()).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Student> updateStudent(@PathVariable Long id,@ModelAttribute @Valid StudentDto studentDto) {
        try {
            Student updatedStudent = studentService.updateStudent(id, studentDto);
            return ResponseEntity.ok(updatedStudent);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    @PutMapping("/closePendingFor14Days")
    public ResponseEntity<?> updateStudentStatusToClosed() {
        try {
            List<Student> updatedStudents = studentService.updateStudentStatusToClosed();
            return ResponseEntity.ok(updatedStudents);
        } catch (Exception e) {
            log.error("Error updating student status based on days", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while updating student status.");
        }
    }

    @GetMapping("/pendingFor21Days")
    public ResponseEntity<?> getStudentsPendingFor21Days(@RequestParam(required = false) Long courseId,
                                                         @RequestParam(required = false) String academicYear) {
        try {
            List<Student> students = studentService.getStudentsPendingForMoreThan21Days(courseId, academicYear);
            return ResponseEntity.ok(students);
        } catch (Exception e) {
            log.error("Error fetching students pending for 21 days", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while fetching students.");
        }
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteStudent(@PathVariable Long id) {
        ResponseDto response = studentService.deleteStudent(id);
        if (response.getResponseCode() == HttpStatus.OK) {
            return ResponseEntity.ok(response.get(Constants.RESPONSE).toString());
        } else {
            return ResponseEntity.status(response.getResponseCode()).body(response.get(Constants.RESPONSE).toString());
        }
    }

    @PutMapping("/{studentId}/verify")
    public ResponseEntity<Student> verifyStudent(@PathVariable Long studentId, @RequestParam("status") VerificationStatus status, @RequestParam("remarks") String remarks) {
        Student updatedStudent = studentService.verifyStudent(studentId, status, remarks);
        return ResponseEntity.ok(updatedStudent);
    }
    @GetMapping("/pendingVerification")
    public ResponseEntity<List<Student>> getStudentsPendingVerification() {
        List<Student> students = studentService.findByVerificationStatus(VerificationStatus.PENDING);
        return ResponseEntity.ok(students);
    }


}