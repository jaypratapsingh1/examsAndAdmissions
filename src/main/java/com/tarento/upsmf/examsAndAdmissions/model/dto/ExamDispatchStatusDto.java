package com.tarento.upsmf.examsAndAdmissions.model.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Setter
@Getter
public class ExamDispatchStatusDto {
    private Long examId;
    private String examName;
    private Boolean proofUploaded;
    private LocalDate updatedDate;
    private LocalDate lastDateToUpload;
    private String dispatchProofFileLocation;
}
