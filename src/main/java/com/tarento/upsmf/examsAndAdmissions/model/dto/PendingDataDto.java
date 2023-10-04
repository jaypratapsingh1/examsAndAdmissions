package com.tarento.upsmf.examsAndAdmissions.model.dto;// PendingDataDto.java

import com.tarento.upsmf.examsAndAdmissions.model.AttendanceRecord;
import com.tarento.upsmf.examsAndAdmissions.model.ExamFee;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class PendingDataDto {
    private String firstName;  // From student entity
    private String lastName;   // From student entity
    private String courseName; // From exam entity
    private String studentEnrollmentNumber; // From student entity
    private LocalDate registrationDate;
    private String status;
    private String remarks;
    private String examCenterName; // From examCenter entity
    private boolean feesPaid;
    private double attendancePercentage;
}