package com.tarento.upsmf.examsAndAdmissions.model.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class ExamFeeRequestDto {

    private Long id;
    private Double fee;
}
