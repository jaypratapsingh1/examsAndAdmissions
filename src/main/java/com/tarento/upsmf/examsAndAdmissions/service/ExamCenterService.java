package com.tarento.upsmf.examsAndAdmissions.service;

import com.tarento.upsmf.examsAndAdmissions.model.ExamCenter;
import com.tarento.upsmf.examsAndAdmissions.model.ExamCycle;
import com.tarento.upsmf.examsAndAdmissions.model.Institute;
import com.tarento.upsmf.examsAndAdmissions.model.StudentExamRegistration;
import com.tarento.upsmf.examsAndAdmissions.model.dto.ApprovalRejectionDTO;
import com.tarento.upsmf.examsAndAdmissions.repository.ExamCenterRepository;
import com.tarento.upsmf.examsAndAdmissions.repository.ExamCycleRepository;
import com.tarento.upsmf.examsAndAdmissions.repository.InstituteRepository;
import com.tarento.upsmf.examsAndAdmissions.repository.StudentExamRegistrationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ExamCenterService {
    @Autowired
    private InstituteRepository instituteRepository;
    @Autowired
    private StudentExamRegistrationRepository studentExamRegistrationRepository;
    @Autowired
    private ExamCenterRepository examCenterRepository;
    @Autowired
    private ExamCycleRepository examCycleRepository;

    public List<ExamCenter> getVerifiedExamCentersInDistrict(String district) {
        return examCenterRepository.findByDistrictAndVerified(district, true);
    }@Transactional
    public void assignAlternateExamCenter(Long unverifiedInstituteId, Long alternateExamCenterId) {
        // Fetch the unverified institute
        Institute unverifiedInstitute = instituteRepository.findById(unverifiedInstituteId)
                .orElseThrow(() -> new EntityNotFoundException("Unverified Institute not found"));

        // Fetch the alternate exam center
        ExamCenter alternateExamCenter = examCenterRepository.findById(alternateExamCenterId)
                .orElseThrow(() -> new EntityNotFoundException("Alternate Exam Center not found"));

        // Ensure both the institute and exam center belong to the same district
        if (!unverifiedInstitute.getDistrict().equals(alternateExamCenter.getDistrict())) {
            throw new IllegalArgumentException("Institute and Alternate Exam Center do not belong to the same district.");
        }

        // Fetch all student registrations linked to the unverified institute
        List<StudentExamRegistration> affectedRegistrations = studentExamRegistrationRepository.findByInstitute(unverifiedInstitute);

        // Update the exam center for these registrations
        for (StudentExamRegistration registration : affectedRegistrations) {
            registration.setExamCenter(alternateExamCenter);
        }

        // Save the updated registrations
        studentExamRegistrationRepository.saveAll(affectedRegistrations);
    }

    public List<ExamCenter> getExamCentersByStatus(ExamCycle examCycle, Boolean isVerifiedStatus) {
        return examCenterRepository.findByExamCycleAndVerified(examCycle, isVerifiedStatus);
    }

    public void updateCCTVStatus(Long examCenterId, Boolean status, String ipAddress, String remarks) {
        ExamCenter examCenter = examCenterRepository.findById(examCenterId)
                .orElseThrow(() -> new EntityNotFoundException("Exam Center not found"));
        examCenter.setVerified(status);
        examCenter.setIpAddress(ipAddress);
        examCenter.setRemarks(remarks);

        // Save the updated exam center
        examCenterRepository.save(examCenter);

        // Update the StudentExamRegistration based on the verification status
        if (status) { // If the exam center is verified
            setExamCenterForStudentsOfInstitute(examCenter.getInstitute().getId(), examCenter);
        } else { // If the exam center is not verified
            unsetExamCenterForStudentsOfInstitute(examCenter.getInstitute().getId());
        }
    }

    private void setExamCenterForStudentsOfInstitute(Long instituteId, ExamCenter verifiedExamCenter) {
        List<StudentExamRegistration> studentsWithoutExamCenters = studentExamRegistrationRepository.findByInstituteIdAndExamCenterIsNull(instituteId);

        for (StudentExamRegistration registration : studentsWithoutExamCenters) {
            registration.setExamCenter(verifiedExamCenter);
        }

        studentExamRegistrationRepository.saveAll(studentsWithoutExamCenters);
    }

    private void unsetExamCenterForStudentsOfInstitute(Long instituteId) {
        // Logic to unset exam center (set to null) or assign to an alternate exam center
        List<StudentExamRegistration> affectedStudents = studentExamRegistrationRepository.findByExamCenterInstituteId(instituteId);

        for (StudentExamRegistration registration : affectedStudents) {
            registration.setExamCenter(null); // Or assign to an alternate exam center
        }

        studentExamRegistrationRepository.saveAll(affectedStudents);
    }

    public List<ExamCenter> getAllExamCenters() {
        return examCenterRepository.findAll();
    }

    public List<ExamCenter> getExamCentersByExamCycle(ExamCycle examCycle) {
        return examCenterRepository.findByExamCycle(examCycle);
    }
}
