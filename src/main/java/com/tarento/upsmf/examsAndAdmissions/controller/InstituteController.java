package com.tarento.upsmf.examsAndAdmissions.controller;

import com.tarento.upsmf.examsAndAdmissions.model.Institute;
import com.tarento.upsmf.examsAndAdmissions.model.dto.ApprovalRejectionDTO;
import com.tarento.upsmf.examsAndAdmissions.service.InstituteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/institutes")
public class InstituteController {

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
}
