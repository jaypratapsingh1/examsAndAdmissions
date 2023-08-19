package com.tarento.upsmf.examsAndAdmissions.model.dto;

import com.tarento.upsmf.examsAndAdmissions.model.dto.CourseDetailDTO;
import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ExamCycleDTO {

    private Long id;
    private String examCycleName;
    private String startDate;
    private String endDate;
    private String createdBy;
    private String createdOn;
    private String modifiedBy;
    private String modifiedOn;
    private String status;
    private Boolean isObsolete;

    private List<CourseDetailDTO> courseDetails;

    // getters and setters
}
