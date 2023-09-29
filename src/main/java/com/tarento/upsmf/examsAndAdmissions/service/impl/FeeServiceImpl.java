package com.tarento.upsmf.examsAndAdmissions.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tarento.upsmf.examsAndAdmissions.exception.ExamFeeException;
import com.tarento.upsmf.examsAndAdmissions.model.*;
import com.tarento.upsmf.examsAndAdmissions.model.dto.ExamFeeDto;
import com.tarento.upsmf.examsAndAdmissions.model.dto.ExamFeeSearchDto;
import com.tarento.upsmf.examsAndAdmissions.model.dto.ExamSearchResponseDto;
import com.tarento.upsmf.examsAndAdmissions.repository.ExamFeeRepository;
import com.tarento.upsmf.examsAndAdmissions.service.ExamCycleService;
import com.tarento.upsmf.examsAndAdmissions.service.FeeService;
import com.tarento.upsmf.examsAndAdmissions.service.InstituteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
@Slf4j
public class FeeServiceImpl implements FeeService {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${payment.fee.status.url}")
    private String feeStatusURL;
    @Value("${payment.fee.all.url}")
    private String AllFeeTransactionURL;

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

    @Override
    public ExamSearchResponseDto getAllExamFee(ExamFeeSearchDto examFeeSearchDto) {
        // validate request
        validateGetAllPayload(examFeeSearchDto);
        String sortKey = examFeeSearchDto.getSort().keySet().stream().findFirst().get();
        Sort.Order order = Sort.Order.asc(sortKey);
        if(examFeeSearchDto.getSort().get(sortKey)!=null
                && examFeeSearchDto.getSort().get(sortKey).equalsIgnoreCase("asc")) {
            order = Sort.Order.asc(sortKey);
        }
        PageRequest pageRequest = PageRequest.of(examFeeSearchDto.getPage(), examFeeSearchDto.getSize(), Sort.by(order));
        Page<ExamFee> examFees = examFeeRepository.findAll(pageRequest);
        return ExamSearchResponseDto.builder().count(examFees.getTotalElements()).examFees(examFees.getContent()).build();
    }

    private void validateGetAllPayload(ExamFeeSearchDto examFeeSearchDto) {
        if(examFeeSearchDto == null) {
            examFeeSearchDto = ExamFeeSearchDto.builder()
                    .page(0).size(50).build();
        }
        if(examFeeSearchDto.getPage() <= 0) {
            examFeeSearchDto.setPage(0);
        }
        if(examFeeSearchDto.getSize() <= 0) {
            examFeeSearchDto.setSize(50);
        }
        if(examFeeSearchDto.getSort() == null || examFeeSearchDto.getSort().isEmpty()) {
            Map<String, String> sortMap = new HashMap<>();
            sortMap.put("modifiedNo", "desc");
            examFeeSearchDto.setSort(sortMap);
        } else {
            boolean isKeyMatched = examFeeSearchDto.getSort().entrySet().stream().anyMatch(x -> x.getKey().equalsIgnoreCase("referenceNo") ||
                    x.getKey().equalsIgnoreCase("modifiedOn"));
            if(!isKeyMatched) {
                throw new ExamFeeException("Sort not supported for provided key.");
            }
        }
    }

    @Override
    public ExamFee getExamFeeByRefNo(String refNo) {
        log.info("ref no - {}", refNo);
        // get transaction details from local db
        ExamFee examFee = examFeeRepository.findByReferenceNo(refNo);
        log.info("exam fee - {}", examFee);
        // get latest status from user management
        ResponseEntity<Transaction> paymentUpdateResponse = getPaymentUpdate(refNo);
        log.info("response - {}", paymentUpdateResponse);
        if(paymentUpdateResponse.getStatusCode() == HttpStatus.OK) {
            // update record in DB
            log.info("response body - {}", paymentUpdateResponse.getBody());
        }
        return examFee;
    }

    private ResponseEntity<Transaction> getPaymentUpdate(String refNo) {
        String uri = feeStatusURL.concat(refNo);
        return restTemplate.getForEntity(uri, Transaction.class);
    }

    private void saveExamFee(String referenceNumber, ExamFeeDto examFeeDto) {
        ExamCycle examCycleById = examCycleService.getExamCycleById(examFeeDto.getExamCycleId());
        Institute instituteById = instituteService.getInstituteById(examFeeDto.getInstituteId());
        ExamFee examFee = ExamFee.builder()
                .examCycle(examCycleById)
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
        ExamCycle examCycleById = examCycleService.getExamCycleById(examFeeDto.getExamCycleId());
        if(examCycleById == null) {
            throw new ExamFeeException("Invalid Exam cycle id");
        }
        Institute instituteById = instituteService.getInstituteById(examFeeDto.getInstituteId());
        if(instituteById == null) {
            throw new ExamFeeException("Invalid institute id");
        }
    }
}
