package com.tarento.upsmf.examsAndAdmissions.service.impl;

import com.tarento.upsmf.examsAndAdmissions.model.Exam;
import com.tarento.upsmf.examsAndAdmissions.model.ResponseDto;
import com.tarento.upsmf.examsAndAdmissions.repository.ExamRepository;
import com.tarento.upsmf.examsAndAdmissions.service.ExamService;
import com.tarento.upsmf.examsAndAdmissions.util.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
@Slf4j
public class ExamServiceImpl implements ExamService {

    @Autowired
    private ExamRepository examRepository;

    @Override
    public ResponseDto createExam(Exam exam, String userId) {
        ResponseDto response = new ResponseDto(Constants.API_EXAM_ADD);
        try {
            log.info("Creating new Exam: {}", exam);
            exam.setObsolete(0);
            exam.setCreatedOn(LocalDateTime.now());
            exam.setCreatedBy(userId);
            examRepository.save(exam);
            response.put(Constants.MESSAGE, "Exam created successfully.");
            response.put(Constants.RESPONSE, exam);
            response.setResponseCode(HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error saving exam details", e);
            ResponseDto.setErrorResponse(response, "EXAM_CREATION_FAILED", "Error saving exam details.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return response;
    }

    @Override
    public ResponseDto getAllExams() {
        ResponseDto response = new ResponseDto(Constants.API_EXAM_GET_ALL);
        // Fetch exams where obsolete is set to 0 (active exams)
        List<Exam> exams = examRepository.findByObsolete(0);
        if (!exams.isEmpty()) {
            response.put(Constants.MESSAGE, "Active exams retrieved successfully.");
            response.put(Constants.RESPONSE, exams);
            response.setResponseCode(HttpStatus.OK);
        } else {
            ResponseDto.setErrorResponse(response, "NO_ACTIVE_EXAMS_FOUND", "No active exams found.", HttpStatus.NOT_FOUND);
        }
        return response;
    }

    @Override
    public ResponseDto getExamById(Long id) {
        ResponseDto response = new ResponseDto(Constants.API_EXAM_GET_BY_ID);
        Optional<Exam> examOptional = examRepository.findById(id);
        if (examOptional.isPresent()) {
            Exam exam = examOptional.get();
            if (exam.getObsolete() == 0) { // Check if exam is active
                response.put(Constants.MESSAGE, "Active exam retrieved successfully.");
                response.put(Constants.RESPONSE, exam);
                response.setResponseCode(HttpStatus.OK);
            } else {
                ResponseDto.setErrorResponse(response, "EXAM_IS_OBSOLETE", "Exam is marked as obsolete.", HttpStatus.NOT_FOUND);
            }
        } else {
            ResponseDto.setErrorResponse(response, "EXAM_NOT_FOUND", "Exam id not found.", HttpStatus.NOT_FOUND);
        }
        return response;
    }

    @Override
    public ResponseDto deleteExam(Long id) {
        ResponseDto response = new ResponseDto(Constants.API_EXAM_DELETE);
        log.info("Soft-deleting Exam with ID: {}", id);
        try {
            Exam exam = examRepository.findById(id).orElse(null);
            if (exam != null) {
                exam.setObsolete(1);
                examRepository.save(exam);
                response.put(Constants.MESSAGE, "Exam deleted successfully.");
                response.put(Constants.RESPONSE, "Exam with ID: " + id + " has been soft-deleted.");
                response.setResponseCode(HttpStatus.OK); // Changed this to OK as it's a successful operation
            } else {
                log.warn("Exam with ID: {} not found for deletion!", id);
                ResponseDto.setErrorResponse(response, "EXAM_NOT_FOUND", "Exam with ID: " + id + " not found for deletion.", HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            log.error("Error deleting exam with ID: {}", id, e);
            ResponseDto.setErrorResponse(response, "INTERNAL_SERVER_ERROR", "Error deleting exam.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return response;
    }

    @Override
    public ResponseDto updateExam(Long id, Exam exam, String userId) {
        ResponseDto response = new ResponseDto(Constants.API_EXAM_UPDATE);
        log.info("Updating Exam with ID: {}", id);
        Exam existingExam = examRepository.findById(id).orElse(null);
        if (existingExam != null) {
            existingExam.setExamCycleId(exam.getExamCycleId());
            existingExam.setExamName(exam.getExamName());
            existingExam.setExamDate(exam.getExamDate());
            existingExam.setStartTime(exam.getStartTime());
            existingExam.setEndTime(exam.getEndTime());
            existingExam.setAmount(exam.getAmount());
            // Update auditing metadata from the payload
            existingExam.setModifiedBy(userId);
            existingExam.setModifiedOn(LocalDateTime.now());

            // Soft delete or status flag, if you want to allow it from the payload:
            existingExam.setObsolete(exam.getObsolete());

            examRepository.save(existingExam); // Make sure to save the updated exam

            response.put(Constants.MESSAGE, "Exam updated successfully.");
            response.put(Constants.RESPONSE, "Exam with ID: " + id + " has been updated.");
            response.setResponseCode(HttpStatus.OK);
        } else {
            log.warn("Exam with ID: {} not found!", id);
            ResponseDto.setErrorResponse(response, "EXAM_NOT_FOUND", "Exam with ID: " + id + " not found.", HttpStatus.NOT_FOUND);
        }
        return response;
    }

    @Override
    public ResponseDto restoreExam(Long id) {
        ResponseDto response = new ResponseDto(Constants.API_EXAM_RESTORE);
        log.info("Restoring soft-deleted Exam with ID: {}", id);
        Exam exam = examRepository.findById(id).orElse(null);

        if (exam != null && exam.getObsolete() == 1) {
            exam.setObsolete(0);
            examRepository.save(exam);
            response.put(Constants.MESSAGE, "Exam restored successfully.");
            response.put(Constants.RESPONSE, "Exam with ID: " + id + " has been restored.");
            response.setResponseCode(HttpStatus.OK);
        } else if (exam != null) {
            log.warn("Exam with ID: {} is not soft-deleted!", id);
            ResponseDto.setErrorResponse(response, "EXAM_NOT_SOFT_DELETED", "Exam with ID: " + id + " is not soft-deleted and therefore cannot be restored.", HttpStatus.BAD_REQUEST);
        } else {
            log.warn("Exam with ID: {} not found for restoration!", id);
            ResponseDto.setErrorResponse(response, "EXAM_NOT_FOUND", "Exam with ID: " + id + " not found.", HttpStatus.NOT_FOUND);
        }

        return response;
    }

    @Override
    public ResponseDto publishExamResults(Long examId) {
        ResponseDto response = new ResponseDto(Constants.API_EXAM_PUBLISH_RESULTS);

        try {
            log.info("Publishing results for Exam with ID: {}", examId);
            Exam exam = examRepository.findById(examId).orElse(null);

            if (exam != null) {
                exam.setIsResultsPublished(true);
                examRepository.save(exam);
                response.put(Constants.MESSAGE, "Exam results published successfully.");
                response.put(Constants.RESPONSE, "Results for Exam with ID: " + examId + " have been published.");
                response.setResponseCode(HttpStatus.OK);
            } else {
                log.warn("Exam with ID: {} not found!", examId);
                ResponseDto.setErrorResponse(response, "EXAM_NOT_FOUND", "Exam with ID: " + examId + " not found.", HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            log.error("Error while publishing results for Exam with ID: {}", examId, e);
            ResponseDto.setErrorResponse(response, "PUBLISH_FAILED", "Failed to publish results for Exam with ID: " + examId, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return response;
    }

    public ResponseDto findByExamCycleId(Long examCycleId) {
        ResponseDto responseDto = new ResponseDto(Constants.API_EXAM_FIND_BY_CYCLE);
        try {
            log.info("Finding Exams by ExamCycle ID: {}", examCycleId);
            List<Exam> exams = examRepository.findAllByExamCycleIdAndObsolete(examCycleId, 0);

            if (exams != null && !exams.isEmpty()) {
                Map<String, Object> resultData = new HashMap<>();
                resultData.put("response", exams);
                resultData.put("message", "Successfully retrieved exams");
                responseDto.setResult(resultData);
                responseDto.setResponseCode(HttpStatus.OK);
            } else {
                ResponseDto.setErrorResponse(responseDto, "NO_EXAMS_FOUND", "No active exams found for the given ExamCycle ID", HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            log.error("Error while finding Exams by ExamCycle ID: {}", examCycleId, e);
            ResponseDto.setErrorResponse(responseDto, "INTERNAL_SERVER_ERROR", "Error retrieving exams", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return responseDto;
    }
}
