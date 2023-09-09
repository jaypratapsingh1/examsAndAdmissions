package com.tarento.upsmf.examsAndAdmissions.repository;

import com.tarento.upsmf.examsAndAdmissions.model.AttendanceVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AttendanceVerificationRepository extends JpaRepository<AttendanceVerification, Long> {}
