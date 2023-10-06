package com.tarento.upsmf.examsAndAdmissions.model.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class InstituteDto {

    private Long id;
    private String instituteName;
    private String instituteCode;
    private String address;
    private String email;
    private boolean allowedForExamCentre;
    private String district;
    private Boolean cctvVerified;
    private String ipAddress;
    private String remarks;
}
