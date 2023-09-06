package com.tarento.upsmf.examsAndAdmissions.model.dto;

import lombok.*;

import java.util.Date;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class HallTicketDTO {
    private String examRegistrationNumber;
    private Date dateOfBirth;
}
