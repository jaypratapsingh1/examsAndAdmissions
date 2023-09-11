package com.tarento.upsmf.examsAndAdmissions.model.dto;

import lombok.*;

import javax.persistence.Entity;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CCTVStatusUpdateDTO {
    private Boolean status;
    private String ipAddress;
    private String remarks;
}