package com.tarento.upsmf.examsAndAdmissions.service;

import com.tarento.upsmf.examsAndAdmissions.model.Exam;
import com.tarento.upsmf.examsAndAdmissions.repository.ExamRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class ExamService {

    @Autowired
    private ExamRepository repository;

    public Exam createExam(Exam exam) {
        log.info("Creating new Exam: {}", exam);
        exam.setObsolete(0);
        return repository.save(exam);
    }

    public List<Exam> getAllExams() {
        log.info("Fetching all active Exams...");
        return repository.findByObsolete(0);
    }

    public Exam getExamById(Long id) {
        log.info("Fetching Exam by ID: {}", id);
        return repository.findByIdAndObsolete(id, 0).orElse(null);
    }

    public Exam updateExam(Long id, Exam updatedExam) {
        log.info("Updating Exam with ID: {}", id);
        Exam existingExam = repository.findById(id).orElse(null);
        if (existingExam != null) {
            existingExam.setExamCycleId(updatedExam.getExamCycleId());
            existingExam.setExamDate(updatedExam.getExamDate());

            // Update auditing metadata from the payload
            existingExam.setModifiedBy(updatedExam.getModifiedBy());
            existingExam.setModifiedOn(updatedExam.getModifiedOn());

            // Soft delete or status flag, if you want to allow it from the payload:
            existingExam.setObsolete(updatedExam.getObsolete());

            return repository.save(existingExam);
        }
        log.warn("Exam with ID: {} not found!", id);
        return null;
    }



    public void deleteExam(Long id) {
        log.info("Soft-deleting Exam with ID: {}", id);
        Exam exam = repository.findById(id).orElse(null);
        if (exam != null) {
            exam.setObsolete(1);
            repository.save(exam);
        } else {
            log.warn("Exam with ID: {} not found for deletion!", id);
        }
    }

    public void restoreExam(Long id) {
        log.info("Restoring soft-deleted Exam with ID: {}", id);
        Exam exam = repository.findById(id).orElse(null);
        if (exam != null && exam.getObsolete() == 1) {
            exam.setObsolete(0);
            repository.save(exam);
        } else {
            log.warn("Exam with ID: {} not found for restoration!", id);
        }
    }
}