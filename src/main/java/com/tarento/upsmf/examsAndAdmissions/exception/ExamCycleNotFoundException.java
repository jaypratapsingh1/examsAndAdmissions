package com.tarento.upsmf.examsAndAdmissions.exception;

public class ExamCycleNotFoundException extends RuntimeException {
    public ExamCycleNotFoundException(Long id) {
        super("No exam cycle found with ID: " + id);
    }
}