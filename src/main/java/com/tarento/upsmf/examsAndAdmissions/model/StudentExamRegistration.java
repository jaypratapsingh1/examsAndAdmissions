package com.tarento.upsmf.examsAndAdmissions.model;

import com.tarento.upsmf.examsAndAdmissions.enums.HallTicketStatus;
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
    private String createdBy;
    private String updatedBy;
    @ManyToOne
    @JoinColumn(name = "exam_center_id")
    private ExamCenter examCenter;
    private boolean isFeesPaid;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "institute_id")
    private Institute institute;
    private String hallTicketPath;
    @Column(name = "hall_ticket_status")
    @Enumerated(EnumType.STRING)
    private HallTicketStatus hallTicketStatus = HallTicketStatus.PENDING;
    private LocalDate hallTicketGenerationDate;
}
