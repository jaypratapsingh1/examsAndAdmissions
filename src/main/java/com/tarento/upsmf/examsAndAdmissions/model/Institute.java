package com.tarento.upsmf.examsAndAdmissions.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

@Entity
@Table(name = "institute")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class Institute implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String instituteName;
    private String instituteCode;
    private String address;
    private String email;
    @OneToMany(mappedBy = "institute")
    @JsonIgnore
    private List<Course> courses;
    private String ipAddress;
    private String remarks;
    private boolean cctvVerified;
    private boolean allowedForExamCentre;
    private String district;
    @OneToMany(mappedBy = "institute")
    private List<Student> students;
}
