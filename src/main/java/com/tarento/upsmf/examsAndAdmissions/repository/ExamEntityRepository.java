package com.tarento.upsmf.examsAndAdmissions.repository;

import com.tarento.upsmf.examsAndAdmissions.model.ExamUploadData;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExamEntityRepository extends JpaRepository<ExamUploadData, Long>{
    boolean findByCourseAndExamcycleName(String course, String examcycleName);
}
