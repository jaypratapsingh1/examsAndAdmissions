package com.tarento.upsmf.examsAndAdmissions.service;

import com.tarento.upsmf.examsAndAdmissions.model.ResponseDto;
import com.tarento.upsmf.examsAndAdmissions.model.Subject;

public interface SubjectService {
    ResponseDto createSubject(Subject subject);

    ResponseDto getAllSubjects();

    ResponseDto getSubjectById(Long id);

    ResponseDto deleteSubjectById(Long id);
}
