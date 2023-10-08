package com.tarento.upsmf.examsAndAdmissions.model.dto;

import com.tarento.upsmf.examsAndAdmissions.model.ExamCycle;
import com.tarento.upsmf.examsAndAdmissions.model.Institute;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ExamCenterDTO {
    private Long id;
    private String name;
    private String address;
    private Boolean verified;
    private long examCycle;  // Similarly, if ExamCycle is complex, create ExamCycleDTO
    private String ipAddress;
    private String remarks;
    private String district;
}
