package com.tarento.upsmf.examsAndAdmissions.service;

import com.tarento.upsmf.examsAndAdmissions.model.QuestionPaper;
import com.tarento.upsmf.examsAndAdmissions.model.ResponseDto;
import com.tarento.upsmf.examsAndAdmissions.model.dto.DownloadDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

public interface AttachmentService {

    ResponseEntity<?> downloadQuestionPaper(DownloadDto downloadDto);

    ResponseDto getPreviewUrl(Long questionPaperId);

    ResponseDto deleteQuestionPaper(Long id);

    ResponseDto upload(QuestionPaper questionPaper, String userId, MultipartFile file);
}
