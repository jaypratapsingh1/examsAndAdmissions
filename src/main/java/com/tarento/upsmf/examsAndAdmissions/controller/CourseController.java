package com.tarento.upsmf.examsAndAdmissions.controller;

import com.tarento.upsmf.examsAndAdmissions.model.Course;
import com.tarento.upsmf.examsAndAdmissions.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
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
    public Course createCourse(@RequestBody Course course) {
        return courseService.createCourse(course);
    }

    @GetMapping("/list")
    public List<Course> getAllCourses() {
        return courseService.getAllCourses();
    }

    @GetMapping("/list/{id}")
    public Course getCourseById(@PathVariable Long id) {
        return courseService.getCourseById(id);
    }
}
