package com.tarento.upsmf.examsAndAdmissions.repository;

import com.tarento.upsmf.examsAndAdmissions.model.Exam;
import com.tarento.upsmf.examsAndAdmissions.model.ExamCycle;
import com.tarento.upsmf.examsAndAdmissions.model.Student;
import com.tarento.upsmf.examsAndAdmissions.model.StudentExamRegistration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

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
}
