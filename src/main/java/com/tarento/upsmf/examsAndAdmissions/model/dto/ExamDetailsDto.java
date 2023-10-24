package com.tarento.upsmf.examsAndAdmissions.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExamDetailsDto {
    private Long examId;
    private String examName;
    private boolean internalMarksUploadStatus;
    private LocalDate lastDateToUploadInternalMarks;
}
