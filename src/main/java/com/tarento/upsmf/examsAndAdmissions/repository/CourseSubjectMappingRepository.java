package com.tarento.upsmf.examsAndAdmissions.repository;

import com.tarento.upsmf.examsAndAdmissions.model.CourseSubjectMapping;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CourseSubjectMappingRepository extends JpaRepository<CourseSubjectMapping, Long> {
    Optional<CourseSubjectMapping> findByCourseId(Long id);
}
