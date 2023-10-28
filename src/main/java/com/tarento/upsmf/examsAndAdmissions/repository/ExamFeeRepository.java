package com.tarento.upsmf.examsAndAdmissions.repository;

import com.tarento.upsmf.examsAndAdmissions.model.Course;
import com.tarento.upsmf.examsAndAdmissions.model.ExamFee;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ExamFeeRepository extends PagingAndSortingRepository<ExamFee, Long> {

    ExamFee findByReferenceNo(String referenceNo);

    Boolean existsByReferenceNo(String referenceNo);

    @Query("SELECT ef FROM ExamFee ef WHERE ef.institute.id = :instituteId AND ef.examCycle.course IN :courses AND ef.examCycle.startDate > :currentDate")
    List<ExamFee> findExamFeesByInstituteAndCoursesAndFutureExamCycle(
            @Param("instituteId") Long instituteId,
            @Param("courses") List<Course> courses,
            @Param("currentDate") LocalDate currentDate
    );
}
