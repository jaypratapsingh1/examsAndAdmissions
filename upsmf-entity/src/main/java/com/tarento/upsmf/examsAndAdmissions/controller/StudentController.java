package com.tarento.upsmf.examsAndAdmissions.controller;

import com.tarento.upsmf.examsAndAdmissions.model.Student;
import com.tarento.upsmf.examsAndAdmissions.model.VerificationStatus;
import com.tarento.upsmf.examsAndAdmissions.model.dto.StudentDto;
import com.tarento.upsmf.examsAndAdmissions.service.StudentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/students")
@Slf4j
public class StudentController {
    @Autowired
    private StudentService studentService;

    @PostMapping("/add")
    public ResponseEntity<Student> addStudent(@ModelAttribute @Valid StudentDto studentDto) {
        try {
            Student addedStudent = studentService.enrollStudent(studentDto);
            return ResponseEntity.ok(addedStudent);
        } catch (IOException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/findAll")
    public ResponseEntity<List<Student>> getAllStudents() {
        List<Student> students = studentService.getAllStudents();
        if (students != null && !students.isEmpty()) {
            return ResponseEntity.ok(students);
        } else {
            return ResponseEntity.noContent().build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Student> getStudentById(@PathVariable Long id) {
        Optional<Student> student = studentService.getStudentById(id);
        return student.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
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
    public ResponseEntity<?> getStudentsPendingFor21Days() {
        try {
            List<Student> students = studentService.getStudentsPendingForMoreThan21Days();
            return ResponseEntity.ok(students);
        } catch (Exception e) {
            log.error("Error fetching students pending for 21 days", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while fetching students.");
        }
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStudent(@PathVariable Long id) {
        try {
            studentService.deleteStudent(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{studentId}/verify")
    public ResponseEntity<Student> verifyStudent(@PathVariable Long studentId, @RequestParam("status") VerificationStatus status, @RequestParam("remarks") String remarks, @RequestParam("verificationDate") LocalDate verificationDate) {
        Student updatedStudent = studentService.verifyStudent(studentId, status, remarks, verificationDate);
        return ResponseEntity.ok(updatedStudent);
    }
    @GetMapping("/pendingVerification")
    public ResponseEntity<List<Student>> getStudentsPendingVerification() {
        List<Student> students = studentService.findByVerificationStatus(VerificationStatus.PENDING);
        return ResponseEntity.ok(students);
    }


}