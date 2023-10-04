package com.tarento.upsmf.examsAndAdmissions.model;

import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "institute_user_mapping",indexes = {@Index(columnList = "user_id")})
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class InstituteUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @OneToOne
    @JoinColumn(name = "institute_id")
    private Institute institute;
}
