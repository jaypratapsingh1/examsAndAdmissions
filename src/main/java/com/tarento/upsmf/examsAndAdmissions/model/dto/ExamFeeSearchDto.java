package com.tarento.upsmf.examsAndAdmissions.model.dto;

import lombok.*;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class ExamFeeSearchDto {

        private int page;
        private int size;
        private Map<String, String> sort;
        private Map<String, String> filter;
}
