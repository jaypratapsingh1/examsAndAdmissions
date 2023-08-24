package com.tarento.upsmf.examsAndAdmissions.service.impl;

import com.tarento.upsmf.examsAndAdmissions.model.AuditTrail;
import com.tarento.upsmf.examsAndAdmissions.model.DocumentVerification;
import com.tarento.upsmf.examsAndAdmissions.model.VerificationStatus;
import com.tarento.upsmf.examsAndAdmissions.repository.AuditTrailRepository;
import com.tarento.upsmf.examsAndAdmissions.service.DocumentVerificationService;
import com.tarento.upsmf.examsAndAdmissions.repository.DocumentVerificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class DocumentVerificationServiceImpl implements DocumentVerificationService {
    @Autowired
    private DocumentVerificationRepository documentVerificationRepository;

    @Autowired
    private AuditTrailRepository auditTrailRepository;

    @Override
    public void verifyDocument(Long studentId) {
        DocumentVerification verification = new DocumentVerification();
        verification.setStudentId(studentId);
        verification.setVerificationStatus(VerificationStatus.VERIFIED);
        documentVerificationRepository.save(verification);
        logAuditTrail(studentId, "Document verified by admin");
    }

    @Override
    public void logAuditTrail(Long studentId, String action) {
        AuditTrail auditTrail = new AuditTrail();
        auditTrail.setStudentId(studentId);
        auditTrail.setAction(action);
        auditTrail.setTimestamp(LocalDateTime.now());
        auditTrailRepository.save(auditTrail);
    }
}
