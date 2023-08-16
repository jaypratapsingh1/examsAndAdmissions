package com.tarento.upsmf.examsAndAdmissions.model.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ExamCycleDTO {
    private String startDate;
    private String endDate;
    private List<CourseDetailDTO> courseDetails;

}