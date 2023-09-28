package com.tarento.upsmf.examsAndAdmissions.controller;

import com.tarento.upsmf.examsAndAdmissions.model.DispatchTracker;
import com.tarento.upsmf.examsAndAdmissions.model.Institute;
import com.tarento.upsmf.examsAndAdmissions.model.dto.ApprovalRejectionDTO;
import com.tarento.upsmf.examsAndAdmissions.service.DispatchTrackerService;
import com.tarento.upsmf.examsAndAdmissions.service.InstituteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/institutes")
public class InstituteController {

    @Autowired
    private DispatchTrackerService dispatchTrackerService;
    private final InstituteService instituteService;

    @Autowired
    public InstituteController(InstituteService instituteService) {
        this.instituteService = instituteService;
    }

    @PostMapping("/create")
    public Institute createInstitute(@RequestBody Institute institute) {
        return instituteService.createInstitute(institute);
    }

    @PutMapping("/{id}/update")
    public Institute updateInstitute(@PathVariable Long id, @RequestBody Institute updatedInstitute) {
        return instituteService.updateInstitute(id, updatedInstitute);
    }

    @PostMapping("/verify")
    public void updateVerificationStatus(@RequestBody ApprovalRejectionDTO dto) {
        instituteService.updateVerificationStatus(dto);
    }

    @PutMapping("/{id}/mark-not-allowed")
    public void markNotAllowedForExamCentre(@PathVariable Long id) {
        instituteService.markNotAllowedForExamCentre(id);
    }

    @GetMapping("/{id}")
    public Institute getInstituteById(@PathVariable Long id) {
        return instituteService.getInstituteById(id);
    }

    @PostMapping("/dispatchUpload")
    public ResponseEntity<String> uploadDispatchProof(
            @RequestParam Long examCycleId,
            @RequestParam Long examId,
            @RequestParam MultipartFile dispatchProofFile,
            @RequestParam LocalDate dispatchDate) {
        try {
            dispatchTrackerService.uploadDispatchProof(examCycleId, examId, dispatchProofFile, dispatchDate);
            return ResponseEntity.ok("Dispatch proof uploaded successfully.");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error uploading dispatch proof.");
        }
    }

    @GetMapping("/dispatchList")
    public ResponseEntity<List<DispatchTracker>> getDispatchList(
            @RequestParam Long examCycleId,
            @RequestParam Long examId) {
        List<DispatchTracker> dispatchList = dispatchTrackerService.getDispatchList(examCycleId, examId);
        return ResponseEntity.ok(dispatchList);
    }
}
