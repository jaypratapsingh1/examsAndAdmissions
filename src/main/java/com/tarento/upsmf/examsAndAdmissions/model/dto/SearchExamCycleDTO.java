package com.tarento.upsmf.examsAndAdmissions.model.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class SearchExamCycleDTO {

    private String courseId;
    private Integer startYear;
    private Integer endYear;
}
