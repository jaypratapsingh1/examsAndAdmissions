package com.tarento.upsmf.examsAndAdmissions.service;

import com.tarento.upsmf.examsAndAdmissions.model.Institute;
import com.tarento.upsmf.examsAndAdmissions.model.dto.ApprovalRejectionDTO;
import com.tarento.upsmf.examsAndAdmissions.repository.InstituteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.util.Optional;

@Service
public class InstituteService {
    @Autowired
    private InstituteRepository instituteRepository;

    public void updateVerificationStatus(ApprovalRejectionDTO dto) {
        Institute institute = instituteRepository.findById(dto.getInstituteId())
                .orElseThrow(() -> new EntityNotFoundException("Institute not found"));

        if ("approve".equalsIgnoreCase(dto.getAction())) {
            institute.setCctvVerified(true);
        } else if ("reject".equalsIgnoreCase(dto.getAction())) {
            institute.setCctvVerified(false);
        }

        institute.setIpAddress(dto.getIpAddress());
        institute.setRemarks(dto.getRemarks());

        instituteRepository.save(institute);
    }

    public void markNotAllowedForExamCentre(Long instituteId) {
        Institute institute = instituteRepository.findById(instituteId)
                .orElseThrow(() -> new EntityNotFoundException("Institute not found"));

        institute.setAllowedForExamCentre(false);
        instituteRepository.save(institute);
    }
}
