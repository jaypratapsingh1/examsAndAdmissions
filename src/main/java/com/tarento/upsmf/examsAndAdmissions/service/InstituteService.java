package com.tarento.upsmf.examsAndAdmissions.service;

import com.tarento.upsmf.examsAndAdmissions.exception.DuplicateInstituteException;
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
        institute.handleAction(dto.getAction(), dto.getIpAddress(), dto.getRemarks());
        instituteRepository.save(institute);
    }

    public void markNotAllowedForExamCentre(Long instituteId) {
        Institute institute = instituteRepository.findById(instituteId)
                .orElseThrow(() -> new EntityNotFoundException("Institute not found"));

        institute.setAllowedForExamCentre(false);
        instituteRepository.save(institute);
    }

    public Institute getInstituteById(Long instituteId) {
        Optional<Institute> optionalInstitute = instituteRepository.findById(instituteId);
        return optionalInstitute.orElse(null);
    }

    public Institute createInstitute(Institute institute) {
        // Validate for existing institute
        Institute existingInstitute = instituteRepository.findByInstituteCode(institute.getInstituteCode());

        if (existingInstitute != null) {
            throw new DuplicateInstituteException("An institute with the same code already exists.");
        }
        return instituteRepository.save(institute);
    }

    public Institute updateInstitute(Long id, Institute updatedInstitute) {

        Institute existingInstitute = instituteRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Institute not found with ID: " + id));

        // Update the properties of the existing institute with the provided values
        existingInstitute.setInstituteName(updatedInstitute.getInstituteName());
        existingInstitute.setAddress(updatedInstitute.getAddress());
        existingInstitute.setEmail(updatedInstitute.getEmail());

        return instituteRepository.save(existingInstitute);
    }
}
