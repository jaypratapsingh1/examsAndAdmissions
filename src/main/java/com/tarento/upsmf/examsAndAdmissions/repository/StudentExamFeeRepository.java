package com.tarento.upsmf.examsAndAdmissions.repository;

import com.tarento.upsmf.examsAndAdmissions.model.StudentExam;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudentExamFeeRepository extends PagingAndSortingRepository<StudentExam, Long> {

    @Modifying
    @Query(value = "update student_exam_mapping set status =:status where reference_no =:refNo", nativeQuery = true)
    void updateStatusByRefNo(@Param("status") String status, @Param("refNo") String refNo);

    @Query(value = "select student_id from student_exam_mapping where reference_no =:refNo", nativeQuery = true)
    List<Long> getStudentIdsByRefNo(String refNo);

    List<StudentExam> findByReferenceNo(String referenceNo);
}
