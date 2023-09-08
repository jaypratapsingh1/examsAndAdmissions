package com.tarento.upsmf.examsAndAdmissions.repository;

import com.tarento.upsmf.examsAndAdmissions.model.ExamCenter;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExamCenterRepository extends JpaRepository<ExamCenter, Long> {
    List<ExamCenter> findAllByIsCctvVerified(boolean isVerified);
}
