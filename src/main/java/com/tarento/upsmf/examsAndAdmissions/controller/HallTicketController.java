package com.tarento.upsmf.examsAndAdmissions.controller;

import com.tarento.upsmf.examsAndAdmissions.model.dto.DataCorrectionRequest;
import com.tarento.upsmf.examsAndAdmissions.model.dto.DataCorrectionRequestDTO;
import com.tarento.upsmf.examsAndAdmissions.service.HallTicketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
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
        byte[] data = hallTicketService.getHallTicket(examRegistrationNumber, dateOfBirth).getBody();
        ByteArrayResource resource = new ByteArrayResource(data);
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_OCTET_STREAM).body(resource);
    }

    @PostMapping("/dataCorrection/request")
    public ResponseEntity<String> requestDataCorrection(@RequestBody DataCorrectionRequestDTO request) {
        hallTicketService.requestHallTicketDataCorrection(request.getStudentId(), request.getCorrectionDetails());
        return ResponseEntity.ok("Data correction requested");
    }

    @GetMapping("/dataCorrection/requests")
    public ResponseEntity<List<DataCorrectionRequest>> viewDataCorrectionRequests() {
        return ResponseEntity.ok(hallTicketService.getAllDataCorrectionRequests());
    }

    @PostMapping("/dataCorrection/{requestId}/approve")
    public ResponseEntity<String> approveDataCorrection(@PathVariable Long requestId) {
        hallTicketService.approveDataCorrection(requestId);
        return ResponseEntity.ok("Request approved");
    }

    @PostMapping("/dataCorrection/{requestId}/reject")
    public ResponseEntity<String> rejectDataCorrection(
            @PathVariable Long requestId,
            @RequestParam String rejectionReason) {
        hallTicketService.rejectDataCorrection(requestId, rejectionReason);
        return ResponseEntity.ok("Request rejected");
    }
}
