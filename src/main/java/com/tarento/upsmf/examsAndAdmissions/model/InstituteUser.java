package com.tarento.upsmf.examsAndAdmissions.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import javax.persistence.*;

@Entity
@Table(name = "institute_user_mapping",indexes = {@Index(columnList = "user_id")})
@Data
@AllArgsConstructor
@ToString
@Builder
public class InstituteUser {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @OneToOne
    @JoinColumn(name = "institute_id")
    private Institute institute;
}
