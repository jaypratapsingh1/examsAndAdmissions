package com.tarento.upsmf.examsAndAdmissions.model.dto;

import com.tarento.upsmf.examsAndAdmissions.enums.ResultStatus;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class ResultDisplayDto {

    private Long id;

    private String instituteName;
    private Long instituteId;

    private String firstName;
    private String lastName;
    private String enrollmentNumber;
    private String motherName;
    private String fatherName;

    private String courseValue;
    private String examCycleValue;
    private String examValue;

    private BigDecimal internalMarks;
    private BigDecimal passingInternalMarks;
    private BigDecimal internalMarksObtained;

    private BigDecimal practicalMarks;
    private BigDecimal passingPracticalMarks;
    private BigDecimal practicalMarksObtained;

    private BigDecimal otherMarks;
    private BigDecimal passingOtherMarks;
    private BigDecimal otherMarksObtained;

    private BigDecimal externalMarks;
    private BigDecimal passingExternalMarks;
    private BigDecimal externalMarksObtained;

    private BigDecimal totalMarks;
    private BigDecimal passingTotalMarks;
    private BigDecimal totalMarksObtained;

    private String grade;
    private String result;

    private ResultStatus status;
    private boolean published;
}
