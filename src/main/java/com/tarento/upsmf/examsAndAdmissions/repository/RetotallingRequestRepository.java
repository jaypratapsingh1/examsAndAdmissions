package com.tarento.upsmf.examsAndAdmissions.repository;

import com.tarento.upsmf.examsAndAdmissions.model.Exam;
import com.tarento.upsmf.examsAndAdmissions.model.RetotallingRequest;
import com.tarento.upsmf.examsAndAdmissions.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RetotallingRequestRepository extends JpaRepository<RetotallingRequest, Long> {
        List<RetotallingRequest> findByStudentId(Long studentId);
        boolean existsByStudent_EnrollmentNumberAndExams_Id(String enrolmentNumber, Long examId);
        Optional<RetotallingRequest> findByStudentAndExams(Student student, Exam exam);
}