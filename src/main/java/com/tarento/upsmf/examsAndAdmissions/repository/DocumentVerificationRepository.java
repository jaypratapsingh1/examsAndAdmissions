package com.tarento.upsmf.examsAndAdmissions.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.tarento.upsmf.examsAndAdmissions.model.DocumentVerification;

@Repository
public interface DocumentVerificationRepository extends JpaRepository<DocumentVerification, Long> {
}
