package com.tarento.upsmf.examsAndAdmissions.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class UploadStatusDetails {
    private int totalRecords;
    private int uploadedRecords;
    private int skippedRecords;
    private boolean success;
    private String errorMessage;
}