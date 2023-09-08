package com.tarento.upsmf.examsAndAdmissions.service;

import com.tarento.upsmf.examsAndAdmissions.model.ResponseDto;

public interface QuestionPaperService {
    ResponseDto getAllQuestionPapers();

    ResponseDto deleteQuestionPaper(Long id);

    ResponseDto getQuestionPaperById(Long id);

}
