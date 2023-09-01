package com.tarento.upsmf.examsAndAdmissions.repository;

import com.tarento.upsmf.examsAndAdmissions.model.AttendanceRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AttendanceRepository extends JpaRepository<AttendanceRecord, Long> {
    boolean existsByStudentEnrollmentNumber(String studentEnrollmentNumber);
    AttendanceRecord findByStudentEnrollmentNumber(String studentEnrollmentNumber);
}