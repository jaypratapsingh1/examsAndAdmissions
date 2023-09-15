package com.tarento.upsmf.examsAndAdmissions.service;

import com.tarento.upsmf.examsAndAdmissions.model.ResponseDto;

public interface QuestionPaperService {
    ResponseDto getAllQuestionPapers(Long examCycleId, Long examId);
    ResponseDto getQuestionPaperById(Long id);

}
