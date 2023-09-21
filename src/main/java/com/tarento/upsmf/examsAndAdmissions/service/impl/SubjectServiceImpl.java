package com.tarento.upsmf.examsAndAdmissions.service.impl;

import com.tarento.upsmf.examsAndAdmissions.model.*;
import com.tarento.upsmf.examsAndAdmissions.repository.SubjectRepository;
import com.tarento.upsmf.examsAndAdmissions.service.SubjectService;
import com.tarento.upsmf.examsAndAdmissions.util.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Service
public class SubjectServiceImpl implements SubjectService {

    @Autowired
    private SubjectRepository subjectRepository;

    @Override
    public ResponseDto createSubject(Subject subject) {
        ResponseDto response = new ResponseDto(Constants.API_SUBJECT_CREATE);
        try {
            subject.setCreatedOn(new Timestamp(System.currentTimeMillis()));
            subjectRepository.save(subject);
            response.put(Constants.MESSAGE, Constants.SUCCESSFUL);
            response.put(Constants.RESPONSE, "Subject is created successfully");
            response.setResponseCode(HttpStatus.OK);
        } catch (Exception e) {
            response.getParams().setStatus(Constants.FAILED);
            response.getParams().setErrmsg(e.getMessage());
            response.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return response;
    }

    @Override
    public ResponseDto getAllSubjects() {
        ResponseDto response = new ResponseDto(Constants.API_SUBJECT_GET_ALL);
        List<Subject> subjects = subjectRepository.findAll();
        if (subjects.isEmpty()) {
            response.put(Constants.MESSAGE, "Getting Error in fetching subjects details");
            response.put(Constants.RESPONSE, Constants.FAILUREMESSAGE);
            response.setResponseCode(HttpStatus.NOT_FOUND);
        } else {
            response.put(Constants.MESSAGE, Constants.SUCCESSFUL);
            response.put(Constants.RESPONSE, subjects);
            response.setResponseCode(HttpStatus.OK);
        }
        return response;
    }

    @Override
    public ResponseDto getSubjectById(Long id) {
        ResponseDto response = new ResponseDto(Constants.API_SUBJECT_GET_BY_ID);
        Optional<Subject> subjectOptional = subjectRepository.findById(id);
        if (subjectOptional.isPresent()) {
            Subject subject = subjectOptional.get();
            if (subject.getObsolete() == 0) {
                response.put(Constants.MESSAGE, Constants.SUCCESSFUL);
                response.put(Constants.RESPONSE, subject);
                response.setResponseCode(HttpStatus.OK);
            } else {
                response.put(Constants.MESSAGE, "Subject id is deleted(Obsolete is not equal to zero)");
                response.put(Constants.RESPONSE, Constants.FAILUREMESSAGE);
                response.setResponseCode(HttpStatus.NOT_FOUND);
            }
        } else {
            response.put(Constants.MESSAGE, "Getting Error in fetching Subject details by id");
            response.put(Constants.RESPONSE, Constants.FAILUREMESSAGE);
            response.setResponseCode(HttpStatus.NOT_FOUND);
        }
        return response;
    }

    @Override
    public ResponseDto deleteSubjectById(Long id) {
        ResponseDto response = new ResponseDto(Constants.API_SUBJECT_DELETE);
        try {
            Subject subject = subjectRepository.findById(id).orElse(null);
            if (subject != null) {
                subject.setObsolete(1);
                subjectRepository.save(subject);
                response.put(Constants.MESSAGE, Constants.SUCCESSFUL);
                response.put(Constants.RESPONSE, "Subject id is deleted successfully");
                response.setResponseCode(HttpStatus.OK);
            } else {
                response.put(Constants.MESSAGE, "Subject with id not found for deletion!");
                response.put(Constants.RESPONSE, Constants.MESSAGE);
                response.setResponseCode(HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            response.put(Constants.MESSAGE, "Exception occurred during deleting the Subject id");
            response.put(Constants.RESPONSE, Constants.MESSAGE);
            response.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return response;
    }
}
