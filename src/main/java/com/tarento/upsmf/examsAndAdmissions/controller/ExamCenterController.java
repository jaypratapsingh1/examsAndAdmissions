package com.tarento.upsmf.examsAndAdmissions.controller;

import com.tarento.upsmf.examsAndAdmissions.model.ExamCenter;
import com.tarento.upsmf.examsAndAdmissions.model.ResponseDto;
import com.tarento.upsmf.examsAndAdmissions.model.dto.CCTVStatusUpdateDTO;
import com.tarento.upsmf.examsAndAdmissions.model.dto.ExamCenterDTO;
import com.tarento.upsmf.examsAndAdmissions.service.ExamCenterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/admin")
public class ExamCenterController {

    @Autowired
    private ExamCenterService examCenterService;

    @GetMapping("/verifiedExamCenters")
    public ResponseEntity<ResponseDto> getVerifiedExamCenters(@RequestParam String district) {
        return new ResponseEntity<>(examCenterService.getVerifiedExamCentersInDistrict(district), HttpStatus.OK);
    }

    @PutMapping("/assignAlternate/{originalExamCenterId}")
    public ResponseEntity<ResponseDto> assignAlternateExamCenter(@PathVariable Long originalExamCenterId, @RequestParam Long alternateInstituteId) {
        return new ResponseEntity<>(examCenterService.assignAlternateExamCenter(originalExamCenterId, alternateInstituteId), HttpStatus.OK);
    }

    @GetMapping("/examCenters")
    public ResponseEntity<ResponseDto> getExamCentersByStatus(@RequestParam Long examCycleId, @RequestParam Boolean isVerifiedStatus) {
        return new ResponseEntity<>(examCenterService.getExamCentersByStatus(examCycleId, isVerifiedStatus), HttpStatus.OK);
    }

    @PutMapping("/updateCctvStatus/{examCenterId}")
    public ResponseEntity<ResponseDto> updateCCTVStatus(@PathVariable Long examCenterId, @RequestBody CCTVStatusUpdateDTO updateDTO) {
        return new ResponseEntity<>(examCenterService.updateCCTVStatus(examCenterId, updateDTO), HttpStatus.OK);
    }

    @GetMapping("/examCenters/all")
    public ResponseEntity<ResponseDto> getAllExamCenters() {
        return new ResponseEntity<>(examCenterService.getAllExamCenters(), HttpStatus.OK);
    }
    @GetMapping("/byExamCycle/{examCycleId}")
    public ResponseEntity<ResponseDto> getExamCentersByExamCycle(@PathVariable Long examCycleId) {
        return new ResponseEntity<>(examCenterService.getExamCentersByExamCycle(examCycleId), HttpStatus.OK);
    }
}
