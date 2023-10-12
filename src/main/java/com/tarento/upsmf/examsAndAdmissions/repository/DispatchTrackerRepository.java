package com.tarento.upsmf.examsAndAdmissions.repository;

import com.tarento.upsmf.examsAndAdmissions.model.DispatchTracker;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DispatchTrackerRepository extends JpaRepository<DispatchTracker,Long> {
    List<DispatchTracker> findByExamCycleIdAndExamCenterId(Long examCycleId, Long examCenterId);
    List<DispatchTracker> findByExamCycleIdAndExamCycleId(Long examCycleId, Long examCenterId);
    Optional<DispatchTracker> findByExamCycleId(Long examCycleId);
}
