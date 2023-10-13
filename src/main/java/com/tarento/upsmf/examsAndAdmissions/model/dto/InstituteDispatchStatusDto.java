package com.tarento.upsmf.examsAndAdmissions.model.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
@Setter
@Getter
public class InstituteDispatchStatusDto {
    private Long instituteId;
    private String instituteName;
    private Boolean proofUploaded;
    private LocalDate updatedDate;
    private String examName;
    private String dispatchProofFileLocation;
}
