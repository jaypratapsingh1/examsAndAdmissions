package com.tarento.upsmf.examsAndAdmissions.exception;

public class DuplicateInstituteException extends RuntimeException {

    public DuplicateInstituteException(String message) {
        super(message);
    }

    public DuplicateInstituteException(String message, Throwable cause) {
        super(message, cause);
    }
}