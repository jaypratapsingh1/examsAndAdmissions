package com.tarento.upsmf.examsAndAdmissions.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "document_verification")
public class DocumentVerification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "student_id")
    private Long studentId;

    @ManyToOne
    @JoinColumn(name = "admin_id")
    private Admin admin;

    @Column(name = "verification_status")
    private VerificationStatus verificationStatus;

    @Column(name = "approval_status")
    private VerificationStatus approvalStatus;

    @Column(name = "approval_datetime")
    private LocalDateTime approvalDatetime;
}
