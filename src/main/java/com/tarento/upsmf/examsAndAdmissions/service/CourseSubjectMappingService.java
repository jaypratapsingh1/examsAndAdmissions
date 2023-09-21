package com.tarento.upsmf.examsAndAdmissions.service;

import com.tarento.upsmf.examsAndAdmissions.model.ResponseDto;
import com.tarento.upsmf.examsAndAdmissions.model.dto.CourseSubjectMappingDTO;

public interface CourseSubjectMappingService {
    ResponseDto create(CourseSubjectMappingDTO courseSubjectMappingDTO);

    ResponseDto getAllMapping();

    ResponseDto getAllMappingByFilter(Long courseId);

    ResponseDto getCourseSubjectMappingById(Long id);

    ResponseDto deleteCourseSubjectMapping(Long id);
}
