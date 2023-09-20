package com.tarento.upsmf.examsAndAdmissions.service.impl;

import com.tarento.upsmf.examsAndAdmissions.model.Course;
import com.tarento.upsmf.examsAndAdmissions.model.Institute;
import com.tarento.upsmf.examsAndAdmissions.model.InstituteCourseMapping;
import com.tarento.upsmf.examsAndAdmissions.model.ResponseDto;
import com.tarento.upsmf.examsAndAdmissions.model.dto.InstituteCourseMappingDTO;
import com.tarento.upsmf.examsAndAdmissions.repository.CourseRepository;
import com.tarento.upsmf.examsAndAdmissions.repository.InstituteCourseMappingRepository;
import com.tarento.upsmf.examsAndAdmissions.repository.InstituteRepository;
import com.tarento.upsmf.examsAndAdmissions.service.InstituteCourseMappingService;
import com.tarento.upsmf.examsAndAdmissions.util.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.*;

@Service
@Slf4j
public class InstituteCourseMappingServiceImpl implements InstituteCourseMappingService {

    @Autowired
    private InstituteCourseMappingRepository instituteCourseMappingRepository;

    @Autowired
    private InstituteRepository instituteRepository;

    @Autowired
    private CourseRepository courseRepository;

    private Set instituteIdSet = new HashSet<>();

    private List data = new ArrayList<>();

    private Map<String, Object> responseMap = new HashMap<String, Object>();

    private Map<String, Object> instituteMap = new HashMap<String, Object>();

