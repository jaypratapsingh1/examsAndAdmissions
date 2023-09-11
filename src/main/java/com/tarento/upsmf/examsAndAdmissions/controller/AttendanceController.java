package com.tarento.upsmf.examsAndAdmissions.controller;

import com.tarento.upsmf.examsAndAdmissions.enums.ApprovalStatus;
import com.tarento.upsmf.examsAndAdmissions.model.AttendanceRecord;
import com.tarento.upsmf.examsAndAdmissions.service.AttendanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/attendance")
public class AttendanceController {

    @Autowired
    private AttendanceService attendanceService;

    @PostMapping("/upload")
    public ResponseEntity<String> uploadAttendanceFile(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("Please provide a valid Excel file.");
        }

        try {
            // Parse and save the attendance data from the Excel file
            attendanceService.uploadAttendanceRecords(file);
            return ResponseEntity.ok("File uploaded and processed successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to process the file: " + e.getMessage());
        }
    }

    @GetMapping("/all")
    public List<AttendanceRecord> getAllAttendanceRecords() {
        return attendanceService.getAllAttendanceRecords();
    }

    @PostMapping("/approve/{id}")
    public ResponseEntity<String> approveStudent(@PathVariable Long id) {
        AttendanceRecord record = attendanceService.getRecordById(id);
        if (record == null) {
            return ResponseEntity.badRequest().body("Record not found.");
        }
        record.setApprovalStatus(ApprovalStatus.APPROVED);
        attendanceService.saveRecord(record);
        return ResponseEntity.ok("Student approved successfully.");
    }

    @PostMapping("/reject/{id}")
    public ResponseEntity<String> rejectStudent(@PathVariable Long id, @RequestParam String reason) {
        AttendanceRecord record = attendanceService.getRecordById(id);
        if (record == null) {
            return ResponseEntity.badRequest().body("Record not found.");
        }
        record.setApprovalStatus(ApprovalStatus.REJECTED);
        record.setRejectionReason(reason);
        attendanceService.saveRecord(record);
        return ResponseEntity.ok("Student rejected successfully with reason: " + reason);
    }

}
