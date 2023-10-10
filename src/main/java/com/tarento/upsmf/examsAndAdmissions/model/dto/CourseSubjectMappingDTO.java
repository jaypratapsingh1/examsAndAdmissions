package com.tarento.upsmf.examsAndAdmissions.model.dto;

import lombok.Data;

import java.util.List;

@Data
public class CourseSubjectMappingDTO {

    private List<Long> subjectId;
    private Long courseId;
}
