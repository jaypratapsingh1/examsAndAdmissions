package com.tarento.upsmf.examsAndAdmissions.repository;

import com.tarento.upsmf.examsAndAdmissions.model.ExamFee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExamFeeRepository extends PagingAndSortingRepository<ExamFee, Long> {

    ExamFee findByReferenceNo(String referenceNo);

    Boolean existsByReferenceNo(String referenceNo);

    Page<ExamFee> findAllByExamCycleId(Long examCycleId, PageRequest pageRequest);
}
