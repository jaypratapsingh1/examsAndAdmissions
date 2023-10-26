package com.tarento.upsmf.examsAndAdmissions.model.dto;

import com.tarento.upsmf.examsAndAdmissions.enums.ResultStatus;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class ResultDisplayDto {

    private Long id;

    private String instituteName;
    private Long institute_id;

    private String firstName;
    private String lastName;
    private String enrollmentNumber;
    private String motherName;
    private String fatherName;

    private String courseValue;
    private String examCycleValue;
    private String examValue;

    private Integer internalMarks;
    private Integer passingInternalMarks;
    private Integer internalMarksObtained;

    private Integer practicalMarks;
    private Integer passingPracticalMarks;
    private Integer practicalMarksObtained;

    private Integer otherMarks;
    private Integer passingOtherMarks;
    private Integer otherMarksObtained;

    private Integer externalMarks;
    private Integer passingExternalMarks;
    private Integer externalMarksObtained;

    private Integer totalMarks;
    private Integer passingTotalMarks;
    private Integer totalMarksObtained;

    private String grade;
    private String result;

    private ResultStatus status;
    private boolean published;
}
