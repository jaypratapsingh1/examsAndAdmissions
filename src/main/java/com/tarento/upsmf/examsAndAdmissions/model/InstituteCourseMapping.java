package com.tarento.upsmf.examsAndAdmissions.model;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;

@Entity
@Table(name = "institute_course_mapping")
@Data
public class InstituteCourseMapping implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "seat_capacity")
    private Long seatCapacity;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "institute_id", referencedColumnName = "id")
    private Institute institute;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "course_id", referencedColumnName = "id")
    private Course course;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "created_on")
    private Timestamp createdOn;

    @Column(name = "modified_by")
    private String modifiedBy;

    @Column(name = "modified_on")
    private Timestamp modifiedOn;

    @Column(name = "obsolete", nullable = false, columnDefinition = "int default 0")
    private Integer obsolete = 0;

}
