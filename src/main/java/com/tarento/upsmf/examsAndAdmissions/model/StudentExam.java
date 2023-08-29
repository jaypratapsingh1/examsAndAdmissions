package com.tarento.upsmf.examsAndAdmissions.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "student_exam_mapping", indexes = {@Index(columnList = "reference_no")})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentExam implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "reference_no", nullable = false)
    private String referenceNo;

    @OneToOne(targetEntity = Student.class, mappedBy = "student_id")
    private Student student;

    @ManyToOne
    @JoinColumn(name = "exam_id")
    private Exam exam;

    @Column(name = "amount", nullable = false, columnDefinition="Decimal(10,2) default 0.00")
    private Double amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, columnDefinition = "varchar(50) default 'INITIATED'")
    private Status status;

    public enum Status {
        INITIATED, PAID, FAILED, REFUND, ABANDONED
    }

    // add hall ticket reference here
}
