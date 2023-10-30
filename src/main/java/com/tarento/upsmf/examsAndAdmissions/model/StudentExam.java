package com.tarento.upsmf.examsAndAdmissions.model;

import lombok.*;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "student_exam_mapping", indexes = {@Index(columnList = "reference_no")})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class StudentExam implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "reference_no", nullable = false)
    private String referenceNo;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "student_id", referencedColumnName = "id")
    private Student student;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "exam_id", referencedColumnName = "id")
    private Exam exam;

    @Column(name = "amount", nullable = false, columnDefinition="Decimal(10,2) default 0.00")
    private Double amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, columnDefinition = "varchar(50) default 'INITIATED'")
    private Status status;

    @OneToOne
    @JoinColumn(name = "exam_cycle_id")
    private ExamCycle examCycle;

    public enum Status {
        INITIATED, PAID, FAILED, REFUND, ABANDONED
    }

    // add hall ticket reference here
}
