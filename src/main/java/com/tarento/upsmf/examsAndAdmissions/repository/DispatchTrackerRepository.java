package com.tarento.upsmf.examsAndAdmissions.repository;

import com.tarento.upsmf.examsAndAdmissions.model.DispatchTracker;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DispatchTrackerRepository extends JpaRepository<DispatchTracker,Long> {
    List<DispatchTracker> findByExamCycleIdAndExamId(Long examCycleId, Long examId);
}
