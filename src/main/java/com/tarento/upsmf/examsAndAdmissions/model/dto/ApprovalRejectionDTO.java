package com.tarento.upsmf.examsAndAdmissions.model.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApprovalRejectionDTO {
    private Long instituteId;
    private String ipAddress;
    private String remarks;
    private String action; // "approve" or "reject"
}
