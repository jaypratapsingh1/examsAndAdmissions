package com.tarento.upsmf.examsAndAdmissions.model.dto;

import lombok.*;

import java.math.BigDecimal;

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
    private BigDecimal internalMarks;
    private BigDecimal FinalMarks;
    private BigDecimal RevisedFinalMarks;
    // ... other fields, getters, setters, etc.
}
