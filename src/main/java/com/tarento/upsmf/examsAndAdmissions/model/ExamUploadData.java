package com.tarento.upsmf.examsAndAdmissions.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.tarento.upsmf.examsAndAdmissions.util.LocalDateDeserializer;
import com.tarento.upsmf.examsAndAdmissions.util.LocalTimeDeserializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;


@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "examUpload") // Define the table name
public class ExamUploadData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "examcycle_name")
    @JsonProperty("Examcycle Name")
    private String examcycleName;

    @Column(name = "course")
    @JsonProperty("Course")
    private String course;

    @Column(name = "start_date")
    @JsonProperty("Start Date")
    @JsonFormat(pattern = "dd.MM.yyyy")
    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDate startDate;

    @Column(name = "end_date")
    @JsonProperty("End Date")
    @JsonFormat(pattern = "dd.MM.yyyy")
    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDate endDate;

    @Column(name = "exam_name")
    @JsonProperty("Exam Name")
    private String examName;

    @Column(name = "date")
    @JsonProperty("Date")
    @JsonFormat(pattern = "dd.MM.yyyy")
    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDate date;

    @Column(name = "start_time")
    @JsonProperty("Start Time")
    @JsonDeserialize(using = LocalTimeDeserializer.class)
    private LocalTime startTime;

    @Column(name = "end_time")
    @JsonProperty("End Time")
    @JsonDeserialize(using = LocalTimeDeserializer.class)
    private LocalTime endTime;

    @Column(name = "maximum_marks")
    @JsonProperty("Maximum Marks")
    private Integer maximumMarks;
}

