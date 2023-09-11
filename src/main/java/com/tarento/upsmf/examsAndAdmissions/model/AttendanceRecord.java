package com.tarento.upsmf.examsAndAdmissions.model;

import com.tarento.upsmf.examsAndAdmissions.enums.ApprovalStatus;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDate;
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
    private String firstName;
    private String lastName;

    @Column(unique = true)
    private String studentEnrollmentNumber;
    private String mothersName;
    private String fathersName;
    private String courseName;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_cycle_id")
    private ExamCycle examCycle;
    private Date startDate;
    private Date endDate;
    private int totalDays;
    private int present;
    private int absent;
    private String rejectionReason;
    @Enumerated(EnumType.STRING)
    private ApprovalStatus approvalStatus = ApprovalStatus.PENDING;
    private double attendancePercentage;
    // Calculate attendance percentage before saving to DB
    @PrePersist
    @PreUpdate
    public void calculateAttendancePercentage() {
        this.attendancePercentage = (double) (this.present * 100) / this.totalDays;
    }
}
