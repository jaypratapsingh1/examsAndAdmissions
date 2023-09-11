package com.tarento.upsmf.examsAndAdmissions.repository;

import com.tarento.upsmf.examsAndAdmissions.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface StudentExamRegistrationRepository extends JpaRepository<StudentExamRegistration, Long> {
    Optional<StudentExamRegistration> findByStudentAndExam(Student student, Exam exam);
    List<StudentExamRegistration> findByStudentIdInAndExamIdIn(Set<Long> studentIds, Set<Long> examIds);
    List<StudentExamRegistration> findByExamCycleId(Long examCycleId);
    List<StudentExamRegistration> findByStudentAndExamCycle(Student student, ExamCycle examCycle);
    List<Exam> findExamsByStudent(Student student);
    long countByAssignedExamCenter(ExamCenter center);

    Optional<StudentExamRegistration> findByIdAndStudent_DateOfBirth(String examRegistrationNumber, LocalDate localDate);
}
