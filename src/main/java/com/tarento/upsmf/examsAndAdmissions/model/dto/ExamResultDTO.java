package com.tarento.upsmf.examsAndAdmissions.model.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExamResultDTO {
    private String instituteName;
    private Long instituteId;
    private String studentName;
    private String courseName;
    private String examName;
    private Integer internalMarks;
    private Integer FinalMarks;
    private Integer RevisedFinalMarks;
    // ... other fields, getters, setters, etc.
}
