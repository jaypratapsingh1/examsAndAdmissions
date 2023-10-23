package com.tarento.upsmf.examsAndAdmissions.model.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StudentResultDto {
    private String firstName;
    private String LastName;
    private String courseName;
    private String exam;
    private int internalMark;
    private String enrollmentNumber;
}
