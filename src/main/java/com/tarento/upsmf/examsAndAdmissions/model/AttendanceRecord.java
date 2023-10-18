package com.tarento.upsmf.examsAndAdmissions.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.tarento.upsmf.examsAndAdmissions.enums.ApprovalStatus;
import com.tarento.upsmf.examsAndAdmissions.util.CustomDateDeserializer;
import com.tarento.upsmf.examsAndAdmissions.util.CustomDateSerializer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "attendance_records")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @JsonProperty("First Name")
    private String firstName;
    @JsonProperty("Last Name")
    private String lastName;

    @Column(unique = true)
    @JsonProperty("Enrolment Number")
    private String studentEnrollmentNumber;
    @JsonProperty("Mother's Name")
    private String mothersName;
    @JsonProperty("Father's Name")
    private String fathersName;
    @JsonProperty("Course")
    private String courseName;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_cycle_id")
    private ExamCycle examCycle;
    @JsonProperty("Exam Cycle")
    private String examCycleData;
    @JsonProperty("Start Date")
    @JsonSerialize(using = CustomDateSerializer.class)
    @JsonDeserialize(using = CustomDateDeserializer.class)
    private Date startDate;
    @JsonProperty("End Date")
    @JsonSerialize(using = CustomDateSerializer.class)
    @JsonDeserialize(using = CustomDateDeserializer.class)
    private Date endDate;
    private String rejectionReason;
    @Enumerated(EnumType.STRING)
    private ApprovalStatus approvalStatus = ApprovalStatus.PENDING;
    @JsonProperty("Number of Working Days")
    private int numberOfWorkingDays;
    @JsonProperty("Present Days")
    private int presentDays;
    @JsonProperty("Absent Days")
    private int absentDays;
    @JsonProperty("Attendance Percentage")
    private double attendancePercentage;
    // Calculate attendance percentage before saving to DB
    @PrePersist
    @PreUpdate
    public void calculateAttendancePercentage() {
        this.attendancePercentage = (double) (this.presentDays * 100) / this.numberOfWorkingDays;
    }

    public void setNumberOfWorkingDays(int numberOfWorkingDays) {
        this.numberOfWorkingDays = numberOfWorkingDays;
    }
    public void setAttendancePercentage(double attendancePercentage) {
        this.attendancePercentage = attendancePercentage;
    }
}
