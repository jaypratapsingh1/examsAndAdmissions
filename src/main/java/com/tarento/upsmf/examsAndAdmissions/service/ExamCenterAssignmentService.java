package com.tarento.upsmf.examsAndAdmissions.service;

import com.tarento.upsmf.examsAndAdmissions.model.ExamCenter;
import com.tarento.upsmf.examsAndAdmissions.model.StudentExamRegistration;
import com.tarento.upsmf.examsAndAdmissions.repository.ExamCenterRepository;
import com.tarento.upsmf.examsAndAdmissions.repository.StudentExamRegistrationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

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
}
