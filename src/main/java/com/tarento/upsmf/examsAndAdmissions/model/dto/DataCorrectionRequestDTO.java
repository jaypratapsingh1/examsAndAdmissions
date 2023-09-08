package com.tarento.upsmf.examsAndAdmissions.model.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DataCorrectionRequestDTO {
    private Long studentId;
    private String correctionDetails;
}
