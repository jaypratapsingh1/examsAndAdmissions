package com.tarento.upsmf.examsAndAdmissions.service;

import com.tarento.upsmf.examsAndAdmissions.model.ResponseDto;
import org.springframework.web.multipart.MultipartFile;


import java.io.File;
import java.io.IOException;

public interface StorageService {
	public ResponseDto uploadFile(MultipartFile file, String containerName) throws IOException;

	ResponseDto uploadFile(File file, String containerName);

	public ResponseDto deleteFile(String fileName, String containerName);

	ResponseDto downloadFile(String fileName);
	public String generateSignedUrl(String blobName);
}
