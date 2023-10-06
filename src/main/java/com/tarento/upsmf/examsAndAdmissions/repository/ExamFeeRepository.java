package com.tarento.upsmf.examsAndAdmissions.repository;

import com.tarento.upsmf.examsAndAdmissions.model.ExamFee;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExamFeeRepository extends PagingAndSortingRepository<ExamFee, Long> {

    ExamFee findByReferenceNo(String referenceNo);

    Boolean existsByReferenceNo(String referenceNo);
}
