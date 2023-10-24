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
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

import static com.tarento.upsmf.examsAndAdmissions.model.ResponseDto.setErrorResponse;

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

    public ResponseDto create(CourseSubjectMappingDTO courseSubjectMappingDTO) {
        ResponseDto response = new ResponseDto(Constants.API_COURSE_SUBJECT_MAPPING_CREATE);
        try {
            // Fetch the Course from the database
            Course course = courseRepository.findById(courseSubjectMappingDTO.getCourseId()).orElse(null);
            if (course == null) {
                throw new Exception("Course not found!");
            }

            // Fetch the Subjects from the database
            List<Subject> subjects = subjectRepository.findAllById(courseSubjectMappingDTO.getSubjectIds());
            if(subjects.isEmpty()) {
                throw new Exception("No subjects found for the provided IDs!");
            }

            // Link subjects to the course
            course.setSubjects(subjects);

            // Save the Course to the database, which will also update the course_subject_mapping table
            courseRepository.save(course);

            response.put(Constants.MESSAGE, Constants.SUCCESSFUL);
            response.put(Constants.RESPONSE, course);
            response.setResponseCode(HttpStatus.OK);

        } catch (Exception e) {
            ResponseDto.setErrorResponse(response, "INTERNAL_ERROR", e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return response;
    }

    @Override
    public ResponseDto getAllMapping() {
        ResponseDto response = new ResponseDto(Constants.API_COURSE_SUBJECT_MAPPING_GET_ALL);
        List<CourseSubjectMapping> courseSubjectMappings = courseSubjectMappingRepository.findAll();

        if (!courseSubjectMappings.isEmpty() && courseSubjectMappings.stream().anyMatch(mapping -> mapping.getCourse() != null)) {
            List<Map<String, Object>> courseMappings = courseSubjectMappings.stream().filter(mapping -> mapping.getCourse() != null).map(mapping -> {
                Map<String, Object> courseData = new HashMap<>();
                courseData.put(Constants.COURSE_ID, mapping.getCourse().getId());
                courseData.put(Constants.COURSE_CODE, mapping.getCourse().getCourseCode());
                courseData.put(Constants.COURSE_NAME, mapping.getCourse().getCourseName());
                courseData.put(Constants.DESCRIPTION, mapping.getCourse().getDescription());

                if (mapping.getSubjects() != null) {
                    List<Map<String, Object>> subjects = mapping.getSubjects().stream().map(subject -> {
                        Map<String, Object> subjectData = new HashMap<>();
                        subjectData.put("subjectId", subject.getId());
                        subjectData.put("subjectCode", subject.getSubjectCode());
                        subjectData.put("subjectName", subject.getSubjectName());
                        subjectData.put("description", subject.getDescription());
                        return subjectData;
                    }).collect(Collectors.toList());

                    courseData.put("subjects", subjects);
                }
                return courseData;
            }).collect(Collectors.toList());

            response.put(Constants.MESSAGE, Constants.SUCCESSFUL);
            response.put(Constants.RESPONSE, courseMappings);
            response.setResponseCode(HttpStatus.OK);
        } else {
            setErrorResponse(response, "NO_COURSE_SUBJECT_MAPPINGS", "No course-subject mappings found.", HttpStatus.NOT_FOUND);
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
                            if (courseSubjectMappings.get(i).getSubjects() != null) {
                                subjectMap.put("subjectId", courseSubjectMappings.get(i).getSubjects().get(i).getId());
                                subjectMap.put("subjectCode", courseSubjectMappings.get(i).getSubjects().get(i).getSubjectCode());
                                subjectMap.put("subjectName", courseSubjectMappings.get(i).getSubjects().get(i).getSubjectName());
                                subjectMap.put("description", courseSubjectMappings.get(i).getSubjects().get(i).getDescription());
                            }

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
    public ResponseDto getCourseSubjectMappingById(Long courseId) {
        ResponseDto response = new ResponseDto(Constants.API_COURSE_SUBJECT_MAPPING_GET_BY_ID);

        Optional<Course> courseOptional = courseRepository.findById(courseId);
        if (!courseOptional.isPresent()) {
            setErrorResponse(response, "COURSE_NOT_FOUND", "Course not found for the provided ID", HttpStatus.NOT_FOUND);
            return response;
        }

        Course course = courseOptional.get();
        List<Subject> subjects = course.getSubjects();

        Map<String, Object> courseData = new HashMap<>();
        courseData.put("courseId", course.getId());
        courseData.put("courseCode", course.getCourseCode());
        courseData.put("courseName", course.getCourseName());
        courseData.put("description", course.getDescription());

        if (subjects != null && !subjects.isEmpty()) {
            List<Map<String, Object>> subjectsData = subjects.stream().map(subject -> {
                Map<String, Object> subjectMap = new HashMap<>();
                subjectMap.put("subjectId", subject.getId());
                subjectMap.put("subjectCode", subject.getSubjectCode());
                subjectMap.put("subjectName", subject.getSubjectName());
                subjectMap.put("description", subject.getDescription());
                return subjectMap;
            }).collect(Collectors.toList());

            courseData.put("subjects", subjectsData);
        }

        response.put(Constants.MESSAGE, Constants.SUCCESSFUL);
        response.put(Constants.RESPONSE, courseData);
        response.setResponseCode(HttpStatus.OK);

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
