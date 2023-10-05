package com.tarento.upsmf.examsAndAdmissions.model.dto;

import lombok.*;

import javax.persistence.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class DataCorrectionRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long studentId;
    private String requestedCorrection;
    private String status;
    private String rejectionReason;
    @Column(name = "proof_attachment_path")
    private String proofAttachmentPath;

}
