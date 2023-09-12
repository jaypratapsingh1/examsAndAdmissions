package com.tarento.upsmf.examsAndAdmissions.controller;

import com.tarento.upsmf.examsAndAdmissions.model.ExamCenter;
import com.tarento.upsmf.examsAndAdmissions.model.ExamCycle;
import com.tarento.upsmf.examsAndAdmissions.model.dto.CCTVStatusUpdateDTO;
import com.tarento.upsmf.examsAndAdmissions.service.ExamCenterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityNotFoundException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
public class ExamCenterController {
    @Autowired
    private ExamCenterService examCenterService;

    @GetMapping("/verifiedExamCenters")
    public ResponseEntity<List<ExamCenter>> getVerifiedExamCenters(@RequestParam String district) {
        List<ExamCenter> verifiedExamCenters = examCenterService.getVerifiedExamCentersInDistrict(district);
        if (verifiedExamCenters.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        return ResponseEntity.ok(verifiedExamCenters);
    }
    @PutMapping("/assignAlternate/{originalExamCenterId}")
    public ResponseEntity<String> assignAlternateExamCenter(@PathVariable Long originalExamCenterId, @RequestParam Long alternateInstituteId) {
        examCenterService.assignAlternateExamCenter(originalExamCenterId, alternateInstituteId);
        return ResponseEntity.ok("Alternate exam center assigned successfully");
    }
    @GetMapping("/examCenters")
    public ResponseEntity<List<ExamCenter>> getExamCentersByStatus(@RequestParam Long examCycleId, @RequestParam Boolean isVerifiedStatus) {
        ExamCycle examCycle = new ExamCycle();
        examCycle.setId(examCycleId); // Or fetch the full entity if needed
        List<ExamCenter> examCenters = examCenterService.getExamCentersByStatus(examCycle, isVerifiedStatus);
        return ResponseEntity.ok(examCenters);
    }

    @PutMapping("/updateCctvStatus/{examCenterId}")
    public ResponseEntity<String> updateCCTVStatus(
            @PathVariable Long examCenterId,@RequestBody CCTVStatusUpdateDTO updateDTO) {

        try {
            examCenterService.updateCCTVStatus(examCenterId, updateDTO.getStatus(), updateDTO.getIpAddress(), updateDTO.getRemarks());
            return ResponseEntity.ok("Exam Center CCTV status updated successfully.");
        } catch (EntityNotFoundException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
    @GetMapping("/examCenters/all")
    public ResponseEntity<List<ExamCenter>> getAllExamCenters() {
        List<ExamCenter> examCenters = examCenterService.getAllExamCenters();
        return ResponseEntity.ok(examCenters);
    }

    @GetMapping("/examCenters/examCycle/{examCycleId}")
    public ResponseEntity<List<ExamCenter>> getExamCentersByExamCycle(@PathVariable Long examCycleId) {
        ExamCycle examCycle = new ExamCycle();
        examCycle.setId(examCycleId);
        List<ExamCenter> examCenters = examCenterService.getExamCentersByExamCycle(examCycle);
        return ResponseEntity.ok(examCenters);
    }
}
