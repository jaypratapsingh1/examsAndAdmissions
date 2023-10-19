package com.tarento.upsmf.examsAndAdmissions.repository;

import com.tarento.upsmf.examsAndAdmissions.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;

import java.util.ArrayList;
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
    long countByExamCenter(ExamCenter center);

    Optional<StudentExamRegistration> findByIdAndStudent_DateOfBirth(Long id, LocalDate localDate);

    List<StudentExamRegistration> findByExamCenter(ExamCenter originalExamCenter);

    List<StudentExamRegistration> findByInstituteIdAndExamCenterIsNull(Long instituteId);

    List<StudentExamRegistration> findByExamCenterInstituteId(Long instituteId);

    List<StudentExamRegistration> findByInstitute(Institute unverifiedInstitute);
    List<StudentExamRegistration> findByIsFeesPaidAndStatus(boolean isFeesPaid, String status);


    @Modifying
    @Query(value = "update student_exam_registration set is_fees_paid =:status where student_id =:studentId", nativeQuery = true)
    void updateExamFeeByStudentId(@Param("status") Boolean status, @Param("studentId") Long studentId);

    List<StudentExamRegistration> findByExamCenterIsNullAndInstitute(Institute institute);

    StudentExamRegistration findByStudent(Student student);

    Optional<StudentExamRegistration> findByStudentIdAndExamCycleId(Long studentId, Long examCycleId);

    List<StudentExamRegistration> findByStudentIdInAndExamCycleIdIn(Set<Long> studentIds, ArrayList<Long> longs);
}
