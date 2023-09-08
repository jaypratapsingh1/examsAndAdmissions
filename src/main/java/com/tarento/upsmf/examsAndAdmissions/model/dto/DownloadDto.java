package com.tarento.upsmf.examsAndAdmissions.model.dto;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class DownloadDto {

    private Long questionPaperId;
    private LocalDate examDate;
    private LocalTime examStartingTime;
}
