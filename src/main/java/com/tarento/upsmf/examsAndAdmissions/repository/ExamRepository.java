package com.tarento.upsmf.examsAndAdmissions.repository;

import com.tarento.upsmf.examsAndAdmissions.model.Exam;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface ExamRepository extends JpaRepository<Exam, Long> {

    List<Exam> findByObsolete(Integer value);

    Optional<Exam> findByIdAndObsolete(Long id, Integer value);

    Optional<Exam> findByExamName(String examName);

    Optional<Exam> findByExamNameAndExamDateAndStartTimeAndEndTime(String examName, LocalDate examDate, LocalTime startTime, LocalTime endTime);
}
