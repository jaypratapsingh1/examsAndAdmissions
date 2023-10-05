package com.tarento.upsmf.examsAndAdmissions.service;

import com.tarento.upsmf.examsAndAdmissions.model.ResponseDto;
import com.tarento.upsmf.examsAndAdmissions.model.dto.InstituteCourseMappingDTO;

public interface InstituteCourseMappingService {
    ResponseDto create(InstituteCourseMappingDTO instituteCourseMappingDTO);

    ResponseDto getAllMapping();

    ResponseDto getInstituteCourseMappingById(Long id);

    ResponseDto deleteInstituteCourseMapping(Long id);

    ResponseDto getAllMappingByFilter(Long instituteId);
}
