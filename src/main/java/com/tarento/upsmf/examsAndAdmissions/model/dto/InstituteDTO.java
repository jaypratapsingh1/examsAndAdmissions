package com.tarento.upsmf.examsAndAdmissions.model.dto;

import com.tarento.upsmf.examsAndAdmissions.model.Institute;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InstituteDTO {

    private Long id;
    private String instituteName;
    private String instituteCode;
    private String address;
    private String email;
    private String district;
    public static InstituteDTO convertToDTO(Institute institute) {
        return InstituteDTO.builder()
                .id(institute.getId())
                .instituteName(institute.getInstituteName())
                .instituteCode(institute.getInstituteCode())
                .address(institute.getAddress())
                .email(institute.getEmail())
                .district(institute.getDistrict())
                .build();
    }
}
