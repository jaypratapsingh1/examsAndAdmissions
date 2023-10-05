package com.tarento.upsmf.examsAndAdmissions.model;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

import lombok.Getter;
import org.springframework.http.HttpStatus;

public class ResponseDto {

    private String id;
    private String ver;
    @Getter
    private String ts;
    private ResponseParams params;
    private HttpStatus responseCode;
    private int responseCodeNumeric;
    private ErrorDetails error; // Error details
    private transient Map<String, Object> result = new HashMap<>();

    public static class ErrorDetails {
        private String code;
        private String message;
        private String details;

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getDetails() {
            return details;
        }

        public void setDetails(String details) {
            this.details = details;
        }
    }

    public ResponseDto() {
        this.ver = "v1";
        this.ts = String.valueOf(new Timestamp(System.currentTimeMillis()));
        this.params = new ResponseParams();
    }

    public ResponseDto(HttpStatus status, ResponseParams params) {
        this.ver = "v1";
        this.ts = String.valueOf(new Timestamp(System.currentTimeMillis()));
        this.params = params;
        this.responseCode = status;
    }

    public ResponseDto(String id) {
        this();
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getVer() {
        return ver;
    }

    public void setVer(String ver) {
        this.ver = ver;
    }

    public void setTs(String ts) {
        this.ts = ts;
    }

    public ResponseParams getParams() {
        return params;
    }

    public void setParams(ResponseParams params) {
        this.params = params;
    }

    public HttpStatus getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(HttpStatus responseCode) {
        this.responseCode = responseCode;
        this.responseCodeNumeric = responseCode.value();
    }

    public int getResponseCodeNumeric() {
        return responseCodeNumeric;
    }

    public void setResponseCodeNumeric(int responseCodeNumeric) {
        this.responseCodeNumeric = responseCodeNumeric;
    }

    public Map<String, Object> getResult() {
        return result;
    }

    public void setResult(Map<String, Object> result) {
        this.result = result;
    }

    public Object get(String key) {
        return result.get(key);
    }

    public void put(String key, Object vo) {
        result.put(key, vo);
    }

    public void putAll(Map<String, Object> map) {
        result.putAll(map);
    }

    public boolean containsKey(String key) {
        return result.containsKey(key);
    }

    public ErrorDetails getError() {
        return error;
    }

    public void setError(ErrorDetails error) {
        this.error = error;
    }
}
