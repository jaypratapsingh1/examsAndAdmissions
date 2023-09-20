package com.tarento.upsmf.examsAndAdmissions.model;

import lombok.*;

import javax.persistence.*;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
@Entity
@Table(name = "question_paper")
public class QuestionPaper {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "exam_cycle", referencedColumnName = "id")
    private ExamCycle examCycle;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "exam_id", referencedColumnName = "id")
    private Exam exam;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "course_id", referencedColumnName = "id")
    private Course course;

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "gcp_file_name")
    private String gcpFileName;

    @Column(name = "exam_date")
    private LocalDate examDate;

    @Column(name = "exam_start_time")
    private LocalTime examStartTime;

    @Column(name = "exam_cycle_name", nullable = false)
    private String examCycleName;

    @Column(name = "exam_cycle_id")
    private Long examCycleId;

    @Column(name = "exam_name")
    private String examName;

    @Column(name = "course_name")
    private String courseName;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "created_on")
    private Timestamp createdOn;

    @Column(name = "modified_by")
    private String modifiedBy;

    @Column(name = "modified_on")
    private Timestamp modifiedOn;

    @Column(name = "total_marks")
    private Long totalMarks;

    @Column(name = "internal_marks")
    private Long internalMarks;

    @Column(name = "internal_passing_marks")
    private Long internalPassingMarks;

    @Column(name = "external_marks")
    private Long externalMarks;

    @Column(name = "external_passing_marks")
    private Long externalPassingMarks;

    @Column(name = "passing_marks")
    private Long passingMarks;

    @Column(name = "obsolete", nullable = false, columnDefinition = "int default 0")
    private Integer obsolete = 0;

}
