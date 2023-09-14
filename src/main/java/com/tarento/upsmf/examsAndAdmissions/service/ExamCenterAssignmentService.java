/*
package com.tarento.upsmf.examsAndAdmissions.service;

import com.tarento.upsmf.examsAndAdmissions.model.ExamCenter;
import com.tarento.upsmf.examsAndAdmissions.model.ExamCycle;
import com.tarento.upsmf.examsAndAdmissions.model.Institute;
import com.tarento.upsmf.examsAndAdmissions.model.StudentExamRegistration;
import com.tarento.upsmf.examsAndAdmissions.repository.ExamCenterRepository;
import com.tarento.upsmf.examsAndAdmissions.repository.StudentExamRegistrationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ExamCenterAssignmentService {

    @Autowired
    private ExamCenterRepository examCenterRepository;

    @Autowired
    private StudentExamRegistrationRepository studentExamRegistrationRepository;

    public void assignExamCenterToStudent(StudentExamRegistration registration) {
        List<ExamCenter> centers = examCenterRepository.findAllByIsCctvVerified(true);

        for (ExamCenter center : centers) {
            long studentsAssigned = studentExamRegistrationRepository.countByAssignedExamCenter(center);

            if (studentsAssigned < center.getMaxCapacity()) {
                registration.setAssignedExamCenter(center);
                studentExamRegistrationRepository.save(registration);
                break;
            }
        }
    }
    @Transactional
    public void verifyExamCenterForInstitute(Long instituteId, Long examCycleId, boolean isVerified, String ipAddress, String remarks) {
        Institute institute = instituteRepository.findById(instituteId)
                .orElseThrow(() -> new EntityNotFoundException("Institute not found"));
        ExamCycle examCycle = examCycleRepository.findById(examCycleId)
                .orElseThrow(() -> new EntityNotFoundException("Exam Cycle not found"));

        ExamCenter examCenter = new ExamCenter();
        examCenter.setInstitute(institute);
        examCenter.setExamCycle(examCycle);
        examCenter.setVerified(isVerified);
        examCenter.setIpAddress(ipAddress);
        examCenter.setRemarks(remarks);

        examCenterRepository.save(examCenter);
    }

    public List<Institute> getVerifiedExamCentersForCycle(ExamCycle examCycle) {
        List<ExamCenter> verifiedCenters = examCenterRepository.findByExamCycleAndIsVerified(examCycle, true);
        return verifiedCenters.stream().map(ExamCenter::getInstitute).collect(Collectors.toList());
    }
}
*/
