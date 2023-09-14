package com.tarento.upsmf.examsAndAdmissions.service;

import com.tarento.upsmf.examsAndAdmissions.model.RetotallingRequest;
import com.tarento.upsmf.examsAndAdmissions.model.dao.Payment;
import com.tarento.upsmf.examsAndAdmissions.repository.PaymentRepository;
import com.tarento.upsmf.examsAndAdmissions.repository.RetotallingRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class RetotallingService {

    @Autowired
    private RetotallingRequestRepository retotallingRequestRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    public void markRequestAsCompleted(Long requestId) {
        RetotallingRequest request = retotallingRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found."));
        request.setStatus("Completed");
        retotallingRequestRepository.save(request);
    }
    public RetotallingRequest requestRetotalling(RetotallingRequest request) {
        request.setRequestDate(LocalDate.now());
        request.setStatus("PENDING");
        return retotallingRequestRepository.save(request);
    }

    public List<RetotallingRequest> getAllPendingRequests() {
        return retotallingRequestRepository.findAll();
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
