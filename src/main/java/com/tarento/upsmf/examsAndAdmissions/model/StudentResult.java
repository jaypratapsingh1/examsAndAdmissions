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

    @ManyToOne
    @JoinColumn(name = "student_id")
    private Student student;

    @JsonProperty("First Name")
    private String firstName;
    @JsonProperty("Last Name")
    private String lastName;
    @JsonProperty("Enrolment Number")
    private String enrollmentNumber;
    @JsonProperty("Mother's Name")
    private String motherName;
    @JsonProperty("Father's Name")
    private String fatherName;

    @ManyToOne
    @JoinColumn(name = "course_id")
    private Course course;
    @JsonProperty("Course")
    private String course_name;

    @ManyToOne
    @JoinColumn(name = "exam_cycle_id")
    private ExamCycle examCycle;
    @JsonProperty("Exam Cycle")
    private String examCycle_name;

    @ManyToOne
    @JoinColumn(name = "exam_id")
    private Exam exam;
    @JsonProperty("Exam")
    private String exam_name;

    @JsonProperty("Internal Marks")
    private BigDecimal internalMarks;
    @JsonProperty("Passing Internal Marks")
    private BigDecimal passingInternalMarks;
    @JsonProperty("Internal Marks Obtained")
    private BigDecimal internalMarksObtained;
    @JsonProperty("Practical Marks")
    private BigDecimal practicalMarks;
    @JsonProperty("Passing Practical Marks")
    private BigDecimal passingPracticalMarks;
    @JsonProperty("Practical Marks Obtained")
    private BigDecimal practicalMarksObtained;
    @JsonProperty("Other Marks")
    private BigDecimal otherMarks;
    @JsonProperty("Passing Other Marks")
    private BigDecimal passingOtherMarks;
    @JsonProperty("Other Marks Obtained")
    private BigDecimal otherMarksObtained;
    @JsonProperty("External Marks")
    private BigDecimal externalMarks;
    @JsonProperty("Passing External Marks")
    private BigDecimal passingExternalMarks;
    @JsonProperty("External Marks Obtained")
    private BigDecimal externalMarksObtained;
    @JsonProperty("Total Marks")
    private BigDecimal totalMarks;
    @JsonProperty("Passing Total Marks")
    private BigDecimal passingTotalMarks;
    @JsonProperty("Total Marks Obtained")
    private BigDecimal totalMarksObtained;
    @JsonProperty("Grade")
    private String grade;
    @JsonProperty("Result")
    private String result;

    @Enumerated(EnumType.STRING)
    private ResultStatus status = ResultStatus.ENTERED;

    private boolean published;

    public StudentResult(Student student, String firstName, String lastName, String enrollmentNumber,
                           String motherName, String fatherName, Course course, String course_name,
                           ExamCycle examCycle, String examCycle_name, Exam exam, String exam_name,
                           BigDecimal internalMarks, BigDecimal passingInternalMarks, BigDecimal internalMarksObtained,
                           BigDecimal practicalMarks, BigDecimal passingPracticalMarks, BigDecimal practicalMarksObtained,
                           BigDecimal otherMarks, BigDecimal passingOtherMarks, BigDecimal otherMarksObtained,
                           BigDecimal externalMarks, BigDecimal passingExternalMarks, BigDecimal externalMarksObtained,
                           BigDecimal totalMarks, BigDecimal passingTotalMarks, BigDecimal totalMarksObtained,
                           String grade, String result, ResultStatus status, boolean published) {

        this.student = student;
        this.firstName = firstName;
        this.lastName = lastName;
        this.enrollmentNumber = enrollmentNumber;
        this.motherName = motherName;
        this.fatherName = fatherName;
        this.course = course;
        this.course_name = course_name;
        this.examCycle = examCycle;
        this.examCycle_name = examCycle_name;
        this.exam = exam;
        this.exam_name = exam_name;
        this.internalMarks = internalMarks;
        this.passingInternalMarks = passingInternalMarks;
        this.internalMarksObtained = internalMarksObtained;
        this.practicalMarks = practicalMarks;
        this.passingPracticalMarks = passingPracticalMarks;
        this.practicalMarksObtained = practicalMarksObtained;
        this.otherMarks = otherMarks;
        this.passingOtherMarks = passingOtherMarks;
        this.otherMarksObtained = otherMarksObtained;
        this.externalMarks = externalMarks;
        this.passingExternalMarks = passingExternalMarks;
        this.externalMarksObtained = externalMarksObtained;
        this.totalMarks = totalMarks;
        this.passingTotalMarks = passingTotalMarks;
        this.totalMarksObtained = totalMarksObtained;
        this.grade = grade;
        this.result = result;
        this.status = status;
        this.published = published;
    }


}