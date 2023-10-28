package com.tarento.upsmf.examsAndAdmissions.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tarento.upsmf.examsAndAdmissions.exception.ExamFeeException;
import com.tarento.upsmf.examsAndAdmissions.exception.InvalidRequestException;
import com.tarento.upsmf.examsAndAdmissions.model.*;
import com.tarento.upsmf.examsAndAdmissions.model.dto.ExamFeeDto;
import com.tarento.upsmf.examsAndAdmissions.model.dto.ExamFeeSearchDto;
import com.tarento.upsmf.examsAndAdmissions.model.dto.ExamSearchResponseDto;
import com.tarento.upsmf.examsAndAdmissions.model.dto.StudentExamFeeDto;
import com.tarento.upsmf.examsAndAdmissions.repository.*;
import com.tarento.upsmf.examsAndAdmissions.service.ExamCycleService;
import com.tarento.upsmf.examsAndAdmissions.service.FeeService;
import com.tarento.upsmf.examsAndAdmissions.service.InstituteService;
import com.tarento.upsmf.examsAndAdmissions.util.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

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

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private ExamRepository examRepository;

    @Autowired
    private StudentExamFeeRepository studentExamFeeRepository;

    @Autowired
    private StudentExamRegistrationRepository studentExamRegistrationRepository;

    @Autowired
    private CourseRepository courseRepository;

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
        Sort.Order order = Sort.Order.desc(sortKey);
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

    @Transactional
    @Override
    public ExamFee getExamFeeByRefNo(String refNo) {
        log.info("ref no - {}", refNo);
        if(refNo == null || refNo.isBlank()) {
            throw new InvalidRequestException("Invalid Reference No.");
        }
        // get transaction details from local db
        ExamFee examFee = examFeeRepository.findByReferenceNo(refNo);
        if(examFee == null) {
            throw new InvalidRequestException("No Record found for Provided Reference No.");
        }
        log.info("exam fee - {}", examFee.getId());
        // get latest status from user management
        /*ResponseEntity<Transaction> paymentUpdateResponse = getPaymentUpdate(refNo);
        log.info("response - {}", paymentUpdateResponse);
        if(paymentUpdateResponse.getStatusCode() == HttpStatus.OK) {
            // update record in DB
            updateStudentFeeStatusByRefNo(refNo);
            log.info("response body - {}", paymentUpdateResponse.getBody());
        }*/
        return examFee;
    }

    @Transactional
    @Override
    public void updateExamFeeStatusByRefNo(String refNo) {
        log.info("updating record for ref no - {}", refNo);
        if(refNo == null || refNo.isBlank()) {
            throw new InvalidRequestException("Invalid Reference No.");
        }
        // get transaction details from local db
        Boolean examFeeExists = examFeeRepository.existsByReferenceNo(refNo);
        if(examFeeExists == null || !examFeeExists) {
            throw new InvalidRequestException("No Record found for Provided Reference No.");
        }
        // update record in DB
        updateStudentFeeStatusByRefNo(refNo);
        log.info("completed record for ref no - {}", refNo);
    }

    @Override
    public List<StudentExamFeeDto> getStudentDetailsByRefNo(String refNo) {
        log.info("ref no - {}", refNo);
        if(refNo == null || refNo.isBlank()) {
            throw new InvalidRequestException("Invalid Reference No.");
        }
        // get transaction details from local db
        List<StudentExam> studentExams = studentExamFeeRepository.findByReferenceNo(refNo);
        if(studentExams == null || studentExams.isEmpty()) {
            throw new InvalidRequestException("No Record found for Provided Reference No.");
        }
        log.info("student list - {}", studentExams.size());
        Map<Long, StudentExamFeeDto> studentExamFeeDtoMap = new HashMap<>();
        studentExams.stream().forEach(student -> {
            if(studentExamFeeDtoMap.containsKey(student.getStudent().getId())) {
                StudentExamFeeDto studentExamFeeDto = studentExamFeeDtoMap.get(student.getStudent().getId());
                if(studentExamFeeDto.getExam() != null) {
                    studentExamFeeDto.getExam().add(student.getExam());
                } else {
                    List<Exam> examList = new ArrayList<>();
                    examList.add(student.getExam());
                    studentExamFeeDto.setExam(examList);
                }
                if(studentExamFeeDto.getAmount() != null) {
                    double total = studentExamFeeDto.getAmount() + student.getAmount();
                    studentExamFeeDto.setAmount(total);
                } else {
                    studentExamFeeDto.setAmount(student.getAmount());
                }
            } else {
                List<Exam> examList = new ArrayList<>();
                examList.add(student.getExam());
                StudentExamFeeDto examFeeDto = StudentExamFeeDto.builder().exam(examList)
                        .student(student.getStudent())
                        .amount(student.getAmount())
                        .status(student.getStatus())
                        .referenceNo(student.getReferenceNo())
                        .build();
                studentExamFeeDtoMap.put(student.getStudent().getId(), examFeeDto);
            }
        });
        return studentExamFeeDtoMap.values().stream().collect(Collectors.toList());
    }

    private void updateStudentFeeStatusByRefNo(String refNo) {
        studentExamFeeRepository.updateStatusByRefNo(StudentExam.Status.PAID.name(), refNo);
        List<Long> studentIds = studentExamFeeRepository.getStudentIdsByRefNo(refNo);
        if(studentIds!=null && !studentIds.isEmpty()) {
            studentIds.stream().forEach(id -> {
                studentExamRegistrationRepository.updateExamFeeByStudentId(true, id);
            });
        }
    }

    private ResponseEntity<Transaction> getPaymentUpdate(String refNo) {
        String uri = feeStatusURL.concat(refNo);
        return restTemplate.getForEntity(uri, Transaction.class);
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
