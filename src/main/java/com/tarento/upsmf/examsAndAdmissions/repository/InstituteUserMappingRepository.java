package com.tarento.upsmf.examsAndAdmissions.repository;

import com.tarento.upsmf.examsAndAdmissions.model.InstituteUser;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface InstituteUserMappingRepository extends PagingAndSortingRepository<InstituteUser, Long> {
    InstituteUser findByUserId(String userId);
}
