package com.tarento.upsmf.examsAndAdmissions.exception;

public class ValidationException extends Exception {
    private final boolean isValid;

    public ValidationException(boolean isValid, String message) {
        super(message);
        this.isValid = isValid;
    }

    public boolean isValid() {
        return isValid;
    }
}
