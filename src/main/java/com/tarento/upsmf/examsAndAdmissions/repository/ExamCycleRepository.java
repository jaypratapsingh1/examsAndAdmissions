package com.tarento.upsmf.examsAndAdmissions.repository;

import com.tarento.upsmf.examsAndAdmissions.model.Course;
import com.tarento.upsmf.examsAndAdmissions.model.ExamCycle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ExamCycleRepository extends JpaRepository<ExamCycle, Long> {
    
    // Fetch all non-obsolete records
    List<ExamCycle> findByObsolete(Integer value);

    // Fetch a non-obsolete record by ID
    Optional<ExamCycle> findByIdAndObsolete(Long id, Integer value);
    ExamCycle findByExamCycleName(String name);

    ExamCycle findByExamCycleNameAndCourseAndStartDateAndEndDate(String examCycleName, Course course, LocalDate startDate, LocalDate endDate);
}
