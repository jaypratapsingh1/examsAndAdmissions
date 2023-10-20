package com.tarento.upsmf.examsAndAdmissions.model.dto;

import com.tarento.upsmf.examsAndAdmissions.enums.ApprovalStatus;
import lombok.*;

import java.time.LocalDate;
import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AttendanceRecordDto {
    private Long id;
    private String firstName;
    private String lastName;
    private String studentEnrollmentNumber;
    private String mothersName;
    private String fathersName;
    private String courseName;
    private String examCycleData;
    private Date startDate;
    private Date endDate;
    private String rejectionReason;
    private ApprovalStatus approvalStatus;
    private int numberOfWorkingDays;
    private int presentDays;
    private int absentDays;
    private double attendancePercentage;
}

