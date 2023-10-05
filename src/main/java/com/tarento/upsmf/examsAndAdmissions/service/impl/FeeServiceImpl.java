package com.tarento.upsmf.examsAndAdmissions.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tarento.upsmf.examsAndAdmissions.exception.ExamFeeException;
import com.tarento.upsmf.examsAndAdmissions.model.*;
import com.tarento.upsmf.examsAndAdmissions.model.dto.ExamFeeDto;
import com.tarento.upsmf.examsAndAdmissions.repository.ExamFeeRepository;
import com.tarento.upsmf.examsAndAdmissions.repository.ExamRepository;
import com.tarento.upsmf.examsAndAdmissions.repository.StudentExamFeeRepository;
import com.tarento.upsmf.examsAndAdmissions.repository.StudentRepository;
import com.tarento.upsmf.examsAndAdmissions.service.ExamCycleService;
import com.tarento.upsmf.examsAndAdmissions.service.FeeService;
import com.tarento.upsmf.examsAndAdmissions.service.InstituteService;
import com.tarento.upsmf.examsAndAdmissions.util.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
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

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private ExamRepository examRepository;

    @Autowired
    private StudentExamFeeRepository studentExamFeeRepository;

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
        examFee = examFeeRepository.save(examFee);
        // save student to exam mapping
        saveStudentExamFeeMapping(referenceNumber, examFeeDto);
    }

    private void saveStudentExamFeeMapping(String referenceNumber, ExamFeeDto examFeeDto) {
        List<StudentExam> studentExams = new ArrayList<>();
        // iterate through student and exam map
        examFeeDto.getStudentExam().entrySet().stream().forEach(entry -> {
            String studentId = entry.getKey();
            Map<Long, Double> exams = entry.getValue();
            Optional<Student> student = studentRepository.findById(Long.parseLong(studentId));
            if(student.isPresent() && exams!=null && !exams.isEmpty()) {
                // iterate through inner map to get exam id and corresponding fee
                exams.entrySet().stream().forEach(examEntry -> {
                    // get exam by id
                    Optional<Exam> examDetails = examRepository.findById(examEntry.getKey());
                    //validate
                    if(examDetails.isPresent() && examEntry.getValue() != null && examEntry.getValue() > 0) {
                        // create student exam object
                        StudentExam studentExam = StudentExam.builder()
                                .referenceNo(referenceNumber)
                                .exam(examDetails.get())
                                .student(student.get())
                                .amount(examEntry.getValue())
                                .status(StudentExam.Status.INITIATED)
                                .build();
                        // add to the list
                        studentExams.add(studentExam);
                    }
                });
            }
        });
        // save student exams
        if(!studentExams.isEmpty()) {
            studentExamFeeRepository.saveAll(studentExams);
        }
    }

    private ResponseEntity<PaymentRedirectResponse> getPaymentRedirectResponse(ExamFeeDto examFeeDto, String referenceNumber) {
        // create payment request
        PaymentRedirectRequest request = createRequest(examFeeDto, referenceNumber);
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setBearerAuth("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJSR3RkMkZzeG1EMnJER3I4dkJHZ0N6MVhyalhZUzBSSyJ9.kMLn6177rvY53i0RAN3SPD5m3ctwaLb32pMYQ65nBdA");
        HttpEntity<PaymentRedirectRequest> entity = new HttpEntity<PaymentRedirectRequest>(request, httpHeaders);
        return restTemplate.postForEntity(feeRedirectURL, entity, PaymentRedirectResponse.class);
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
            throw new ExamFeeException("Invalid Exam Cycle ID");
        }
        Institute instituteById = instituteService.getInstituteById(examFeeDto.getInstituteId());
        if(instituteById == null) {
            throw new ExamFeeException("Invalid Institute ID");
        }
    }
}
