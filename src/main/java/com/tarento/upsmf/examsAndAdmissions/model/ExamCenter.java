package com.tarento.upsmf.examsAndAdmissions.model;

import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "exam_center")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class ExamCenter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String address;
    private Boolean verified;
    //private int maxCapacity;
    //private Integer totalSeats;
    //private Integer occupiedSeats = 0; // default to 0
    @ManyToOne
    @JoinColumn(name = "institute_id")
    private Institute institute;

    @ManyToOne
    @JoinColumn(name = "exam_cycle_id")
    private ExamCycle examCycle;

    private String ipAddress;
    private String remarks;
    private boolean allowedForExamCentre;
    private String district;
    private String instituteCode;

}
