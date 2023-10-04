package com.tarento.upsmf.examsAndAdmissions.service;

import com.tarento.upsmf.examsAndAdmissions.exception.DuplicateInstituteException;
import com.tarento.upsmf.examsAndAdmissions.exception.InvalidRequestException;
import com.tarento.upsmf.examsAndAdmissions.model.Institute;
import com.tarento.upsmf.examsAndAdmissions.model.InstituteUser;
import com.tarento.upsmf.examsAndAdmissions.model.dto.ApprovalRejectionDTO;
import com.tarento.upsmf.examsAndAdmissions.model.dto.InstituteUserDto;
import com.tarento.upsmf.examsAndAdmissions.repository.InstituteRepository;
import com.tarento.upsmf.examsAndAdmissions.repository.InstituteUserMappingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class InstituteService {
    @Autowired
    private InstituteRepository instituteRepository;

    public Institute updateVerificationStatus(ApprovalRejectionDTO dto) {
    @Autowired
    private InstituteUserMappingRepository instituteUserMappingRepository;

    public void updateVerificationStatus(ApprovalRejectionDTO dto) {
        Institute institute = instituteRepository.findById(dto.getInstituteId())
                .orElseThrow(() -> new EntityNotFoundException("Institute not found"));
        institute.handleAction(dto.getAction(), dto.getIpAddress(), dto.getRemarks());
        instituteRepository.save(institute);
        return institute;
    }

    public Institute markNotAllowedForExamCentre(Long instituteId) {
        Institute institute = instituteRepository.findById(instituteId)
                .orElseThrow(() -> new EntityNotFoundException("Institute not found"));

        institute.setAllowedForExamCentre(false);
        instituteRepository.save(institute);
        return institute;
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

    public Optional<Institute> getInstituteById(String id) {
        return Optional.ofNullable(instituteRepository.findByInstituteCode(id));
    public List<Institute> getInstituteByUserId(String userId) {
        InstituteUser instituteUser = instituteUserMappingRepository.findByUserId(userId);
        if(instituteUser == null || instituteUser.getInstitute() == null) {
            return Collections.emptyList();
        }
        return Collections.singletonList(instituteUser.getInstitute());
    }

    public Boolean addInstituteUserMapping(InstituteUserDto instituteUserDto) {
        // validate payload
        validateInstituteMappingPayload(instituteUserDto);
        // check if institute exists
        Optional<Institute> optionalInstitute = instituteRepository.findById(instituteUserDto.getInstituteId());
        if(optionalInstitute.isPresent()) {
            InstituteUser instituteUser = InstituteUser.builder()
                    .userId(instituteUserDto.getUserId())
                    .institute(optionalInstitute.get())
                    .build();
            instituteUserMappingRepository.save(instituteUser);
            return true;
        }
        return false;
    }

    private void validateInstituteMappingPayload(InstituteUserDto instituteUserDto) {
        if(instituteUserDto == null) {
            throw new InvalidRequestException("Invalid Request");
        }
        if(instituteUserDto.getUserId() == null || instituteUserDto.getUserId().isBlank()) {
            throw new InvalidRequestException("Invalid User ID");
        }
        if(instituteUserDto.getInstituteId() <= 0) {
            throw new InvalidRequestException("Invalid Institute ID");
        }
    }
}
