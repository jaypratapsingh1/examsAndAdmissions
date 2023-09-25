package com.tarento.upsmf.examsAndAdmissions.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.tarento.upsmf.examsAndAdmissions.enums.VerificationStatus;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "students", indexes = {@Index(columnList = "centerCode")})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(includeFieldNames = true)
@Builder
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String centerCode;
    private String centerName;
    private String session;
    private String examBatch;
    private LocalDate admissionDate;
    private String firstName;
    private String surname;
    private String motherName;
    private String fatherName;
    private LocalDate dateOfBirth;
    private String gender;
    private String caste;
    private String category;
    private String intermediatePassedBoard;
    private String intermediateSubjects;
    private Double intermediatePercentage;
    private String mobileNo;
    private String emailId;
    private String aadhaarNo;
    private String address;
    private String pinCode;
    private String country;
    private String state;
    private String district;
    private String highSchoolMarksheetPath;
    private String highSchoolCertificatePath;
    private String intermediateMarksheetPath;
    private String intermediateCertificatePath;
    private String highSchoolRollNo;
    private String highSchoolYearOfPassing;
    private String intermediateRollNo;
    private String intermediateYearOfPassing;
    @Enumerated(EnumType.STRING)
    private VerificationStatus verificationStatus = VerificationStatus.PENDING;
    @Column(unique = true)
    private String provisionalEnrollmentNumber;
    private String adminRemarks;
    private LocalDate enrollmentDate;
    private LocalDate verificationDate;
    private boolean requiresRevision;
    @Column(unique = true)
    private String enrollmentNumber;
    @ManyToOne
    @JoinColumn(name = "course_id")
    private Course course;
    @ManyToOne
    @JoinColumn(name = "institute_id")
    @JsonBackReference
    private Institute institute;
    @ManyToOne
    @JoinColumn(name = "exam_center_id")
    private Institute examCenter;  // This is where the student will take the exam
    private String academicYear;
}