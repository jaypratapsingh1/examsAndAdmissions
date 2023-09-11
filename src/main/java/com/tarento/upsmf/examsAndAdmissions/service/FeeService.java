package com.tarento.upsmf.examsAndAdmissions.service;

import com.tarento.upsmf.examsAndAdmissions.model.PaymentRedirectResponse;
import com.tarento.upsmf.examsAndAdmissions.model.dto.ExamFeeDto;

public interface FeeService {

    public PaymentRedirectResponse initiateFee(ExamFeeDto examFeeDto);
}
