package com.tarento.upsmf.examsAndAdmissions.service;

import com.tarento.upsmf.examsAndAdmissions.model.dao.Payment;
import com.tarento.upsmf.examsAndAdmissions.model.ResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface PaymentService {
    public ResponseDto makePayment(Payment payment);
    public ResponseEntity<List<Payment>> fetchAllPaymentDetails();
    public ResponseEntity<Payment> fetchPaymantDetailsById(Integer id);
}