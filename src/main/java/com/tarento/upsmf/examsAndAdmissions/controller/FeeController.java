package com.tarento.upsmf.examsAndAdmissions.controller;

import com.tarento.upsmf.examsAndAdmissions.exception.ExamFeeValidationException;
import com.tarento.upsmf.examsAndAdmissions.model.*;
import com.tarento.upsmf.examsAndAdmissions.model.dto.ExamFeeDto;
import com.tarento.upsmf.examsAndAdmissions.model.dto.ExamFeeSearchDto;
import com.tarento.upsmf.examsAndAdmissions.model.dto.ExamSearchResponseDto;
import com.tarento.upsmf.examsAndAdmissions.model.dto.StudentExamFeeDto;
import com.tarento.upsmf.examsAndAdmissions.service.FeeService;
import com.tarento.upsmf.examsAndAdmissions.util.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/fee")
public class FeeController {

    @Autowired
    private FeeService feeService;

    @PostMapping("/initiate")
    public ResponseEntity<ResponseDto> initiateExamFee(@RequestBody ExamFeeDto examFeeDto) {
        try {
            PaymentRedirectResponse paymentRedirectResponse = feeService.initiateFee(examFeeDto);
            return handleSuccessResponse(paymentRedirectResponse);
        } catch (Exception e) {
            return handleErrorResponse(e);
        }
    }

    @PostMapping("/all")
    public ResponseEntity<ResponseDto> getAllExamFee(@RequestBody ExamFeeSearchDto examFeeSearchDto) {
        try {
            ExamSearchResponseDto examFees = feeService.getAllExamFee(examFeeSearchDto);
            return handleSuccessResponse(examFees);
        } catch (Exception e) {
            return handleErrorResponse(e);
        }
    }

    @GetMapping("/status/{refNo}")
    public ResponseEntity<ResponseDto> getExamFeeByRefNo(@PathVariable("refNo") String refNo) {
        try {
            ExamFee examFee = feeService.getExamFeeByRefNo(refNo);
            return handleSuccessResponse(examFee);
        } catch (Exception e) {
            return handleErrorResponse(e);
        }
    }

    @GetMapping("/{examCycleId}/{instituteId}/details")
    public ResponseEntity<ResponseDto> getStudentDetailsByRefNo(@PathVariable("examCycleId") Long examCycleId,
                                                                @PathVariable("instituteId") Long instituteId) {
        try {
            List<StudentExamFeeDto> studentExam = feeService.getStudentDetailsByExamCycleIdAndInstituteId(examCycleId, instituteId);
            return handleSuccessResponse(studentExam);
        } catch (Exception e) {
            return handleErrorResponse(e);
        }
    }

    @PostMapping("/status/update")
    public ResponseEntity<ResponseDto> updateExamFeeByRefNo(@RequestBody String refNo) {
        try {
            feeService.updateExamFeeStatusByRefNo(refNo);
            return handleSuccessResponse(HttpStatus.OK.getReasonPhrase());
        } catch (Exception e) {
            return handleErrorResponse(e);
        }
    }

    public static ResponseEntity<ResponseDto> handleSuccessResponse(Object response) {
        ResponseParams params = new ResponseParams();
        params.setStatus(HttpStatus.OK.getReasonPhrase());
        ResponseDto responseDto = new ResponseDto();
        responseDto.getResult().put(Constants.Parameters.RESPONSE, response);
        responseDto.setResponseCode(HttpStatus.OK);
        responseDto.setParams(params);
        return ResponseEntity.ok().body(responseDto);
    }

    public static ResponseEntity<ResponseDto> handleErrorResponse(Exception e) {
        ResponseParams params = new ResponseParams();
        if(e instanceof ExamFeeValidationException) {
            params.setErrmsg(e.getLocalizedMessage());
            params.setStatus(HttpStatus.BAD_REQUEST.getReasonPhrase());
            return ResponseEntity.badRequest().body(new ResponseDto(HttpStatus.BAD_REQUEST, params));
        }
        params.setErrmsg(e.getLocalizedMessage());
        params.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());
        return ResponseEntity.badRequest().body(new ResponseDto(HttpStatus.INTERNAL_SERVER_ERROR, params));
    }
}
