package com.tarento.upsmf.examsAndAdmissions.service;

import com.tarento.upsmf.examsAndAdmissions.model.ExamCycle;
import com.tarento.upsmf.examsAndAdmissions.repository.ExamCycleRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
@Slf4j
public class ExamCycleService {

    @Autowired
    private ExamCycleRepository repository;

    // Create a new exam cycle
    public ExamCycle createExamCycle(ExamCycle examCycle) {
        log.info("Creating new ExamCycle: {}", examCycle);
        examCycle.setObsolete(0);
        return repository.save(examCycle);
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
}