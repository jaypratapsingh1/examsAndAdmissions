package com.tarento.upsmf.examsAndAdmissions.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tarento.upsmf.examsAndAdmissions.exception.ExamFeeException;
import com.tarento.upsmf.examsAndAdmissions.model.*;
import com.tarento.upsmf.examsAndAdmissions.model.dto.ExamFeeDto;
import com.tarento.upsmf.examsAndAdmissions.repository.ExamFeeRepository;
import com.tarento.upsmf.examsAndAdmissions.service.ExamCycleService;
import com.tarento.upsmf.examsAndAdmissions.service.FeeService;
import com.tarento.upsmf.examsAndAdmissions.service.InstituteService;
import com.tarento.upsmf.examsAndAdmissions.util.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Calendar;
import java.util.UUID;

@Service
@Slf4j
public class FeeServiceImpl implements FeeService {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${payment.initiate.fee.url}")
    private String feeRedirectURL;

    @Value("${payment.initiate.fee.request.endpoint.url}")
    private String feeRequestEndpointURL;

    @Value("${payment.initiate.fee.request.return.url}")
    private String feeRequestReturnURL;

    @Value("${payment.initiate.fee.merchant.id}")
    private String merchantId;

    @Value("${payment.initiate.fee.payment.mode}")
    private String paymentMode;

    @Value("${payment.initiate.fee.sub.merchant.id}")
    private String subMerchantId;

    @Value("${payment.initiate.fee.payer.id}")
    private String payerId;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private ExamFeeRepository examFeeRepository;

    @Autowired
    private ExamCycleService examCycleService;

    @Autowired
    private InstituteService instituteService;

    /**
     * API to save and return payment redirect URL
     *
     * @param examFeeDto
     * @return
     */
    @Override
    public PaymentRedirectResponse initiateFee(ExamFeeDto examFeeDto) throws ExamFeeException{
        // validate payload
        validateInitiateFeePayload(examFeeDto);
        // generate reference no
        String referenceNumber = String.valueOf(examFeeDto.getExamCycleId())
                .concat(String.valueOf(examFeeDto.getInstituteId()))
                .concat(String.valueOf(Calendar.getInstance().getTimeInMillis()))
                .concat(UUID.randomUUID().toString());
        log.debug("Fee payment initiate - Ref No - {}", referenceNumber);
        // make call to payment API
        ResponseEntity<PaymentRedirectResponse> response = getPaymentRedirectResponse(examFeeDto, referenceNumber);
        log.debug("Fee payment initiate - response - {}", response);
        // save details
        if(response.getStatusCode() == HttpStatus.OK) {
            // save details
            saveExamFee(referenceNumber, examFeeDto);
            // send response
            return PaymentRedirectResponse.builder().redirectUrl(response.getBody().getRedirectUrl()).referenceNo(referenceNumber).build();
        }
        // return error
        throw new ExamFeeException("Payment failed");
    }

    private void saveExamFee(String referenceNumber, ExamFeeDto examFeeDto) {
        ResponseDto examCycleById = examCycleService.getExamCycleById(examFeeDto.getExamCycleId());
        Institute instituteById = instituteService.getInstituteById(examFeeDto.getInstituteId());
        ExamFee examFee = ExamFee.builder()
                .examCycle((ExamCycle) examCycleById.get(Constants.RESPONSE))
                .amount(examFeeDto.getAmount())
                .referenceNo(referenceNumber)
                .createdBy(examFeeDto.getCreatedBy())
                .modifiedBy(examFeeDto.getCreatedBy())
                .institute(instituteById)
                .status(ExamFee.Status.INITIATED)
                .build();
        examFeeRepository.save(examFee);
    }

    private ResponseEntity<PaymentRedirectResponse> getPaymentRedirectResponse(ExamFeeDto examFeeDto, String referenceNumber) {
        // create payment request
        PaymentRedirectRequest request = createRequest(examFeeDto, referenceNumber);
        return restTemplate.postForEntity(feeRedirectURL, request, PaymentRedirectResponse.class);
    }

    private PaymentRedirectRequest createRequest(ExamFeeDto examFeeDto, String referenceNumber) {
        MandatoryFields mandatoryFields = MandatoryFields.builder()
                .referenceNo(referenceNumber)
                .submerchantId(subMerchantId)
                .transactionAmount(String.valueOf(examFeeDto.getAmount()))
                .invoiceId("x1")
                .invoiceDate("x")
                .invoiceTime("x")
                .merchantId("x")
                .payerType(examFeeDto.getPayerType().name())
                .payerId(payerId)
                .transactionId("x")
                .transactionDate("x")
                .transactionTime("x")
                .transactionStatus("x")
                .refundId("x")
                .refundDate("x")
                .refundTime("x")
                .refundStatus("x").build();
        return PaymentRedirectRequest.builder()
                .endpoint(feeRequestEndpointURL)
                .returnUrl(feeRequestReturnURL)
                .paymode(paymentMode)
                .secret("")
                .merchantId(merchantId)
                .mandatoryFields(mandatoryFields)
                .optionalFields("")
                .build();
    }

    private void validateInitiateFeePayload(ExamFeeDto examFeeDto) throws ExamFeeException {
        if(examFeeDto == null) {
            throw new ExamFeeException("Invalid Request");
        }
        if(examFeeDto.getExamCycleId() == null || examFeeDto.getExamCycleId() <= 0) {
            throw new ExamFeeException("Invalid Exam ID");
        }
        if(examFeeDto.getInstituteId() == null || examFeeDto.getInstituteId() <= 0) {
            throw new ExamFeeException("Invalid Institute ID");
        }
        if(examFeeDto.getAmount() == null || examFeeDto.getAmount() <= 0) {
            throw new ExamFeeException("Invalid Fee Amount ID");
        }
        if(examFeeDto.getStudentExam() == null || examFeeDto.getStudentExam().isEmpty()) {
            throw new ExamFeeException("Missing Student and Exam Details");
        }
        if(examFeeDto.getPayerType() == null) {
            throw new ExamFeeException("Missing Payer Type Information");
        }
        ResponseDto examCycleById = examCycleService.getExamCycleById(examFeeDto.getExamCycleId());
        if(examCycleById == null) {
            throw new ExamFeeException("Invalid Exam cycle id");
        }
        Institute instituteById = instituteService.getInstituteById(examFeeDto.getInstituteId());
        if(instituteById == null) {
            throw new ExamFeeException("Invalid institute id");
        }
    }
}
