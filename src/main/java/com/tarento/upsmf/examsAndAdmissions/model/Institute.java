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
@ToString(exclude = {"courses", "students", "registrations"})
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
    private boolean allowedForExamCentre;
    private String district;
    @OneToMany(mappedBy = "institute")
    @JsonManagedReference
    private List<Student> students;
    @OneToMany(mappedBy = "institute")
    @JsonBackReference // To prevent infinite recursion
    private List<StudentExamRegistration> registrations;
}
