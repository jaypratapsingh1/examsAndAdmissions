package com.tarento.upsmf.examsAndAdmissions.service.impl;

import com.tarento.upsmf.examsAndAdmissions.model.ResponseDto;
import com.tarento.upsmf.examsAndAdmissions.model.dao.Payment;
import com.tarento.upsmf.examsAndAdmissions.repository.PaymentRepository;
import com.tarento.upsmf.examsAndAdmissions.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import com.tarento.upsmf.examsAndAdmissions.util.Constants;

import java.util.List;
import java.util.Optional;

@Service
public class PaymentServiceImpl implements PaymentService {

    @Autowired
    PaymentRepository paymentRepository;
    @Override
    public ResponseDto makePayment(Payment payment) {
        ResponseDto response = new ResponseDto(Constants.API_PAYMENT_ADD);

        // Calculate noOfExams based on the number of selected options
        int noOfExams = payment.getExams().size();

        // Calculate the fee amount based on noOfExams and exam fee amount (replace with actual fee calculation)
        int examFeeAmount = 100; // Example exam fee amount
        int feeAmount = calculateFee(noOfExams, examFeeAmount);

        // Set the calculated values in the FeeManage object
        payment.setNoOfExams(noOfExams);
        payment.setFeeAmount(feeAmount);

        // Save the FeeManage object
        try {
            Payment result = paymentRepository.save(payment);

            response.put(Constants.MESSAGE, Constants.SUCCESSFUL);
            response.put(Constants.RESPONSE, result);
            response.setResponseCode(HttpStatus.OK);
        } catch (Exception e) {
            response.put(Constants.MESSAGE, "Error saving fee details");
            response.put(Constants.RESPONSE, Constants.FAILUREMESSAGE);
            response.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return response;
    }
    public Integer calculateFee(Integer noOfExams, Integer examFeeAmount) {
        return noOfExams * examFeeAmount;
    }

    @Override
    public ResponseEntity<List<Payment>> fetchAllPaymentDetails() {
        List<Payment> paymentDetails = paymentRepository.findAll();
        return new ResponseEntity<>(paymentDetails, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Payment> fetchPaymantDetailsById(Integer id) {
        Optional<Payment> paymentManageOptional = paymentRepository.findById(id);

        if (paymentManageOptional.isPresent()) {
            Payment payment = paymentManageOptional.get();
            return new ResponseEntity<>(payment, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}
