package com.tarento.upsmf.examsAndAdmissions.repository;

import com.tarento.upsmf.examsAndAdmissions.model.UploadedFile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileUploadRepository extends JpaRepository<UploadedFile,Integer> {
}
