package com.tarento.upsmf.examsAndAdmissions.controller;

import com.tarento.upsmf.examsAndAdmissions.model.ResponseDto;
import com.tarento.upsmf.examsAndAdmissions.service.AttendanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/attendance")
public class AttendanceController {

    @Autowired
    private AttendanceService attendanceService;

    @PostMapping("/upload")
    public ResponseEntity<ResponseDto> uploadAttendanceFile(@RequestParam("file") MultipartFile file) throws IOException {
        return new ResponseEntity<>(attendanceService.uploadAttendanceRecords(file), HttpStatus.OK);
    }

    @GetMapping("/all")
    public ResponseEntity<ResponseDto> getAllAttendanceRecords() {
        return new ResponseEntity<>(attendanceService.getAllAttendanceRecords(), HttpStatus.OK);
    }
    @PostMapping("/approve/{id}")
    public ResponseEntity<ResponseDto> approveStudent(@PathVariable Long id) {
        ResponseDto response = attendanceService.approveStudent(id);

        return new ResponseEntity<>(response, response.getResponseCode());
    }

    @PostMapping("/reject/{id}")
    public ResponseEntity<ResponseDto> rejectStudent(@PathVariable Long id, @RequestParam String reason) {
        ResponseDto response = attendanceService.rejectStudent(id, reason);

        return new ResponseEntity<>(response, response.getResponseCode());
    }
    @PostMapping("/bulkUpload")
    public ResponseEntity<ResponseDto> processBulkAttendanceUpload(@RequestParam("file") MultipartFile file, @RequestParam("fileType") String fileType) {
        ResponseDto response = attendanceService.processBulkAttendanceUpload(file, fileType);
        return new ResponseEntity<>(response, response.getResponseCode());
    }
}
