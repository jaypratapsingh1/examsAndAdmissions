package com.tarento.upsmf.examsAndAdmissions.model.dto;

import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class StudentExamDto {

    private Long studentId;
    private List<ExamFeeRequestDto> exam;

}
