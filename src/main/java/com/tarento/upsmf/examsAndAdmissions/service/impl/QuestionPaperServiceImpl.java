package com.tarento.upsmf.examsAndAdmissions.service.impl;

import com.tarento.upsmf.examsAndAdmissions.model.Exam;
import com.tarento.upsmf.examsAndAdmissions.model.QuestionPaper;
import com.tarento.upsmf.examsAndAdmissions.model.ResponseDto;
import com.tarento.upsmf.examsAndAdmissions.model.dto.QuestionPaperResponseDTO;
import com.tarento.upsmf.examsAndAdmissions.repository.ExamRepository;
import com.tarento.upsmf.examsAndAdmissions.repository.QuestionPaperRepository;
import com.tarento.upsmf.examsAndAdmissions.service.QuestionPaperService;
import com.tarento.upsmf.examsAndAdmissions.util.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class QuestionPaperServiceImpl implements QuestionPaperService {
    @Autowired
    private QuestionPaperRepository questionPaperRepository;
    @Autowired
    private ExamRepository examRepository;
    private final Logger logger = LoggerFactory.getLogger(getClass().getName());

    @Override
    public ResponseDto getAllQuestionPapers(Long examCycleId, Long examId) {
        ResponseDto response = new ResponseDto(Constants.API_QUESTION_PAPER_GET_ALL);
        logger.info("Fetching all Question papers...");
        List<QuestionPaper> questionPapers = questionPaperRepository.findAll();
        if (questionPapers.isEmpty()) {
            response.put(Constants.MESSAGE, "Getting Error in fetching Question papers details");
            response.setResponseCode(HttpStatus.NOT_FOUND);
        } else {
            Boolean filterFlag = false;
            List<QuestionPaper> questionPaperList = new ArrayList<>();
            for (int i = 0; i < questionPapers.size(); i++) {
                QuestionPaper questionPaper = questionPapers.get(i);
                if (questionPaper.getExamCycleId().equals(examCycleId) && questionPaper.getExam().getId().equals(examId)) {
                    filterFlag = true;
                    questionPaperList.add(questionPaper);
                } else {
                    response.put(Constants.MESSAGE, "Data is not there related to filters applied");
                    response.setResponseCode(HttpStatus.NOT_FOUND);
                }
            }
            if (filterFlag) {
                response.put(Constants.MESSAGE, Constants.SUCCESSFUL);
                response.put(Constants.RESPONSE, questionPaperList);
                response.setResponseCode(HttpStatus.OK);
            }
        }
        return response;
    }

    @Override
    public ResponseDto getQuestionPaperById(Long id) {
        ResponseDto response = new ResponseDto(Constants.API_QUESTION_PAPER_GET_BY_ID);
        Optional<QuestionPaper> questionPaperOptional = questionPaperRepository.findById(id);
        if (questionPaperOptional.isPresent()) {
            QuestionPaper questionPaper = questionPaperOptional.get();
            if (questionPaper.getObsolete() == 0) {
                response.put(Constants.MESSAGE, Constants.SUCCESSFUL);
                response.put(Constants.RESPONSE, questionPaper);
                response.setResponseCode(HttpStatus.OK);
            } else {
                response.put(Constants.MESSAGE, "Question paper id is deleted(Obsolete is not equal to zero)");
                response.setResponseCode(HttpStatus.NOT_FOUND);
            }
        } else {
            response.put(Constants.MESSAGE, "Getting Error in fetching Question paper details by id");
            response.setResponseCode(HttpStatus.NOT_FOUND);
        }
        return response;
    }
    public ResponseDto getAllQuestionPapersByExamCycleId(Long examCycleId) {
        ResponseDto response = new ResponseDto(Constants.API_QUESTION_PAPER_GET_ALL);
        logger.info("Fetching all exams and their question papers for examCycleId...");

        List<Exam> examsForCycle = examRepository.findByExamCycleId(examCycleId);

        if (examsForCycle.isEmpty()) {
            response.put(Constants.MESSAGE, "No exams found for the given exam cycle.");
            response.setResponseCode(HttpStatus.NOT_FOUND);
            return response;
        }

        List<Map<String, Object>> examDataList = new ArrayList<>();

        for (Exam exam : examsForCycle) {
            Map<String, Object> examData = new HashMap<>();
            examData.put("examId", exam.getId());
            examData.put("examName", exam.getExamName());
            examData.put("courseName",exam.getCourse().getCourseName());
            examData.put("examDate",exam.getExamDate());
            examData.put("startTime",exam.getStartTime());
            examData.put("maximumMark",exam.getMaximumMark());

            List<QuestionPaper> questionPapersForExam = questionPaperRepository.findByExamCycleIdAndExamId(examCycleId, exam.getId());
            examData.put("questionPapers", questionPapersForExam.stream()
                    .map(this::mapToQuestionPaperDTO)
                    .collect(Collectors.toList()));

            examDataList.add(examData);
        }

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("examCycleId", examCycleId);
        responseData.put("exams", examDataList);

        response.put(Constants.MESSAGE, Constants.SUCCESSFUL);
        response.put(Constants.RESPONSE, responseData);
        response.setResponseCode(HttpStatus.OK);

        return response;
    }
    public QuestionPaperResponseDTO mapToQuestionPaperDTO(QuestionPaper questionPaper) {
        QuestionPaperResponseDTO dto = new QuestionPaperResponseDTO();

        dto.setId(questionPaper.getId());
        dto.setFileName(questionPaper.getFileName());
        dto.setGcpFileName(questionPaper.getGcpFileName());
        dto.setExamDate(questionPaper.getExamDate());
        dto.setExamStartTime(questionPaper.getExamStartTime());
        dto.setExamCycleName(questionPaper.getExamCycleName());
        dto.setExamCycleId(questionPaper.getExamCycleId());
        dto.setExamName(questionPaper.getExamName());
        dto.setCourseName(questionPaper.getCourseName());
        dto.setCreatedBy(questionPaper.getCreatedBy());
        dto.setCreatedOn(questionPaper.getCreatedOn());
        dto.setModifiedBy(questionPaper.getModifiedBy());
        dto.setModifiedOn(questionPaper.getModifiedOn());
        dto.setTotalMarks(questionPaper.getTotalMarks());
        dto.setInternalMarks(questionPaper.getInternalMarks());
        dto.setInternalPassingMarks(questionPaper.getInternalPassingMarks());
        dto.setExternalMarks(questionPaper.getExternalMarks());
        dto.setExternalPassingMarks(questionPaper.getExternalPassingMarks());
        dto.setPassingMarks(questionPaper.getPassingMarks());
        dto.setObsolete(questionPaper.getObsolete());

        return dto;
    }

}
