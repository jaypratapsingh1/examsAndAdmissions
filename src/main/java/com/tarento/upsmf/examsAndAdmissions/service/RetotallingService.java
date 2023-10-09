package com.tarento.upsmf.examsAndAdmissions.service;

import com.tarento.upsmf.examsAndAdmissions.enums.RetotallingStatus;
import com.tarento.upsmf.examsAndAdmissions.model.Exam;
import com.tarento.upsmf.examsAndAdmissions.model.ResponseDto;
import com.tarento.upsmf.examsAndAdmissions.model.RetotallingRequest;
import com.tarento.upsmf.examsAndAdmissions.model.Student;
import com.tarento.upsmf.examsAndAdmissions.model.dao.Payment;
import com.tarento.upsmf.examsAndAdmissions.repository.PaymentRepository;
import com.tarento.upsmf.examsAndAdmissions.repository.RetotallingRequestRepository;
import com.tarento.upsmf.examsAndAdmissions.util.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class RetotallingService {

    @Autowired
    private RetotallingRequestRepository retotallingRequestRepository;

    @Autowired
    private PaymentRepository paymentRepository;
    @Autowired
    private StudentResultService studentResultService;

    public void markRequestAsCompleted(Long requestId) {
        RetotallingRequest request = retotallingRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found."));
        request.setStatus(RetotallingStatus.COMPLETED);
        retotallingRequestRepository.save(request);
    }
    public ResponseDto requestRetotalling(RetotallingRequest retotallingRequest) {
        ResponseDto response = new ResponseDto(Constants.API_REQUEST_RETOTALLING);

        // Fetch the student from the database using the enrollmentNumber
        ResponseDto fetchResponse = studentResultService.fetchStudentByEnrollmentNumber(retotallingRequest.getStudent().getEnrollmentNumber());

        if (fetchResponse.getResponseCode() != HttpStatus.OK) {
            return fetchResponse;
        }

        Student existingStudent = (Student) fetchResponse.get(Constants.RESPONSE);

        // Set the fetched student to the retotallingRequest
        retotallingRequest.setStudent(existingStudent);

        // Check for each exam
        for (Exam exam : retotallingRequest.getExams()) {
            // Check if payment was successful
            if (!isPaymentSuccessful(existingStudent.getEnrollmentNumber(), exam.getId())) {
                ResponseDto.setErrorResponse(response, "PAYMENT_NOT_COMPLETED", "Payment not completed for exam ID: " + exam.getId() + ". Please make the payment before requesting re-totalling.", HttpStatus.BAD_REQUEST);
                return response;
            }

            // Check if a re-totalling request already exists
            if (hasAlreadyRequestedRetotalling(existingStudent.getEnrollmentNumber(), exam.getId())) {
                ResponseDto.setErrorResponse(response, "ALREADY_REQUESTED_RETOTALLING", "You have already requested re-totalling for exam ID: " + exam.getId(), HttpStatus.BAD_REQUEST);
                return response;
            }
        }

        // Save the re-totalling request
        retotallingRequest.setRequestDate(LocalDate.now());
        retotallingRequest.setStatus(RetotallingStatus.PENDING);
        RetotallingRequest savedRequest = retotallingRequestRepository.save(retotallingRequest);

        response.put(Constants.MESSAGE, "Retotalling request saved successfully.");
        response.put(Constants.RESPONSE, savedRequest);
        response.setResponseCode(HttpStatus.OK);

        return response;
    }

    public ResponseDto getAllPendingRequests() {
        ResponseDto response = new ResponseDto(Constants.API_GET_ALL_PENDING_REQUESTS);

        List<RetotallingRequest> requests = retotallingRequestRepository.findAll();

        if (requests.isEmpty()) {
            ResponseDto.setErrorResponse(response, "NO_PENDING_REQUESTS", "No pending requests found.", HttpStatus.NOT_FOUND);
        } else {
            response.put(Constants.MESSAGE, "Retrieval successful.");
            response.put(Constants.RESPONSE, requests);
            response.setResponseCode(HttpStatus.OK);
        }

        return response;
    }

    public boolean hasAlreadyRequestedRetotalling(String enrolmentNumber, Long examId) {
        return retotallingRequestRepository.existsByStudent_EnrollmentNumberAndExams_Id(enrolmentNumber, examId);
    }
    public boolean isPaymentSuccessful(String enrolmentNumber, Long examId) {
        Optional<Payment> paymentOptional = Optional.ofNullable(paymentRepository.findByEnrollmentNumber(enrolmentNumber));
        if (paymentOptional.isEmpty()) {
            throw new RuntimeException("Payment not found for the given enrollment number.");
        }

        Payment payment = paymentRepository.findByEnrollmentNumber(enrolmentNumber);
        if (payment != null && payment.getExams() != null) {
            return payment.getExams().stream().anyMatch(exam -> exam.getId().equals(examId));
        }
        return false;
    }
}
