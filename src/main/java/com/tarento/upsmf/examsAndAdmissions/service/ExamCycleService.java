package com.tarento.upsmf.examsAndAdmissions.service;

import com.tarento.upsmf.examsAndAdmissions.model.Course;
import com.tarento.upsmf.examsAndAdmissions.model.CourseDetails;
import com.tarento.upsmf.examsAndAdmissions.model.Exam;
import com.tarento.upsmf.examsAndAdmissions.model.ExamCycle;
import com.tarento.upsmf.examsAndAdmissions.model.dto.CourseDetailDTO;
import com.tarento.upsmf.examsAndAdmissions.model.dto.ExamCycleDTO;
import com.tarento.upsmf.examsAndAdmissions.model.dto.ExamDTO;
import com.tarento.upsmf.examsAndAdmissions.repository.CourseRepository;
import com.tarento.upsmf.examsAndAdmissions.repository.ExamCycleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ExamCycleService {

    @Autowired
    private ExamCycleRepository examCycleRepository;
    @Autowired
    private CourseRepository courseRepository;

    public ExamCycle createExamCycle(ExamCycleDTO examCycleDTO) {
        ExamCycle examCycle = convertToEntity(examCycleDTO);
        return examCycleRepository.save(examCycle);
    }
    private ExamCycle convertToEntity(ExamCycleDTO dto) {
        ExamCycle examCycle = new ExamCycle();
        examCycle.setStartDate(LocalDate.parse(dto.getStartDate()));
        examCycle.setEndDate(LocalDate.parse(dto.getEndDate()));

        List<CourseDetails> courseDetailsList = new ArrayList<>();
        for (CourseDetailDTO cdDto : dto.getCourseDetails()) {
            CourseDetails cd = new CourseDetails();
            Optional<Course> course = courseRepository.findById( cdDto.getCourseId());
            if (course == null) {
                throw new EntityNotFoundException("Course not found with ID: " + cdDto.getCourseId());
            }
            cd.setCourse(course.get());

            List<Exam> exams = new ArrayList<>();
            for (ExamDTO eDto : cdDto.getExams()) {
                Exam exam = new Exam();
                exam.setExamName(eDto.getExamName());
                exam.setExamDate(LocalDateTime.parse(eDto.getExamDate()));
                exam.setExamDuration(Duration.parse(eDto.getExamDuration()));
                exams.add(exam);
            }
            cd.setExams(exams);
            courseDetailsList.add(cd);
        }
        examCycle.setCourseDetails(courseDetailsList);

        return examCycle;
    }


    public List<ExamCycle> getAllExamCycles() {
        return examCycleRepository.findAll();
    }

    public ExamCycle getExamCycleById(Long id) {
        return examCycleRepository.findById(id).orElse(null);
    }
}
