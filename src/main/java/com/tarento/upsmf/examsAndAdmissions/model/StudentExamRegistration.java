package com.tarento.upsmf.examsAndAdmissions.model;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "student_exam_registration")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class StudentExamRegistration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "student_id")
    private Student student;

    @ManyToOne
    @JoinColumn(name = "exam_id")
    private Exam exam;

    @ManyToOne
    @JoinColumn(name = "exam_cycle_id")
    private ExamCycle examCycle;

    private LocalDate registrationDate;
    private String status;
    private String remarks;
    private String createdBy;
    private String updatedBy;
}
