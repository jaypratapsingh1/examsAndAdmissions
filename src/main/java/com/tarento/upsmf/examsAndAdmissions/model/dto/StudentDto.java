package com.tarento.upsmf.examsAndAdmissions.model.dto;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StudentDto {
    private String centerCode;
    private String centerName;
    private String courseCode;
    private String courseName;
    private String session;
    private String examBatch;
    private LocalDate  admissionDate;
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
    private String highSchoolRollNo;
    private String highSchoolYearOfPassing;
    private String intermediateRollNo;
    private String intermediateYearOfPassing;
    private String adminRemarks;
    private LocalDate enrollmentDate;
    private LocalDate verificationDate;
    private boolean requiresRevision;
    private String enrollmentNumber;

    private MultipartFile highSchoolMarksheet;
    private MultipartFile highSchoolCertificate;
    private MultipartFile intermediateMarksheet;
    private MultipartFile intermediateCertificate;
}
