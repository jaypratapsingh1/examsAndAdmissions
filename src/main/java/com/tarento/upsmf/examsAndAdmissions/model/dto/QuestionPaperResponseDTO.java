package com.tarento.upsmf.examsAndAdmissions.model.dto;

import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
public class QuestionPaperResponseDTO {
    private Long id;
    private String fileName;
    private String gcpFileName;
    private LocalDate examDate;
    private LocalTime examStartTime;
    private String examCycleName;
    private Long examCycleId;
    private String examName;
    private String courseName;
    private String createdBy;
    private Timestamp createdOn;
    private String modifiedBy;
    private Timestamp modifiedOn;
    private Long totalMarks;
    private Long internalMarks;
    private Long internalPassingMarks;
    private Long externalMarks;
    private Long externalPassingMarks;
    private Long passingMarks;
    private Integer obsolete;
}
