package com.tarento.upsmf.examsAndAdmissions.controller;

import com.tarento.upsmf.examsAndAdmissions.enums.ApprovalStatus;
import com.tarento.upsmf.examsAndAdmissions.model.AttendanceRecord;
import com.tarento.upsmf.examsAndAdmissions.repository.AttendanceRepository;
import com.tarento.upsmf.examsAndAdmissions.service.AttendanceService;
import com.tarento.upsmf.examsAndAdmissions.service.DataImporterService;
import com.tarento.upsmf.examsAndAdmissions.util.Constants;
import org.codehaus.jettison.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/attendance")
public class AttendanceController {

    @Autowired
    private AttendanceService attendanceService;

    @Autowired
    private DataImporterService dataImporterService;
    @Autowired
    AttendanceRepository repository;

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

    @PostMapping("/bulkUpload")
    public ResponseEntity<Map<String, Object>> processBulkAttendanceUpload(@RequestParam("file") MultipartFile file, @RequestParam("fileType") String fileType) {
        Map<String, Object> response = new HashMap<>();
        JSONArray jsonArray = null;
        Class<AttendanceRecord> dtoClass = AttendanceRecord.class;
        try {
            switch (fileType.toLowerCase()) {
                case Constants.CSV:
                    jsonArray = dataImporterService.csvToJson(file);
                    break;
                case Constants.EXCEL:
                    jsonArray = dataImporterService.excelToJson(file);
                    break;
                default:
                    // Handle unsupported file type
                    response.put("error", "Unsupported file type");
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            List<AttendanceRecord> dtoList = dataImporterService.convertJsonToDtoList(jsonArray, AttendanceRecord.class);
            Boolean success = dataImporterService.saveDtoListToPostgres(dtoList, repository);

            if (success) {
                response.put("message", "File processed successfully.");
                return ResponseEntity.ok(response);
            } else {
                response.put("error", "File processing failed.");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
        } catch (Exception e) {
            response.put("error", "An error occurred while processing the file: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

}
