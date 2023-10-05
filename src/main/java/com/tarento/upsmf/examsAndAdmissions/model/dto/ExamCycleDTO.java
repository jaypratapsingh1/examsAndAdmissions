package com.tarento.upsmf.examsAndAdmissions.model.dto;

import com.tarento.upsmf.examsAndAdmissions.enums.ExamCycleStatus;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class ExamCycleDTO {

    private Long id;
    private String examCycleName;
    // Fields related to Course
    private Long courseId;
    private String courseCode;
    private String courseName;

    private LocalDate startDate;
    private LocalDate endDate;
    private String createdBy;
    private LocalDateTime createdOn;
    private String modifiedBy;
    private LocalDateTime modifiedOn;
    private ExamCycleStatus status;
    private Integer obsolete;
}
