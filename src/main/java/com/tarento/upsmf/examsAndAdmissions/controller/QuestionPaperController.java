package com.tarento.upsmf.examsAndAdmissions.controller;

import com.tarento.upsmf.examsAndAdmissions.model.QuestionPaper;
import com.tarento.upsmf.examsAndAdmissions.model.ResponseDto;
import com.tarento.upsmf.examsAndAdmissions.service.AttachmentService;
import com.tarento.upsmf.examsAndAdmissions.service.QuestionPaperService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/questionPaper")
public class QuestionPaperController {

    @Autowired
    private QuestionPaperService questionPaperService;

    @Autowired
    private AttachmentService attachmentService;

    @GetMapping("/download/{questionPaperId}")
    public ResponseEntity<?> downloadFile(@PathVariable Long questionPaperId) {
        ResponseEntity response = attachmentService.downloadQuestionPaper(questionPaperId);
        return new ResponseEntity<>(response, response.getStatusCode());
    }

    @PostMapping("/upload")
    public ResponseEntity<ResponseDto> upload(QuestionPaper questionPaper, String createdBy, MultipartFile file) {
        ResponseDto response = attachmentService.upload(questionPaper, createdBy, file);
        return new ResponseEntity<>(response, response.getResponseCode());
    }

    @GetMapping("/list")
    public ResponseEntity<ResponseDto> getAllQuestionPapers(@RequestParam(required = true) Long examCycleId, Long examId) {
        ResponseDto response = questionPaperService.getAllQuestionPapers(examCycleId, examId);
        return new ResponseEntity<>(response, response.getResponseCode());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseDto> getQuestionPaperById(@PathVariable Long id) {
        ResponseDto response = questionPaperService.getQuestionPaperById(id);
        return new ResponseEntity<>(response, response.getResponseCode());
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ResponseDto> deleteQuestionPaper(@PathVariable Long id) {
        ResponseDto response = attachmentService.deleteQuestionPaper(id);
        return new ResponseEntity<>(response, response.getResponseCode());
    }

    @GetMapping("/preview/{questionPaperId}")
    public ResponseEntity<ResponseDto> getPreviewUrl(@PathVariable Long questionPaperId) {
        ResponseDto response = attachmentService.getPreviewUrl(questionPaperId);
        return new ResponseEntity<>(response, response.getResponseCode());
    }
}