    @Override
    public ResponseDto create(InstituteCourseMappingDTO instituteCourseMappingDTO) {
        ResponseDto response = new ResponseDto(Constants.API_INSTITUTE_COURSE_MAPPING_CREATE);
        try {
            InstituteCourseMapping instituteCourseMapping = new InstituteCourseMapping();
            Long instituteId = instituteCourseMappingDTO.getInstituteId();
            Institute institute = instituteRepository.findById(instituteId).orElse(null);
            instituteCourseMapping.setInstitute(institute);
            Course course = courseRepository.findById(instituteCourseMappingDTO.getCourseId()).orElse(null);
            instituteCourseMapping.setCourse(course);
            instituteCourseMapping.setSeatCapacity(instituteCourseMappingDTO.getSeatCapacity());
            instituteCourseMapping.setCreatedOn(new Timestamp(System.currentTimeMillis()));
            instituteCourseMapping = instituteCourseMappingRepository.save(instituteCourseMapping);
            response.put(Constants.MESSAGE, Constants.SUCCESSFUL);
            response.put(Constants.RESPONSE, instituteCourseMapping);
            response.setResponseCode(HttpStatus.OK);
        } catch (Exception e) {
            response.getParams().setStatus(Constants.FAILED);
            response.getParams().setErrmsg(e.getMessage());
            response.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return response;
    }

    @Override
    public ResponseDto getAllMappingByFilter(Long instituteId) {
        ResponseDto response = new ResponseDto(Constants.API_INSTITUTE_COURSE_MAPPING_GET_ALL);
        log.info("Fetching all InstituteCourseMapping ..");
        List<InstituteCourseMapping> instituteCourseMappings = instituteCourseMappingRepository.findAll();
        if (instituteCourseMappings.isEmpty()) {
            response.put(Constants.MESSAGE, "Getting Error in fetching InstituteCourseMapping details");
            response.put(Constants.RESPONSE, Constants.FAILUREMESSAGE);
            response.setResponseCode(HttpStatus.NOT_FOUND);
        } else {
            instituteIdSet = new HashSet<>();
            data = new ArrayList<>();
            for (int i = 0; i < instituteCourseMappings.size(); i++) {
                instituteIdSet.add(instituteCourseMappings.get(i).getInstitute().getId());
            }

            for (Object iterateInstituteIds : instituteIdSet) {
                responseMap = new HashMap<String, Object>();
                instituteMap = new HashMap<String, Object>();
                Boolean checkInstitute = true;
                List courseList = new ArrayList<>();
                for (int i = 0; i < instituteCourseMappings.size(); i++) {
                    Map<String, Object> courseMap = new HashMap<String, Object>();
                    if (iterateInstituteIds.equals(instituteCourseMappings.get(i).getInstitute().getId())) {
                        if (instituteId.equals(instituteCourseMappings.get(i).getInstitute().getId())) {
                            if (checkInstitute) {
                                checkInstitute = false;
                                responseMap.put("instituteId", instituteCourseMappings.get(i).getInstitute().getId());
                                responseMap.put("instituteName", instituteCourseMappings.get(i).getInstitute().getInstituteName());
                                responseMap.put("instituteCode", instituteCourseMappings.get(i).getInstitute().getInstituteCode());
                                responseMap.put("address", instituteCourseMappings.get(i).getInstitute().getAddress());
                                responseMap.put("email", instituteCourseMappings.get(i).getInstitute().getEmail());
                                responseMap.put("allowedForExamCentre", instituteCourseMappings.get(i).getInstitute().isAllowedForExamCentre());
                                responseMap.put("district", instituteCourseMappings.get(i).getInstitute().getDistrict());
                                responseMap.put("cctvVerified", instituteCourseMappings.get(i).getInstitute().isCctvVerified());
                                responseMap.put("ipAddress", instituteCourseMappings.get(i).getInstitute().getIpAddress());
                                responseMap.put("remarks", instituteCourseMappings.get(i).getInstitute().getRemarks());
                            }
                            courseMap.put(Constants.COURSE_ID, instituteCourseMappings.get(i).getCourse().getId());
                            courseMap.put("courseCode", instituteCourseMappings.get(i).getCourse().getCourseCode());
                            courseMap.put(Constants.COURSE_NAME, instituteCourseMappings.get(i).getCourse().getCourseName());
                            courseMap.put("description", instituteCourseMappings.get(i).getCourse().getDescription());
                            courseMap.put("seatCapacity", instituteCourseMappings.get(i).getSeatCapacity());
                        }
                    }
                    if (!courseMap.isEmpty())
                        courseList.add(courseMap);
                }
                if (!responseMap.isEmpty())
                    instituteMap.put("institute", responseMap);
                if (!courseList.isEmpty())
                    responseMap.put("courses", courseList);
                if (!instituteMap.isEmpty())
                    data.add(instituteMap);
            }

            response.put(Constants.MESSAGE, Constants.SUCCESSFUL);
            response.put(Constants.RESPONSE, data);
            response.setResponseCode(HttpStatus.OK);
        }
        return response;
    }

    @Override
    public ResponseDto getAllMapping() {
        ResponseDto response = new ResponseDto(Constants.API_INSTITUTE_COURSE_MAPPING_GET_ALL);
        log.info("Fetching all InstituteCourseMapping ..");
        List<InstituteCourseMapping> instituteCourseMappings = instituteCourseMappingRepository.findAll();
        if (instituteCourseMappings.isEmpty()) {
            response.put(Constants.MESSAGE, "Getting Error in fetching InstituteCourseMapping details");
            response.put(Constants.RESPONSE, Constants.FAILUREMESSAGE);
            response.setResponseCode(HttpStatus.NOT_FOUND);
        } else {
            instituteIdSet = new HashSet<>();
            data = new ArrayList<>();
            for (int i = 0; i < instituteCourseMappings.size(); i++) {
                instituteIdSet.add(instituteCourseMappings.get(i).getInstitute().getId());
            }

            for (Object iterateInstituteIds : instituteIdSet) {
                responseMap = new HashMap<String, Object>();
                instituteMap = new HashMap<String, Object>();
                Boolean checkInstitute = true;
                List courseList = new ArrayList<>();
                for (int i = 0; i < instituteCourseMappings.size(); i++) {
                    Map<String, Object> courseMap = new HashMap<String, Object>();
                    if (iterateInstituteIds.equals(instituteCourseMappings.get(i).getInstitute().getId())) {
                        if (checkInstitute) {
                            checkInstitute = false;
                            responseMap.put("instituteId", instituteCourseMappings.get(i).getInstitute().getId());
                            responseMap.put("instituteName", instituteCourseMappings.get(i).getInstitute().getInstituteName());
                            responseMap.put("instituteCode", instituteCourseMappings.get(i).getInstitute().getInstituteCode());
                            responseMap.put("address", instituteCourseMappings.get(i).getInstitute().getAddress());
                            responseMap.put("email", instituteCourseMappings.get(i).getInstitute().getEmail());
                            responseMap.put("allowedForExamCentre", instituteCourseMappings.get(i).getInstitute().isAllowedForExamCentre());
                            responseMap.put("district", instituteCourseMappings.get(i).getInstitute().getDistrict());
                            responseMap.put("cctvVerified", instituteCourseMappings.get(i).getInstitute().isCctvVerified());
                            responseMap.put("ipAddress", instituteCourseMappings.get(i).getInstitute().getIpAddress());
                            responseMap.put("remarks", instituteCourseMappings.get(i).getInstitute().getRemarks());
                        }
                        courseMap.put(Constants.COURSE_ID, instituteCourseMappings.get(i).getCourse().getId());
                        courseMap.put("courseCode", instituteCourseMappings.get(i).getCourse().getCourseCode());
                        courseMap.put(Constants.COURSE_NAME, instituteCourseMappings.get(i).getCourse().getCourseName());
                        courseMap.put("description", instituteCourseMappings.get(i).getCourse().getDescription());
                        courseMap.put("seatCapacity", instituteCourseMappings.get(i).getSeatCapacity());
                    }
                    if (!courseMap.isEmpty())
                        courseList.add(courseMap);
                }
                if (!responseMap.isEmpty())
                    instituteMap.put("institute", responseMap);
                if (!courseList.isEmpty())
                    responseMap.put("courses", courseList);
                if (!instituteMap.isEmpty())
                    data.add(instituteMap);
            }

            response.put(Constants.MESSAGE, Constants.SUCCESSFUL);
            response.put(Constants.RESPONSE, data);
            response.setResponseCode(HttpStatus.OK);
        }
        return response;
    }

    @Override
    public ResponseDto getInstituteCourseMappingById(Long id) {
        ResponseDto response = new ResponseDto(Constants.API_INSTITUTE_COURSE_MAPPING_GET_BY_ID);
        Optional<InstituteCourseMapping> instituteCourseMappingOptional = instituteCourseMappingRepository.findById(id);
        if (instituteCourseMappingOptional.isPresent()) {
            InstituteCourseMapping instituteCourseMapping = instituteCourseMappingOptional.get();
            if (instituteCourseMapping.getObsolete() == 0) {
                response.put(Constants.MESSAGE, Constants.SUCCESSFUL);
                response.put(Constants.RESPONSE, instituteCourseMapping);
                response.setResponseCode(HttpStatus.OK);
            } else {
                response.put(Constants.MESSAGE, "InstituteCourseMapping id is deleted(Obsolete is not equal to zero)");
                response.put(Constants.RESPONSE, Constants.FAILUREMESSAGE);
                response.setResponseCode(HttpStatus.NOT_FOUND);
            }
        } else {
            response.put(Constants.MESSAGE, "Getting Error in fetching InstituteCourseMapping details by id");
            response.put(Constants.RESPONSE, Constants.FAILUREMESSAGE);
            response.setResponseCode(HttpStatus.NOT_FOUND);
        }
        return response;
    }

    @Override
    public ResponseDto deleteInstituteCourseMapping(Long id) {
        ResponseDto response = new ResponseDto(Constants.API_INSTITUTE_COURSE_MAPPING_DELETE);
        try {
            InstituteCourseMapping instituteCourseMapping = instituteCourseMappingRepository.findById(id).orElse(null);
            if (instituteCourseMapping != null) {
                instituteCourseMapping.setObsolete(1);
                instituteCourseMappingRepository.save(instituteCourseMapping);
                response.put(Constants.MESSAGE, Constants.SUCCESSFUL);
                response.put(Constants.RESPONSE, "InstituteCourseMapping id is deleted successfully");
                response.setResponseCode(HttpStatus.OK);
            } else {
                log.warn("InstituteCourseMapping with ID: {} not found for deletion!", id);
                response.put(Constants.MESSAGE, "InstituteCourseMapping with id not found for deletion!");
                response.put(Constants.RESPONSE, Constants.MESSAGE);
                response.setResponseCode(HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            response.put(Constants.MESSAGE, "Exception occurred during deleting the InstituteCourseMapping id");
            response.put(Constants.RESPONSE, Constants.MESSAGE);
            response.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return response;
    }
}
