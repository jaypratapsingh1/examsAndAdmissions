package com.tarento.upsmf.examsAndAdmissions.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "exam_fee", indexes = {@Index(columnList = "reference_no")})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExamFee {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "reference_no", nullable = false)
    private String referenceNo;

    @Column(name = "amount", nullable = false, columnDefinition="Decimal(10,2) default 0.00")
    private Double amount;

    @OneToOne
    @JoinColumn(name = "exam_cycle_id")
    private ExamCycle examCycle;

    @OneToOne
    @JoinColumn(name = "institute_id")
    private Institute institute;

    @OneToMany(targetEntity = StudentExam.class, mappedBy = "referenceNo", fetch = FetchType.EAGER)
    @Fetch(value = FetchMode.SUBSELECT)
    private List<StudentExam> studentExams;

    @Column(name = "created_by", nullable = false)
    private String createdBy;

    @Column(name = "created_on", nullable = false)
    @CreationTimestamp
    private LocalDateTime createdOn;

    @Column(name = "modified_by", nullable = false)
    private String modifiedBy;

    @Column(name = "modified_on", nullable = false)
    @UpdateTimestamp
    private LocalDateTime modifiedOn;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, columnDefinition = "varchar(50) default 'INITIATED'")
    private Status status;

    public enum Status {
        INITIATED, PAID, FAILED, REFUND, ABANDONED
    }
}
