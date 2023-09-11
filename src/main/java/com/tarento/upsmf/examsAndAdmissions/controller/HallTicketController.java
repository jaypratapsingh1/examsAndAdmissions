package com.tarento.upsmf.examsAndAdmissions.controller;

import com.tarento.upsmf.examsAndAdmissions.model.dto.DataCorrectionRequest;
import com.tarento.upsmf.examsAndAdmissions.model.dto.DataCorrectionRequestDTO;
import com.tarento.upsmf.examsAndAdmissions.service.HallTicketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/hallticket")
public class HallTicketController {

    @Autowired
    HallTicketService hallTicketService;

    @GetMapping("/download")
    public ResponseEntity<ByteArrayResource> getHallTicket(@RequestParam String examRegistrationNumber, @RequestParam String dateOfBirth) {
        if (examRegistrationNumber == null || dateOfBirth == null || examRegistrationNumber.isEmpty() || dateOfBirth.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }

        byte[] data = hallTicketService.getHallTicket(examRegistrationNumber, dateOfBirth).getBody();

        if (data == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        ByteArrayResource resource = new ByteArrayResource(data);
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_OCTET_STREAM).body(resource);
    }

    @PostMapping("/dataCorrection/request")
    public ResponseEntity<String> requestDataCorrection(@RequestBody DataCorrectionRequestDTO request) {
        if (request == null || request.getStudentId() == null || request.getCorrectionDetails() == null || request.getCorrectionDetails().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid request data");
        }

        hallTicketService.requestHallTicketDataCorrection(request.getStudentId(), request.getCorrectionDetails());
        return ResponseEntity.ok("Data correction requested");
    }

    @GetMapping("/dataCorrection/requests")
    public ResponseEntity<List<DataCorrectionRequest>> viewDataCorrectionRequests() {
        return ResponseEntity.ok(hallTicketService.getAllDataCorrectionRequests());
    }

    @PostMapping("/dataCorrection/{requestId}/approve")
    public ResponseEntity<String> approveDataCorrection(@PathVariable Long requestId) {
        if (requestId == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid request ID");
        }

        hallTicketService.approveDataCorrection(requestId);
        return ResponseEntity.ok("Request approved");
    }

    @PostMapping("/dataCorrection/{requestId}/reject")
    public ResponseEntity<String> rejectDataCorrection(
            @PathVariable Long requestId,
            @RequestParam String rejectionReason) {
        if (requestId == null || rejectionReason == null || rejectionReason.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid input data");
        }

        hallTicketService.rejectDataCorrection(requestId, rejectionReason);
        return ResponseEntity.ok("Request rejected");
    }
}
