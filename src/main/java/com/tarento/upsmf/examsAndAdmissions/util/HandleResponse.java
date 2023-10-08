package com.tarento.upsmf.examsAndAdmissions.util;

import com.tarento.upsmf.examsAndAdmissions.exception.ExamFeeValidationException;
import com.tarento.upsmf.examsAndAdmissions.model.ResponseDto;
import com.tarento.upsmf.examsAndAdmissions.model.ResponseParams;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class HandleResponse {

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
