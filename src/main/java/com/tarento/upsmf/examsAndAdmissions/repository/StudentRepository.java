package com.tarento.upsmf.examsAndAdmissions.repository;

import com.tarento.upsmf.examsAndAdmissions.model.Student;
import com.tarento.upsmf.examsAndAdmissions.model.VerificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
    List<Student> findByVerificationStatus(VerificationStatus status);
    List<Student> findByEnrollmentDateBeforeAndVerificationStatus(LocalDate date, VerificationStatus status);
    List<Student> findByVerificationDateBeforeAndVerificationStatus(LocalDate date, VerificationStatus status);

    Optional<Student> findByEnrollmentNumber(String enrollmentNumber);
}
