package com.tarento.upsmf.examsAndAdmissions.controller;

import com.tarento.upsmf.examsAndAdmissions.model.Course;
import com.tarento.upsmf.examsAndAdmissions.model.ResponseDto;
import com.tarento.upsmf.examsAndAdmissions.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/courses")
public class CourseController {

    private final CourseService courseService;

    @Autowired
    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    @PostMapping("/create")
    public ResponseEntity<?> createCourse(@RequestBody Course course) {
        Course result = courseService.createCourse(course);
        return FeeController.handleSuccessResponse(result);
    }

    @GetMapping("/list")
    public ResponseEntity<?> getAllCourses() {
        List<Course> result = courseService.getAllCourses();
        return FeeController.handleSuccessResponse(result);
    }

    @GetMapping("/list/{id}")
    public ResponseEntity<?> getCourseById(@PathVariable Long id) {
        Course result = courseService.getCourseById(id);
        return FeeController.handleSuccessResponse(result);
    }
}
