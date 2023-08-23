package com.tarento.upsmf.examsAndAdmissions.controller;

import com.tarento.upsmf.examsAndAdmissions.model.ResponseDto;
import com.tarento.upsmf.examsAndAdmissions.model.dao.Payment;
import com.tarento.upsmf.examsAndAdmissions.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/institute")
public class PaymentController {

    @Autowired
    PaymentService paymentService;

    @PostMapping("/payment")
    public ResponseEntity<?> makePayment(@RequestBody Payment payment){
        ResponseDto response = paymentService.makePayment(payment);
        return new ResponseEntity<>(response, response.getResponseCode());
    }
    @GetMapping("/payment")
    public ResponseEntity<?> getPaymentDetails(){
        ResponseEntity<List<Payment>> response = paymentService.fetchAllPaymentDetails();
        return new ResponseEntity<>(response, response.getStatusCode());
    }
    @GetMapping("/{id}")
    public ResponseEntity<?> fetchPaymentDetailsById(@PathVariable Integer id) {
        ResponseEntity<Payment> response = paymentService.fetchPaymantDetailsById(id);
        return new ResponseEntity<>(response.getBody(), response.getStatusCode());
    }}