package com.tarento.upsmf.examsAndAdmissions.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDate;

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

    private LocalDate dispatchDate;

    private String dispatchProofFileLocation;

    // Constructors, getters, setters, etc.
}
