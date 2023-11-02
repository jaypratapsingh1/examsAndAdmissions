package com.tarento.upsmf.examsAndAdmissions.model.dto;

import com.tarento.upsmf.examsAndAdmissions.model.StudentResult;
import lombok.*;

import java.util.List;

public class ValidationResultDto {
    private boolean isValid;
    @Getter
    private List<String> validationErrors;
    @Getter
    private List<StudentResult> savedEntities;

    public boolean isValid() {
        return isValid;
    }

    public void setValid(boolean isValid) {
        this.isValid = isValid;
    }

    public void setValidationErrors(List<String> validationErrors) {
        this.validationErrors = validationErrors;
    }

    public void setSavedEntities(List<StudentResult> savedEntities) {
        this.savedEntities = savedEntities;
    }
}
