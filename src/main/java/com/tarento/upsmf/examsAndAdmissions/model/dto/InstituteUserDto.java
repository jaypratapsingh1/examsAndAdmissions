package com.tarento.upsmf.examsAndAdmissions.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

@Data
@AllArgsConstructor
@Builder
@ToString
public class InstituteUserDto implements Serializable {

    private String userId;

    private long instituteId;

}
