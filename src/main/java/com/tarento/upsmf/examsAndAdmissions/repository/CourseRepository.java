package com.tarento.upsmf.examsAndAdmissions.repository;

import com.tarento.upsmf.examsAndAdmissions.model.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CourseRepository extends JpaRepository<Course, Long> {
    Course findByCourseCode(String courseCode);

    @Query(value="select * from course where course_name=:courseName",nativeQuery = true)
    Optional<Course> findByCourseNameIgnoreCase(@Param("courseName") String courseName);

    Course findByInstituteId(Long id);
}
