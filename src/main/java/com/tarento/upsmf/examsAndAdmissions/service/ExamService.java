package com.tarento.upsmf.examsAndAdmissions.service;

import com.tarento.upsmf.examsAndAdmissions.model.Exam;
import com.tarento.upsmf.examsAndAdmissions.model.ResponseDto;
import org.springframework.stereotype.Service;

@Service
public interface ExamService {
    public ResponseDto createExam(Exam exam);

    public ResponseDto getAllExams();

    public ResponseDto getExamById(Long id);

    public ResponseDto updateExam(Long id, Exam updatedExam);

    public ResponseDto deleteExam(Long id);

    public ResponseDto restoreExam(Long id);

    void publishExamResults(Long examId);
}