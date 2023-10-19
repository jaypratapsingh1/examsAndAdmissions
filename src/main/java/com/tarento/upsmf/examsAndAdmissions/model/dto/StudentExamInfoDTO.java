package com.tarento.upsmf.examsAndAdmissions.model.dto;

import com.tarento.upsmf.examsAndAdmissions.model.Exam;
import lombok.*;

import java.time.LocalDate;
import java.util.List;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StudentExamInfoDTO {

    private String firstName;
    private String surname;
    private String enrollmentNumber;
    private String courseName;
    private int admissionDate;
    private int numberOfExams;
    private List<ExamInfoDto> exams;
}
