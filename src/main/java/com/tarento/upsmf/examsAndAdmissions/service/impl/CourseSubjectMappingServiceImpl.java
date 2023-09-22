package com.tarento.upsmf.examsAndAdmissions.service.impl;

import com.tarento.upsmf.examsAndAdmissions.model.*;
import com.tarento.upsmf.examsAndAdmissions.model.dto.CourseSubjectMappingDTO;
import com.tarento.upsmf.examsAndAdmissions.repository.CourseRepository;
import com.tarento.upsmf.examsAndAdmissions.repository.CourseSubjectMappingRepository;
import com.tarento.upsmf.examsAndAdmissions.repository.SubjectRepository;
import com.tarento.upsmf.examsAndAdmissions.service.CourseSubjectMappingService;
import com.tarento.upsmf.examsAndAdmissions.util.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.*;

@Service
@Slf4j
public class CourseSubjectMappingServiceImpl implements CourseSubjectMappingService {

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private CourseSubjectMappingRepository courseSubjectMappingRepository;

    private Set courseIdSet = new HashSet<>();

    private List data = new ArrayList<>();

    private Map<String, Object> responseMap = new HashMap<String, Object>();

    private Map<String, Object> courseMap = new HashMap<String, Object>();

    @Override
    public ResponseDto create(CourseSubjectMappingDTO courseSubjectMappingDTO) {
        ResponseDto response = new ResponseDto(Constants.API_COURSE_SUBJECT_MAPPING_CREATE);
        try {
            CourseSubjectMapping courseSubjectMapping = new CourseSubjectMapping();
            Subject subject = subjectRepository.findById(courseSubjectMappingDTO.getSubjectId()).orElse(null);
            courseSubjectMapping.setSubject(subject);
            Course course = courseRepository.findById(courseSubjectMappingDTO.getCourseId()).orElse(null);
            courseSubjectMapping.setCourse(course);
            courseSubjectMapping.setCreatedOn(new Timestamp(System.currentTimeMillis()));
            courseSubjectMapping = courseSubjectMappingRepository.save(courseSubjectMapping);
            response.put(Constants.MESSAGE, Constants.SUCCESSFUL);
            response.put(Constants.RESPONSE, courseSubjectMapping);
            response.setResponseCode(HttpStatus.OK);
        } catch (Exception e) {
            response.getParams().setStatus(Constants.FAILED);
            response.getParams().setErrmsg(e.getMessage());
            response.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return response;
    }

    @Override
    public ResponseDto getAllMapping() {
        ResponseDto response = new ResponseDto(Constants.API_COURSE_SUBJECT_MAPPING_GET_ALL);
        log.info("Fetching all CourseSubjectMapping ..");
        List<CourseSubjectMapping> courseSubjectMappings = courseSubjectMappingRepository.findAll();
        if (courseSubjectMappings.isEmpty()) {
            response.put(Constants.MESSAGE, "Getting Error in fetching CourseSubjectMapping details");
            response.put(Constants.RESPONSE, Constants.FAILUREMESSAGE);
            response.setResponseCode(HttpStatus.NOT_FOUND);
        } else {
            courseIdSet = new HashSet<>();
            data = new ArrayList<>();
            for (int i = 0; i < courseSubjectMappings.size(); i++) {
                courseIdSet.add(courseSubjectMappings.get(i).getCourse().getId());
            }

            for (Object iterateCourseIds : courseIdSet) {
                responseMap = new HashMap<String, Object>();
                courseMap = new HashMap<String, Object>();
                Boolean checkCourse = true;
                List subjectList = new ArrayList<>();
                for (int i = 0; i < courseSubjectMappings.size(); i++) {
                    Map<String, Object> subjectMap = new HashMap<String, Object>();
                    if (iterateCourseIds.equals(courseSubjectMappings.get(i).getCourse().getId())) {
                        if (checkCourse) {
                            checkCourse = false;
                            responseMap.put(Constants.COURSE_ID, courseSubjectMappings.get(i).getCourse().getId());
                            responseMap.put("courseCode", courseSubjectMappings.get(i).getCourse().getCourseCode());
                            responseMap.put(Constants.COURSE_NAME, courseSubjectMappings.get(i).getCourse().getCourseName());
                            responseMap.put("description", courseSubjectMappings.get(i).getCourse().getDescription());
                        }
                        subjectMap.put("subjectId", courseSubjectMappings.get(i).getSubject().getId());
                        subjectMap.put("subjectCode", courseSubjectMappings.get(i).getSubject().getSubjectCode());
                        subjectMap.put("subjectName", courseSubjectMappings.get(i).getSubject().getSubjectName());
                        subjectMap.put("description", courseSubjectMappings.get(i).getSubject().getDescription());
                    }
                    if (!subjectMap.isEmpty())
                        subjectList.add(subjectMap);
                }
                if (!subjectList.isEmpty())
                    responseMap.put("subjects", subjectList);
                if (!responseMap.isEmpty())
                    courseMap.put("course", responseMap);
                if (!courseMap.isEmpty())
                    data.add(courseMap);
            }

            response.put(Constants.MESSAGE, Constants.SUCCESSFUL);
            response.put(Constants.RESPONSE, data);
            response.setResponseCode(HttpStatus.OK);
        }
        return response;
    }

    @Override
    public ResponseDto getAllMappingByFilter(Long courseId) {
        ResponseDto response = new ResponseDto(Constants.API_COURSE_SUBJECT_MAPPING_GET_ALL);
        log.info("Fetching all CourseSubjectMapping ..");
        List<CourseSubjectMapping> courseSubjectMappings = courseSubjectMappingRepository.findAll();
        if (courseSubjectMappings.isEmpty()) {
            response.put(Constants.MESSAGE, "Getting Error in fetching CourseSubjectMapping details");
            response.put(Constants.RESPONSE, Constants.FAILUREMESSAGE);
            response.setResponseCode(HttpStatus.NOT_FOUND);
        } else {
            courseIdSet = new HashSet<>();
            data = new ArrayList<>();
            for (int i = 0; i < courseSubjectMappings.size(); i++) {
                courseIdSet.add(courseSubjectMappings.get(i).getCourse().getId());
            }

            for (Object iterateCourseIds : courseIdSet) {
                responseMap = new HashMap<String, Object>();
                courseMap = new HashMap<String, Object>();
                Boolean checkCourse = true;
                List subjectList = new ArrayList<>();
                for (int i = 0; i < courseSubjectMappings.size(); i++) {
                    Map<String, Object> subjectMap = new HashMap<String, Object>();
                    if (iterateCourseIds.equals(courseSubjectMappings.get(i).getCourse().getId())) {
                        if (courseId.equals(courseSubjectMappings.get(i).getCourse().getId())) {
                            if (checkCourse) {
                                checkCourse = false;
                                responseMap.put(Constants.COURSE_ID, courseSubjectMappings.get(i).getCourse().getId());
                                responseMap.put("courseCode", courseSubjectMappings.get(i).getCourse().getCourseCode());
                                responseMap.put(Constants.COURSE_NAME, courseSubjectMappings.get(i).getCourse().getCourseName());
                                responseMap.put("description", courseSubjectMappings.get(i).getCourse().getDescription());
                            }
                            subjectMap.put("subjectId", courseSubjectMappings.get(i).getSubject().getId());
                            subjectMap.put("subjectCode", courseSubjectMappings.get(i).getSubject().getSubjectCode());
                            subjectMap.put("subjectName", courseSubjectMappings.get(i).getSubject().getSubjectName());
                            subjectMap.put("description", courseSubjectMappings.get(i).getSubject().getDescription());
                        }
                    }
                    if (!subjectMap.isEmpty())
                        subjectList.add(subjectMap);
                }
                if (!subjectList.isEmpty())
                    responseMap.put("subjects", subjectList);
                if (!responseMap.isEmpty())
                    courseMap.put("course", responseMap);
                if (!courseMap.isEmpty())
                    data.add(courseMap);
            }

            response.put(Constants.MESSAGE, Constants.SUCCESSFUL);
            response.put(Constants.RESPONSE, data);
            response.setResponseCode(HttpStatus.OK);
        }
        return response;
    }

    @Override
    public ResponseDto getCourseSubjectMappingById(Long id) {
        ResponseDto response = new ResponseDto(Constants.API_COURSE_SUBJECT_MAPPING_GET_BY_ID);
        Optional<CourseSubjectMapping> courseSubjectMappingOptional = courseSubjectMappingRepository.findById(id);
        if (courseSubjectMappingOptional.isPresent()) {
            CourseSubjectMapping courseSubjectMapping = courseSubjectMappingOptional.get();
            if (courseSubjectMapping.getObsolete() == 0) {
                response.put(Constants.MESSAGE, Constants.SUCCESSFUL);
                response.put(Constants.RESPONSE, courseSubjectMapping);
                response.setResponseCode(HttpStatus.OK);
            } else {
                response.put(Constants.MESSAGE, "CourseSubjectMapping id is deleted(Obsolete is not equal to zero)");
                response.put(Constants.RESPONSE, Constants.FAILUREMESSAGE);
                response.setResponseCode(HttpStatus.NOT_FOUND);
            }
        } else {
            response.put(Constants.MESSAGE, "Getting Error in fetching CourseSubjectMapping details by id");
            response.put(Constants.RESPONSE, Constants.FAILUREMESSAGE);
            response.setResponseCode(HttpStatus.NOT_FOUND);
        }
        return response;
    }

    @Override
    public ResponseDto deleteCourseSubjectMapping(Long id) {
        ResponseDto response = new ResponseDto(Constants.API_COURSE_SUBJECT_MAPPING_DELETE);
        try {
            CourseSubjectMapping courseSubjectMapping = courseSubjectMappingRepository.findById(id).orElse(null);
            if (courseSubjectMapping != null) {
                courseSubjectMapping.setObsolete(1);
                courseSubjectMappingRepository.save(courseSubjectMapping);
                response.put(Constants.MESSAGE, Constants.SUCCESSFUL);
                response.put(Constants.RESPONSE, "CourseSubjectMapping id is deleted successfully");
                response.setResponseCode(HttpStatus.OK);
            } else {
                log.warn("CourseSubjectMapping with ID: {} not found for deletion!", id);
                response.put(Constants.MESSAGE, "CourseSubjectMapping with id not found for deletion!");
                response.put(Constants.RESPONSE, Constants.MESSAGE);
                response.setResponseCode(HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            response.put(Constants.MESSAGE, "Exception occurred during deleting the CourseSubjectMapping id");
            response.put(Constants.RESPONSE, Constants.MESSAGE);
            response.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return response;
    }
}
