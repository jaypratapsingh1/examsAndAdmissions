package com.tarento.upsmf.examsAndAdmissions.model;

import com.fasterxml.jackson.annotation.JsonProperty;
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

    // Associations with Student entity
    @ManyToOne
    @JoinColumn(name = "student_id")
    private Student student;

    // New Fields

    private String firstName;
    private String lastName;
    private String enrollmentNumber;
    private String motherName;
    private String fatherName;

    // Associations with Course, ExamCycle, and Exam entities
    @ManyToOne
    @JoinColumn(name = "course_id")
    private Course course;
    @JsonProperty("Course")
    private String courseValue;

    @ManyToOne
    @JoinColumn(name = "exam_cycle_id")
    private ExamCycle examCycle;
    @JsonProperty("Exam Cycle")
    private String examCycleValue;

    @ManyToOne
    @JoinColumn(name = "exam_id")
    private Exam exam;
    @JsonProperty("Exam")
    private String examValue;

    // Fields from your previous message
    private BigDecimal internalMarks;
    private BigDecimal passingInternalMarks;
    private BigDecimal internalMarksObtained;

    private BigDecimal practicalMarks;
    private BigDecimal passingPracticalMarks;
    private BigDecimal practicalMarksObtained;

    private BigDecimal otherMarks;
    private BigDecimal passingOtherMarks;
    private BigDecimal otherMarksObtained;

    private BigDecimal externalMarks;
    private BigDecimal passingExternalMarks;
    private BigDecimal externalMarksObtained;

    private BigDecimal totalMarks;
    private BigDecimal passingTotalMarks;
    private BigDecimal totalMarksObtained;

    private String grade;
    private String result;

    @Enumerated(EnumType.STRING)
    private ResultStatus status = ResultStatus.ENTERED;

    private boolean published;
}