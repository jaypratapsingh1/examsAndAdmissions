package com.tarento.upsmf.examsAndAdmissions.model;

import com.tarento.upsmf.examsAndAdmissions.enums.RetotallingStatus;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "retotalling_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RetotallingRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "student_id")
    private Student student;

    @ManyToMany
    @JoinTable(
            name = "retotalling_exam",
            joinColumns = @JoinColumn(name = "retotalling_id"),
            inverseJoinColumns = @JoinColumn(name = "exam_id")
    )
    private List<Exam> exams;

    @Column(name = "request_date")
    private LocalDate requestDate;

    @Column(name = "status")
    private RetotallingStatus status;
    
    @Column(name = "remarks")
    private String remarks; // Any additional information or comments
}
