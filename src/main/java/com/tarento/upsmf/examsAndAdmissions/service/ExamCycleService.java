package com.tarento.upsmf.examsAndAdmissions.service;

import com.tarento.upsmf.examsAndAdmissions.model.*;
import com.tarento.upsmf.examsAndAdmissions.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class ExamCycleService {

    @Autowired
    private ExamCycleRepository repository;
    @Autowired
    private ExamRepository examRepository;
    @Autowired
    private InstituteRepository instituteRepository;
    @Autowired
    private ExamCenterRepository examCenterRepository;
    @Autowired
    private CourseRepository courseRepository;

    // Create a new exam cycle
    public ExamCycle createExamCycle(ExamCycle examCycle) {
        log.info("Creating new ExamCycle: {}", examCycle);

        if (examCycle != null && examCycle.getId() == null) {
            examCycle.setObsolete(0);
            examCycle = repository.save(examCycle);
        }

        List<Institute> allInstitutes = instituteRepository.findAll();

        // Register each institute as a potential exam center for this exam cycle
        for (Institute institute : allInstitutes) {
            ExamCenter examCenter = new ExamCenter();
            examCenter.setInstitute(institute);
            examCenter.setExamCycle(examCycle);
            examCenter.setVerified(null); // marking as pending
            examCenter.setDistrict(institute.getDistrict());
            examCenter.setAddress(institute.getAddress());
            examCenter.setName(institute.getInstituteName());
            examCenterRepository.save(examCenter);
        }

        return examCycle;
    }

    // Fetch all active exam cycles
    public List<ExamCycle> getAllExamCycles() {
        log.info("Fetching all active ExamCycles...");
        return repository.findByObsolete(0);
    }

    // Fetch all soft-deleted exam cycles
    public List<ExamCycle> getAllObsoleteExamCycles() {
        log.info("Fetching all soft-deleted ExamCycles...");
        return repository.findByObsolete(1);
    }

    // Fetch a specific exam cycle by its ID
    public ExamCycle getExamCycleById(Long id) {
        log.info("Fetching ExamCycle by ID: {}", id);
        return repository.findByIdAndObsolete(id, 0).orElse(null);
    }

    // Update an existing exam cycle
    public ExamCycle updateExamCycle(Long id, ExamCycle updatedExamCycle) {
        log.info("Updating ExamCycle with ID: {}", id);
        ExamCycle existingExamCycle = repository.findById(id).orElse(null);
        if (existingExamCycle != null) {
            existingExamCycle.setExamCycleName(updatedExamCycle.getExamCycleName());
            existingExamCycle.setCourseId(updatedExamCycle.getCourseId());
            existingExamCycle.setStartDate(updatedExamCycle.getStartDate());
            existingExamCycle.setEndDate(updatedExamCycle.getEndDate());
            existingExamCycle.setStatus(updatedExamCycle.getStatus());

            // Update auditing metadata
            // Assuming you have a way to fetch the current user, e.g., a utility method
            existingExamCycle.setModifiedBy(updatedExamCycle.getModifiedBy());
            existingExamCycle.setModifiedOn(updatedExamCycle.getModifiedOn()); // Current date/time

            return repository.save(existingExamCycle);
        }
        log.warn("ExamCycle with ID: {} not found!", id);
        return null;
    }

    // Soft delete an exam cycle
    public void deleteExamCycle(Long id) {
        log.info("Soft-deleting ExamCycle with ID: {}", id);
        ExamCycle examCycle = repository.findById(id).orElse(null);
        if (examCycle != null) {
            examCycle.setObsolete(1);
            repository.save(examCycle);
        } else {
            log.warn("ExamCycle with ID: {} not found for deletion!", id);
        }
    }

    // Restore a soft-deleted exam cycle
    public void restoreExamCycle(Long id) {
        log.info("Restoring soft-deleted ExamCycle with ID: {}", id);
        ExamCycle examCycle = repository.findById(id).orElse(null);
        if (examCycle != null && examCycle.getObsolete() == 1) {
            examCycle.setObsolete(0);
            repository.save(examCycle);
        } else {
            log.warn("ExamCycle with ID: {} not found for restoration!", id);
        }
    }
    public ExamCycle addExamsToCycle(Long id, List<Exam> exams) {
        ExamCycle examCycle = repository.findById(id).orElse(null);
        if (examCycle != null) {
            for (Exam exam : exams) {
                Optional<Exam> existingExam = examRepository.findByExamNameAndExamDateAndStartTimeAndEndTime(
                        exam.getExamName(), exam.getExamDate(), exam.getStartTime(), exam.getEndTime());
                if (existingExam.isPresent()) {
                    throw new RuntimeException("Exam already exists with same details: " + exam.getExamName());
                }
                // Fetch the course using courseId
                Course course = courseRepository.findById(exam.getCourse().getId()).orElse(null);
                if (course == null) {
                    // If course doesn't exist, return error or handle it
                    throw new RuntimeException("Course not found with ID: " + exam.getCourse().getId());
                }
                // Link the exam to the course
                exam.setCourse(course);

                // Convert time strings to LocalTime
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
                exam.setStartTime(LocalTime.parse(exam.getStartTime().format(formatter)));
                exam.setEndTime(LocalTime.parse(exam.getEndTime().format(formatter)));

                // Link exam to exam cycle
                exam.setExamCycleId(examCycle.getId());

                examRepository.save(exam);
            }
            return examCycle;
        }
        return null;
    }

    public ExamCycle removeExamFromCycle(Long id, Exam exam) {
        ExamCycle examCycle = repository.findById(id).orElse(null);
        if(examCycle != null) {
            exam.setExamCycleId(null);
            examRepository.save(exam);
            return examCycle;
        }
        return null;
    }
}