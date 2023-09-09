package com.tarento.upsmf.examsAndAdmissions.service.impl;

import com.tarento.upsmf.examsAndAdmissions.model.Exam;
import com.tarento.upsmf.examsAndAdmissions.model.ResponseDto;
import com.tarento.upsmf.examsAndAdmissions.repository.ExamRepository;
import com.tarento.upsmf.examsAndAdmissions.service.ExamService;
import com.tarento.upsmf.examsAndAdmissions.util.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ExamServiceImpl implements ExamService {

    private static final Logger logger = LoggerFactory.getLogger(ExamServiceImpl.class);
    @Autowired
    private ExamRepository examRepository;

    @Override
    public ResponseDto createExam(Exam exam) {
        ResponseDto response = new ResponseDto(Constants.API_EXAM_ADD);
        try {
            logger.info("Creating new Exam: {}", exam);
            exam.setObsolete(0);
            examRepository.save(exam);
            response.put(Constants.MESSAGE, Constants.SUCCESSFUL);
            response.put(Constants.RESPONSE, "Successfully created exam");
            response.setResponseCode(HttpStatus.OK);
        } catch (Exception e) {
            response.put(Constants.MESSAGE, "Error saving exam details");
            response.put(Constants.RESPONSE, Constants.FAILUREMESSAGE);
            response.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return response;
    }
    @Override
    public ResponseDto getAllExams() {
        ResponseDto response = new ResponseDto(Constants.API_EXAM_GET_ALL);
        logger.info("Fetching all active Exams...");
        List<Exam> exams = examRepository.findAll();
        if (exams.isEmpty()) {
            response.put(Constants.MESSAGE, "Error fetching exam details");
            response.put(Constants.RESPONSE, Constants.FAILUREMESSAGE);
            response.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR);
        } else {
            response.put(Constants.MESSAGE, Constants.SUCCESSFUL);
            response.put(Constants.RESPONSE, exams);
            response.setResponseCode(HttpStatus.OK);
        }
        return response;
    }
    @Override
    public ResponseDto getExamById(Long id) {
        ResponseDto response = new ResponseDto(Constants.API_EXAM_GET_BY_ID);
        logger.info("Fetching Exam by ID: {}", id);
        Optional<Exam> examOptional = examRepository.findById(id);
        if (examOptional.isPresent()) {
            Exam exam = examOptional.get();
            if (exam.getObsolete() == 0) {
                response.put(Constants.MESSAGE, Constants.SUCCESSFUL);
                response.put(Constants.RESPONSE, exam);
                response.setResponseCode(HttpStatus.OK);
            } else {
                response.put(Constants.MESSAGE, "Error saving fee details");
                response.put(Constants.RESPONSE, Constants.FAILUREMESSAGE);
                response.setResponseCode(HttpStatus.NOT_FOUND);
            }
        }
            response.put(Constants.MESSAGE, "Error saving fee details");
            response.put(Constants.RESPONSE, Constants.FAILUREMESSAGE);
            response.setResponseCode(HttpStatus.NOT_FOUND);
        return response;
    }
    @Override
    public ResponseDto deleteExam(Long id) {
        ResponseDto response = new ResponseDto(Constants.API_EXAM_DELETE);
        logger.info("Soft-deleting Exam with ID: {}", id);
        try {
            Exam exam = examRepository.findById(id).orElse(null);
            if (exam != null) {
                exam.setObsolete(1);
                examRepository.save(exam);
                response.put(Constants.MESSAGE, Constants.SUCCESSFUL);
                response.put(Constants.RESPONSE, Constants.MESSAGE);
                response.setResponseCode(HttpStatus.NO_CONTENT);
            } else {
                logger.warn("Exam with ID: {} not found for deletion!", id);
                response.put(Constants.MESSAGE, Constants.SUCCESSFUL);
                response.put(Constants.RESPONSE, Constants.MESSAGE);
                response.setResponseCode(HttpStatus.NOT_FOUND);
            }
        }catch (Exception e) {
            response.put(Constants.MESSAGE, Constants.SUCCESSFUL);
            response.put(Constants.RESPONSE, Constants.MESSAGE);
            response.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return response;
    }

    @Override
    public ResponseDto updateExam(Long id, Exam exam) {
        ResponseDto response = new ResponseDto(Constants.API_EXAM_UPDATE);
        logger.info("Updating Exam with ID: {}", id);
        Exam existingExam = examRepository.findById(id).orElse(null);
        if (existingExam != null) {
            existingExam.setExamCycleId(exam.getExamCycleId());
            existingExam.setExamName(exam.getExamName());
            existingExam.setExamDate(exam.getExamDate());
            existingExam.setStartTime(exam.getStartTime());
            existingExam.setEndTime(exam.getEndTime());
            // Update auditing metadata from the payload
            existingExam.setModifiedBy(exam.getModifiedBy());
            existingExam.setModifiedOn(exam.getModifiedOn());

            // Soft delete or status flag, if you want to allow it from the payload:
            existingExam.setObsolete(exam.getObsolete());

            response.put(Constants.MESSAGE, Constants.SUCCESSFUL);
            response.put(Constants.RESPONSE, exam);
            response.setResponseCode(HttpStatus.OK);
        }
        logger.warn("Exam with ID: {} not found!", id);
        response.put(Constants.MESSAGE, "Exam id not found");
        response.put(Constants.RESPONSE, Constants.FAILUREMESSAGE);
        response.setResponseCode(HttpStatus.NOT_FOUND);
        return response;
    }

    @Override
    public ResponseDto restoreExam(Long id) {
        ResponseDto response = new ResponseDto(Constants.API_EXAM_RESTORE);
        try {
            logger.info("Restoring soft-deleted Exam with ID: {}", id);
            Exam exam = examRepository.findById(id).orElse(null);
            if (exam != null && exam.getObsolete() == 1) {
                exam.setObsolete(0);
                examRepository.save(exam);
                response.put(Constants.MESSAGE, Constants.SUCCESSFUL);
                response.put(Constants.RESPONSE, exam);
                response.setResponseCode(HttpStatus.OK);
            } else {
                logger.warn("Exam with ID: {} not found for restoration!", id);
                response.put(Constants.MESSAGE, "Exam id not found");
                response.put(Constants.RESPONSE, Constants.FAILUREMESSAGE);
                response.setResponseCode(HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            response.put(Constants.MESSAGE, "Exam id not found");
            response.put(Constants.RESPONSE, Constants.FAILUREMESSAGE);
            response.setResponseCode(HttpStatus.NOT_FOUND);
        }
        return response;
    }
}
