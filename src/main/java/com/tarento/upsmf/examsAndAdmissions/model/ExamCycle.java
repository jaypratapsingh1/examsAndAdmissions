package com.tarento.upsmf.examsAndAdmissions.model;

import com.tarento.upsmf.examsAndAdmissions.enums.ExamCycleStatus;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "exam_cycle")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class ExamCycle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "exam_cycle_name", nullable = false)
    private String examCycleName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_code")
    private Course course;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "created_on")
    private LocalDateTime createdOn;

    @Column(name = "modified_by")
    private String modifiedBy;

    @Column(name = "modified_on")
    private LocalDateTime modifiedOn;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private ExamCycleStatus status;

    @Column(name = "obsolete", nullable = false, columnDefinition = "int default 0")
    private Integer obsolete = 0;
}
