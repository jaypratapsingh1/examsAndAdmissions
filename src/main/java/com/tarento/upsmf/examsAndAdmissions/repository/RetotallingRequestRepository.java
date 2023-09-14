package com.tarento.upsmf.examsAndAdmissions.repository;

import com.tarento.upsmf.examsAndAdmissions.model.RetotallingRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RetotallingRequestRepository extends JpaRepository<RetotallingRequest, Long> {
        List<RetotallingRequest> findByStudentId(Long studentId);
        boolean existsByStudent_EnrollmentNumberAndExams_Id(String enrolmentNumber, Long examId);
}