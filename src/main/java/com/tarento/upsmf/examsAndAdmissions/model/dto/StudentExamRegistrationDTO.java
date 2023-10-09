package com.tarento.upsmf.examsAndAdmissions.model.dto;

import lombok.*;

import java.time.LocalDate;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StudentExamRegistrationDTO {

    private Long id;
    private Long studentId;
    private Long examCycleId;
    private Set<Long> examIds;
    private LocalDate registrationDate;
    private String status;
    private String remarks;
    private String createdBy;
    private String updatedBy;
    private Long instituteId;
}
