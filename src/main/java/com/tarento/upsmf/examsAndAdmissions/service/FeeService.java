package com.tarento.upsmf.examsAndAdmissions.service;

import com.tarento.upsmf.examsAndAdmissions.model.ExamFee;
import com.tarento.upsmf.examsAndAdmissions.model.PaymentRedirectResponse;
import com.tarento.upsmf.examsAndAdmissions.model.dto.ExamFeeDto;
import com.tarento.upsmf.examsAndAdmissions.model.dto.ExamFeeSearchDto;
import com.tarento.upsmf.examsAndAdmissions.model.dto.ExamSearchResponseDto;
import com.tarento.upsmf.examsAndAdmissions.model.dto.StudentExamFeeDto;

import java.util.List;

public interface FeeService {

    public PaymentRedirectResponse initiateFee(ExamFeeDto examFeeDto);

    ExamSearchResponseDto getAllExamFee(ExamFeeSearchDto examFeeSearchDto);

    ExamFee getExamFeeByRefNo(String refNo);

    void updateExamFeeStatusByRefNo(String refNo);

    List<StudentExamFeeDto> getStudentDetailsByRefNo(String refNo);

    List<ExamFee> getFeeDetailsByExamCycleAndCourse(Long instituteId);
}
