package com.tarento.upsmf.examsAndAdmissions.model;

import com.tarento.upsmf.examsAndAdmissions.enums.ResultStatus;
import lombok.*;

import javax.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "student_results")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class StudentResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "student_id")
    private Student student;

    @ManyToOne
    @JoinColumn(name = "course_id")
    private Course course;

    @ManyToOne
    @JoinColumn(name = "exam_id")
    private Exam exam;
    private BigDecimal internalMarks;
    private BigDecimal passingInternalMarks;
    private BigDecimal internalMarksObtained;

    private BigDecimal externalMarks;
    private BigDecimal passingExternalMarks;
    private BigDecimal externalMarksObtained;

    private BigDecimal practicalMarks;
    private BigDecimal passingPracticalMarks;
    private BigDecimal practicalMarksObtained;

    private BigDecimal otherMarks;
    private BigDecimal passingOtherMarks;
    private BigDecimal otherMarksObtained;

    private BigDecimal totalMarks;
    private BigDecimal passingTotalMarks;
    private BigDecimal totalMarksObtained;
    private String grade;
    private String result;
    @Enumerated(EnumType.STRING)
    private ResultStatus status = ResultStatus.ENTERED;
    private boolean published;
}