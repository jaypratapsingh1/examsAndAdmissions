package com.tarento.upsmf.examsAndAdmissions.model.dto;

import com.tarento.upsmf.examsAndAdmissions.enums.ApprovalStatus;
import com.tarento.upsmf.examsAndAdmissions.model.ExamCycle;
import com.tarento.upsmf.examsAndAdmissions.model.Institute;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ExamCenterDTO {
    private Long id;
    private String name;
    private String address;
    private ApprovalStatus approvalStatus = ApprovalStatus.PENDING;
    private long examCycle;  // Similarly, if ExamCycle is complex, create ExamCycleDTO
    private String ipAddress;
    private String remarks;
    private String district;
    private boolean allowedForExamCentre;
    private String instituteCode;
}
