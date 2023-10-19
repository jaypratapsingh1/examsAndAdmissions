package com.tarento.upsmf.examsAndAdmissions.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DataCorrectionResponseDto {
    private Long id;
    private Long studentId;
    private String status;
    private String proofAttachmentPath;
    private String updatedFirstName;
    private String updatedLastName;
    private String updatedDOB;
}
