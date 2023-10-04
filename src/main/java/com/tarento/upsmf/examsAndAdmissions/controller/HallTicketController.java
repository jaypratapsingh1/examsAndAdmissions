package com.tarento.upsmf.examsAndAdmissions.controller;

import com.tarento.upsmf.examsAndAdmissions.model.ResponseDto;
import com.tarento.upsmf.examsAndAdmissions.model.dto.DataCorrectionRequest;
import com.tarento.upsmf.examsAndAdmissions.service.HallTicketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/hallticket")
public class HallTicketController {

    @Autowired
    HallTicketService hallTicketService;

    @GetMapping("/download")
    public ResponseEntity<ByteArrayResource> getHallTicket(@RequestParam Long id, @RequestParam String dateOfBirth) {
        if (id == null || dateOfBirth == null || dateOfBirth.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }

        byte[] data = hallTicketService.getHallTicket(id, dateOfBirth).getBody();

        if (data == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        ByteArrayResource resource = new ByteArrayResource(data);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=hallticket.pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_PDF)
                .body(resource);
    }

    @PostMapping("/dataCorrection/request")
    public ResponseEntity<String> requestDataCorrection(
            @RequestParam("studentId") Long studentId,
            @RequestParam("correctionDetails") String correctionDetails,
            @RequestParam("proof") MultipartFile proof) {
        try {
            if (studentId == null || correctionDetails == null || correctionDetails.isEmpty() || proof.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid request data");
            }

            hallTicketService.requestHallTicketDataCorrection(studentId, correctionDetails, proof);
            return ResponseEntity.ok("Data correction requested");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error saving proof file");
        } catch (Exception e) {
            // Catch other types of exceptions if necessary
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred processing your request");
        }
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

    @GetMapping("/pendingData")
    public ResponseEntity<ResponseDto> getPendingData(
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) Long examCycleId,
            @RequestParam(required = false) Long instituteId) {

        ResponseDto pendingDataList = hallTicketService.getPendingDataForHallTickets(courseId, examCycleId, instituteId);

        if (pendingDataList != null) {
            return ResponseEntity.ok(pendingDataList);
        } else {
            return ResponseEntity.noContent().build();
        }
    }
    @GetMapping("/downloadProof/{requestId}")
    public ResponseEntity<Resource> downloadProof(@PathVariable Long requestId) {
        try {
            String proofUrl = hallTicketService.getProofUrlByRequestId(requestId);

            RestTemplate restTemplate = new RestTemplate();
            byte[] bytes = restTemplate.getForObject(proofUrl, byte[].class);
            ByteArrayResource resource = new ByteArrayResource(bytes);

            String contentType = "image/jpeg";

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + "proof.jpg" + "\"")
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}