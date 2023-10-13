package com.tarento.upsmf.examsAndAdmissions.model;

import com.tarento.upsmf.examsAndAdmissions.enums.DispatchStatus;
import com.tarento.upsmf.examsAndAdmissions.util.Constants;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.Date;

@Entity
@Table(name = "dispatch_tracker")
@Getter
@Setter
public class DispatchTracker {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "exam_cycle_id")
    private ExamCycle examCycle;

    @ManyToOne
    @JoinColumn(name = "exam_id")
    private Exam exam;

    @ManyToOne
    @JoinColumn(name = "exam_center_id")
    private ExamCenter examCenter;

    private LocalDate dispatchDate;

    private String dispatchProofFileLocation;

    @Column(name = "dispatch_status")
    @Enumerated(EnumType.STRING)
    private DispatchStatus dispatchStatus;
    private LocalDate dispatchLastDate;
}
