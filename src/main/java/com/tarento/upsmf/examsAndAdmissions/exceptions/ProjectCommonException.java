package com.tarento.upsmf.examsAndAdmissions.exceptions;

public class ProjectCommonException extends RuntimeException {


    private static final long serialVersionUID = 1L;

    private String code;

    private String message;

    private int responseCode;

    public ProjectCommonException(String code, String message, int responseCode) {
        super();
        this.code = code;
        this.message = message;
        this.responseCode = responseCode;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }
}
