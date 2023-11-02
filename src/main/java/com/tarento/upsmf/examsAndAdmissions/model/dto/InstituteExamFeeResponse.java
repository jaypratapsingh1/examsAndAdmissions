package com.tarento.upsmf.examsAndAdmissions.model.dto;

import com.tarento.upsmf.examsAndAdmissions.model.ExamCycle;
import com.tarento.upsmf.examsAndAdmissions.model.Institute;
import lombok.*;

import javax.persistence.Transient;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class InstituteExamFeeResponse {

    private ExamCycle examCycle;
    private Institute institute;
    private long totalStudentsCount;
    private long totalPaidCount;
    private double totalPaidAmount;


}
