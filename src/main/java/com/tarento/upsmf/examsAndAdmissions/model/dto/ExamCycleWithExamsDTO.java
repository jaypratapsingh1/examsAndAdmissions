package com.tarento.upsmf.examsAndAdmissions.model.dto;

import com.tarento.upsmf.examsAndAdmissions.model.Exam;
import com.tarento.upsmf.examsAndAdmissions.model.ExamCycle;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExamCycleWithExamsDTO {
    private ExamCycle examCycle;
    private List<Exam> exams;
}
