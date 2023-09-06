package com.tarento.upsmf.examsAndAdmissions.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.tarento.upsmf.examsAndAdmissions.model.Institute;

import java.util.List;

@Repository
public interface InstituteRepository extends JpaRepository<Institute, Long> {
    List<Institute> findByDistrictAndCctvVerified(String district, boolean b);
}
