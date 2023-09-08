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
    private boolean isCctvVerified;
    private int maxCapacity;
    private Integer totalSeats;
    private Integer occupiedSeats = 0; // default to 0
}
