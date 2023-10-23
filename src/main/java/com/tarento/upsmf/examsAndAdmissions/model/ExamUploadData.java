package com.tarento.upsmf.examsAndAdmissions.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.tarento.upsmf.examsAndAdmissions.util.CustomDateDeserializer;
import com.tarento.upsmf.examsAndAdmissions.util.LocalDateDeserializer;
import com.tarento.upsmf.examsAndAdmissions.util.LocalTimeDeserializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Date;


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

    @JsonProperty("Start Date")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd.MM.yyyy")
    @JsonDeserialize(using = CustomDateDeserializer.class)
    private Date startDate;

    @JsonProperty("End Date")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd.MM.yyyy")
    @JsonDeserialize(using = CustomDateDeserializer.class)
    private Date endDate;

    @JsonProperty("Exam Name")
    private String examName;

    @JsonProperty("Date")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd.MM.yyyy")
    @JsonDeserialize(using = CustomDateDeserializer.class)
    private Date date;

    @JsonProperty("Start Time")
    @JsonDeserialize(using = LocalTimeDeserializer.class)
    private LocalTime startTime;

    @JsonProperty("End Time")
    @JsonDeserialize(using = LocalTimeDeserializer.class)
    private LocalTime endTime;

    @Column(name = "maximum_marks")
    @JsonProperty("Maximum Marks")
    private Integer maximumMarks;
}

