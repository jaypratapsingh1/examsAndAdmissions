package com.tarento.upsmf.examsAndAdmissions.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExamFeeDto implements Serializable {

    private Long examCycleId;

    private Long instituteId;

    private Map<String, List<Long>> studentExam;

    private Double amount;

    private PayerType payerType;

    private String createdBy;

    public enum PayerType {
        INSTITUTE
    }
}
