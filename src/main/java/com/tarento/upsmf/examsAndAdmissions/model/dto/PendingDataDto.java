package com.tarento.upsmf.examsAndAdmissions.model.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class PendingDataDto {
    private Long id; // assuming it's the ID of the StudentExamRegistration or the Student
    private String firstName;
    private String lastName;
    private LocalDate dob; // Date of Birth
    private String courseName;
    private String courseYear; // Assuming course year is a String. Adjust if needed.
    private String studentEnrollmentNumber;
    private LocalDate registrationDate;
    private String status;
    private String remarks;
    private String examCenterName;
    private boolean feesPaid;
    private double attendancePercentage;

    private ExamCycleDetails examCycle;

    @Getter
    @Setter
    public static class ExamCycleDetails {
        private Long id;
        private String name;
        private LocalDate startDate;
        private LocalDate endDate;
        private String createdBy;
        private String modifiedBy;
        private String status; // Assuming status is a string representation of the ExamCycleStatus enum
        private Integer obsolete;

        private List<ExamDetails> exams;
    }

    @Getter
    @Setter
    public static class ExamDetails {
        private String examName;
        private LocalDate examDate;
        private String startTime;
        private String endTime;
        private String createdBy;
        private String modifiedBy;
        private Boolean isResultsPublished;
        private Integer obsolete;
        //... any other relevant details
    }
}
