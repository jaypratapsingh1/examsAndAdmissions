package com.tarento.upsmf.examsAndAdmissions.repository;

import com.tarento.upsmf.examsAndAdmissions.model.StudentResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResultRepository extends JpaRepository<StudentResult, Long> {
    StudentResult findByEnrollmentNumber(String enrollmentNumber);

    List<StudentResult> findByStudentId(Long studentId);
}