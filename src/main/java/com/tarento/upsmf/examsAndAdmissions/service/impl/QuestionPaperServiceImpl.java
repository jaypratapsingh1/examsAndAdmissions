package com.tarento.upsmf.examsAndAdmissions.service.impl;

import com.tarento.upsmf.examsAndAdmissions.model.QuestionPaper;
import com.tarento.upsmf.examsAndAdmissions.model.ResponseDto;
import com.tarento.upsmf.examsAndAdmissions.repository.QuestionPaperRepository;
import com.tarento.upsmf.examsAndAdmissions.service.QuestionPaperService;
import com.tarento.upsmf.examsAndAdmissions.util.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class QuestionPaperServiceImpl implements QuestionPaperService {
    @Autowired
    private QuestionPaperRepository questionPaperRepository;
    private final Logger logger = LoggerFactory.getLogger(getClass().getName());

    @Override
    public ResponseDto getAllQuestionPapers() {
        ResponseDto response = new ResponseDto(Constants.API_QUESTION_PAPER_GET_ALL);
        logger.info("Fetching all Question papers...");
        List<QuestionPaper> questionPapers = questionPaperRepository.findAll();
        if (questionPapers.isEmpty()) {
            response.put(Constants.MESSAGE, "Getting Error in fetching Question papers details");
            response.put(Constants.RESPONSE, Constants.FAILUREMESSAGE);
            response.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR);
        } else {
            response.put(Constants.MESSAGE, Constants.SUCCESSFUL);
            response.put(Constants.RESPONSE, questionPapers);
            response.setResponseCode(HttpStatus.OK);
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
                response.put(Constants.RESPONSE, Constants.FAILUREMESSAGE);
                response.setResponseCode(HttpStatus.NOT_FOUND);
            }
        } else {
            response.put(Constants.MESSAGE, "Getting Error in fetching Question paper details by id");
            response.put(Constants.RESPONSE, Constants.FAILUREMESSAGE);
            response.setResponseCode(HttpStatus.NOT_FOUND);
        }
        return response;
    }

    @Override
    public ResponseDto deleteQuestionPaper(Long id) {
        ResponseDto response = new ResponseDto(Constants.API_QUESTION_PAPER_DELETE);
        try {
            QuestionPaper questionPaper = questionPaperRepository.findById(id).orElse(null);
            if (questionPaper != null) {
                questionPaper.setObsolete(1);
                questionPaperRepository.save(questionPaper);
                response.put(Constants.MESSAGE, Constants.SUCCESSFUL);
                response.put(Constants.RESPONSE, "Question paper id is deleted successfully");
                response.setResponseCode(HttpStatus.OK);
            } else {
                logger.warn("questionPaper with ID: {} not found for deletion!", id);
                response.put(Constants.MESSAGE, "questionPaper with id not found for deletion!");
                response.put(Constants.RESPONSE, Constants.MESSAGE);
                response.setResponseCode(HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            response.put(Constants.MESSAGE, "Exception occurred during deleting the questionPaper id");
            response.put(Constants.RESPONSE, Constants.MESSAGE);
            response.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return response;
    }

}
