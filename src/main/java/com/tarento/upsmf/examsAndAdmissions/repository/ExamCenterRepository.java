package com.tarento.upsmf.examsAndAdmissions.repository;

import com.tarento.upsmf.examsAndAdmissions.model.ExamCenter;
import com.tarento.upsmf.examsAndAdmissions.model.ExamCycle;
import com.tarento.upsmf.examsAndAdmissions.model.Institute;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ExamCenterRepository extends JpaRepository<ExamCenter, Long> {
    List<ExamCenter> findByExamCycleAndVerified(ExamCycle examCycle, Boolean isVerifiedStatus);

    Optional<ExamCenter> findByInstituteAndExamCycle(Institute institute, ExamCycle examCycle);

    List<ExamCenter> findByExamCycle(ExamCycle examCycle);
    List<ExamCenter> findByDistrictAndVerified(String district, Boolean verified);
    Optional<ExamCenter> findByInstituteCodeAndVerifiedTrue(String instituteCode);
}
