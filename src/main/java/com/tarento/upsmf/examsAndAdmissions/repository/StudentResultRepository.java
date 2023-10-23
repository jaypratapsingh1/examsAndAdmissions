package com.tarento.upsmf.examsAndAdmissions.repository;

import com.tarento.upsmf.examsAndAdmissions.model.Exam;
import com.tarento.upsmf.examsAndAdmissions.model.ExamCycle;
import com.tarento.upsmf.examsAndAdmissions.model.StudentResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface StudentResultRepository extends JpaRepository<StudentResult, Long> {
    StudentResult findByStudent_EnrollmentNumber(String enrolmentNumber);
    List<StudentResult> findByCourse_IdAndExam_ExamCycleIdAndPublished(Long courseId, Long examCycleId, boolean b);

    StudentResult findByStudent_EnrollmentNumberAndStudent_DateOfBirthAndPublished(String enrollmentNumber, LocalDate dateOfBirth, boolean b);

    boolean existsByEnrollmentNumber(String enrollmentNumber);
    List<StudentResult> findByExamCycleAndExam(Long examCycle, Exam exam);
    List<StudentResult> findByExamCycleId(Long examCycle);
    List<StudentResult> findByExam(Exam exam);

    List<StudentResult> findByStudent_Institute_IdAndExamCycle_IdAndExam_id(Long instituteId, Long examCycleId, Long examId);

    @Query("SELECT sr FROM StudentResult sr WHERE sr.examCycle.id = :examCycle AND sr.exam.id = :exam AND sr.student.institute.id = :institute")
    List<StudentResult> findByExamCycleAndExamAndInstitute(@Param("examCycle") Long examCycle, @Param("exam") Long exam, @Param("institute") Long institute);
    Optional<StudentResult> findByExamIdAndStudentId(Long examId, Long studentId);
    @Modifying
    @Transactional
    @Query(value = "UPDATE student_results SET external_marks = null FROM exam, students WHERE student_results.exam_cycle_id = exam.exam_cycle_id AND student_results.student_id = students.id AND exam.exam_cycle_id = :examCycleId AND students.institute_id = :instituteId", nativeQuery = true)
    int setExternalMarksToNull(@Param("examCycleId") Long examCycleId, @Param("instituteId") Long instituteId);
}
