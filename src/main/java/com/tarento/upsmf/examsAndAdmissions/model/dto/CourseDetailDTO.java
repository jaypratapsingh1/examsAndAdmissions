package com.tarento.upsmf.examsAndAdmissions.model.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CourseDetailDTO {
    private Long id;
    private Long courseId;
    private List<ExamDTO> exams;

}