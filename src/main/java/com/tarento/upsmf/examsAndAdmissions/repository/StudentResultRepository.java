package com.tarento.upsmf.examsAndAdmissions.repository;

import com.tarento.upsmf.examsAndAdmissions.model.Exam;
import com.tarento.upsmf.examsAndAdmissions.model.ExamCycle;
import com.tarento.upsmf.examsAndAdmissions.model.StudentResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface StudentResultRepository extends JpaRepository<StudentResult, Long> {
    StudentResult findByStudent_EnrollmentNumber(String enrolmentNumber);
    List<StudentResult> findByCourse_IdAndExam_ExamCycleIdAndPublished(Long courseId, Long examCycleId, boolean b);

    StudentResult findByStudent_EnrollmentNumberAndStudent_DateOfBirthAndPublished(String enrollmentNumber, LocalDate dateOfBirth, boolean b);

    boolean existsByEnrollmentNumber(String enrollmentNumber);
    List<StudentResult> findByExamCycleAndExam(Long examCycle, Exam exam);
    List<StudentResult> findByExamCycleId(Long examCycle);
    List<StudentResult> findByExam(Exam exam);

    List<StudentResult> findByStudent_Institute_IdAndExamCycle_Id(Long instituteId, Long examCycleId);

    @Query("SELECT sr FROM StudentResult sr WHERE sr.examCycle.id = :examCycle AND sr.exam.id = :exam AND sr.student.institute.id = :institute")
    List<StudentResult> findByExamCycleAndExamAndInstitute(@Param("examCycle") Long examCycle, @Param("exam") Long exam, @Param("institute") Long institute);
}
