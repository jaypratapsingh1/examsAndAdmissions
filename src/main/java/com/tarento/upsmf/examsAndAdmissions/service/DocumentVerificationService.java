package com.tarento.upsmf.examsAndAdmissions.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public interface DocumentVerificationService {
    public void verifyDocument(Long studentId);
    public void logAuditTrail(Long studentId, String action);
}

