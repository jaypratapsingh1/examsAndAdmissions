package com.tarento.upsmf.examsAndAdmissions.model.dto;

import com.tarento.upsmf.examsAndAdmissions.enums.ApprovalStatus;
import lombok.*;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CCTVStatusUpdateDTO {
    private ApprovalStatus approvalStatus = ApprovalStatus.PENDING;
    private String ipAddress;
    private String remarks;
}