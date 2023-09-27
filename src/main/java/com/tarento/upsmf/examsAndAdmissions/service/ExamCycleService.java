package com.tarento.upsmf.examsAndAdmissions.service;

import com.tarento.upsmf.examsAndAdmissions.enums.ExamCycleStatus;
import com.tarento.upsmf.examsAndAdmissions.model.*;
import com.tarento.upsmf.examsAndAdmissions.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.tarento.upsmf.examsAndAdmissions.model.dto.ExamCycleDTO;

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
    public ExamCycle createExamCycle(ExamCycle examCycle, String userId) {
        // Check if an ExamCycle with the same details already exists
        ExamCycle existingExamCycle = repository.findByExamCycleNameAndCourseAndStartDateAndEndDate(
                examCycle.getExamCycleName(),
                examCycle.getCourse(),
                examCycle.getStartDate(),
                examCycle.getEndDate()
        );

        if (existingExamCycle != null) {
            // If an ExamCycle with the same details exists, return it
            return existingExamCycle;
        }

        // Create a new ExamCycle
        log.info("Creating new ExamCycle: {}", examCycle);
        Course course = courseRepository.findById(examCycle.getCourse().getId())
                .orElseThrow(() -> new RuntimeException("Course not found"));

        examCycle.setObsolete(0);
        examCycle.setCreatedOn(LocalDateTime.now());
        examCycle.setStatus(ExamCycleStatus.DRAFT);
        examCycle.setCreatedBy(userId);
        examCycle.setCourse(course);
        examCycle = repository.save(examCycle);

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
    public List<ExamCycleDTO> getAllExamCycles() {
        log.info("Fetching all active ExamCycles...");
        List<ExamCycle> examCycles = repository.findByObsolete(0);
        return examCycles.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
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
    public ExamCycle updateExamCycle(Long id, ExamCycle updatedExamCycle, String userId) {
        log.info("Updating ExamCycle with ID: {}", id);
        ExamCycle existingExamCycle = repository.findById(id).orElse(null);
        if (existingExamCycle != null) {
            existingExamCycle.setExamCycleName(updatedExamCycle.getExamCycleName());
            existingExamCycle.setCourse(updatedExamCycle.getCourse());
            existingExamCycle.setStartDate(updatedExamCycle.getStartDate());
            existingExamCycle.setEndDate(updatedExamCycle.getEndDate());
            //existingExamCycle.setStatus(updatedExamCycle.getStatus());

            // Update auditing metadata
            // Assuming you have a way to fetch the current user, e.g., a utility method
            existingExamCycle.setModifiedBy(userId);
            existingExamCycle.setModifiedOn(LocalDateTime.now()); // Current date/time

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
    public ExamCycle addExamsToCycle(Long id, List<Exam> exams, String userId) {
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
                exam.setCreatedBy(userId);
                exam.setCreatedOn(LocalDateTime.now());

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
    public ExamCycle publishExamCycle(Long id) {
        Optional<ExamCycle> optionalExamCycle = repository.findById(id);
        if(!optionalExamCycle.isPresent()) {
            return null;
        }
        ExamCycle examCycle = optionalExamCycle.get();
        examCycle.setStatus(ExamCycleStatus.PUBLISHED);
        return repository.save(examCycle);
    }
    public ExamCycleDTO toDTO(ExamCycle examCycle) {
        ExamCycleDTO dto = new ExamCycleDTO();
        dto.setId(examCycle.getId());
        dto.setExamCycleName(examCycle.getExamCycleName());

        // Set Course related fields
        if (examCycle.getCourse() != null) {
            dto.setCourseId(examCycle.getCourse().getId());
            dto.setCourseCode(examCycle.getCourse().getCourseCode());
            dto.setCourseName(examCycle.getCourse().getCourseName());  // Assuming your Course entity has a getCourseName() method
        }

        dto.setStartDate(examCycle.getStartDate());
        dto.setEndDate(examCycle.getEndDate());
        dto.setCreatedBy(examCycle.getCreatedBy());
        dto.setCreatedOn(examCycle.getCreatedOn());
        dto.setModifiedBy(examCycle.getModifiedBy());
        dto.setModifiedOn(examCycle.getModifiedOn());
        dto.setStatus(examCycle.getStatus());
        dto.setObsolete(examCycle.getObsolete());

        return dto;
    }


}