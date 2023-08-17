package com.tarento.upsmf.examsAndAdmissions.model;

import com.tarento.upsmf.examsAndAdmissions.model.Course;
import com.tarento.upsmf.examsAndAdmissions.model.CourseDetails;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "exam_cycle")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class ExamCycle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String examCycleName;  // New field

    @ManyToOne
    @JoinColumn(name = "course_id")
    private Course course;  // Represents the Course-ID

    private LocalDate startDate;  // Represents start_date
    private LocalDate endDate;    // Represents end_date
    private String createdBy;
    private LocalDateTime createdOn;
    private String modifiedBy;
    private LocalDateTime modifiedOn;
    private String status;   // Can be "Publish" or "Draft"
    private Boolean isObsolete; // Represents Obselete [0/1]

    @OneToMany(mappedBy = "examCycle", cascade = CascadeType.ALL)
    private List<CourseDetails> courseDetails;

    // ... rest of your code ...
}
