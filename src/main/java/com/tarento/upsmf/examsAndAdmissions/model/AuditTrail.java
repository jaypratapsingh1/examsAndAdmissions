package com.tarento.upsmf.examsAndAdmissions.model;

import javax.persistence.*;
import java.time.LocalDateTime;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_trail")
public class AuditTrail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "action")
    private String action;

    @Column(name = "datetime")
    private LocalDateTime datetime;

    @Column(name = "user_name")
    private String userName;

    @ManyToOne
    @JoinColumn(name = "student_id")
    private Student student;

    @ManyToOne
    @JoinColumn(name = "admin_id") // Assuming you have a column named "admin_id" in AuditTrail table
    private Admin admin;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public LocalDateTime getDatetime() {
        return datetime;
    }

    public void setDatetime(LocalDateTime datetime) {
        this.datetime = datetime;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.datetime = timestamp;
    }

    public void setStudentId(Long studentId) {
        this.student = new Student();
        this.student.setId(studentId);
    }
}
