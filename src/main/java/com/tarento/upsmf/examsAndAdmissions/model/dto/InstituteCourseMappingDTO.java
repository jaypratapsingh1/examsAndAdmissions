package com.tarento.upsmf.examsAndAdmissions.model.dto;

import lombok.Data;

@Data
public class InstituteCourseMappingDTO {

    private Long instituteId;
    private Long courseId;
    private Long seatCapacity;

}
