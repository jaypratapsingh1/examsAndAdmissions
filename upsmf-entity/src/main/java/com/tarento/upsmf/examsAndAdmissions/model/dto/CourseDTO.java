package com.tarento.upsmf.examsAndAdmissions.model.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CourseDTO {

    private Long id;
    private String courseCode;
    private String courseName;

}