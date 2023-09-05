package com.tarento.upsmf.examsAndAdmissions.controller;

import com.tarento.upsmf.examsAndAdmissions.service.InstituteService;
import com.tarento.upsmf.examsAndAdmissions.model.dto.ApprovalRejectionDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityNotFoundException;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {
    @Autowired
    private InstituteService instituteService;

    @PutMapping("/cctv/approve-reject")
    public ResponseEntity<String> approveOrReject(@RequestBody ApprovalRejectionDTO dto) {
        try {
            instituteService.updateVerificationStatus(dto);
            return ResponseEntity.ok("Approval/Rejection successful");
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Institute not found");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred");
        }
    }

    @PutMapping("/markNotAllowed/{instituteId}")
    public ResponseEntity<String> markNotAllowedForExamCentre(@PathVariable Long instituteId) {
        try {
            instituteService.markNotAllowedForExamCentre(instituteId);
            return ResponseEntity.ok("Institute marked as not allowed for exam centre");
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Institute not found");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred");
        }
    }
}
