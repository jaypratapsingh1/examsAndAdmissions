package com.tarento.upsmf.examsAndAdmissions.service.impl;

import com.tarento.upsmf.examsAndAdmissions.model.ResponseDto;
import com.tarento.upsmf.examsAndAdmissions.model.UploadedFile;
import com.tarento.upsmf.examsAndAdmissions.repository.FileUploadRepository;
import com.tarento.upsmf.examsAndAdmissions.service.FileService;
import com.tarento.upsmf.examsAndAdmissions.service.StorageService;
import com.tarento.upsmf.examsAndAdmissions.util.Constants;
import com.tarento.upsmf.examsAndAdmissions.util.ServerProperties;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FileServiceImpl implements FileService {
    @Autowired
    StorageService storageService;
    @Autowired
    ServerProperties serverConfig;
    @Autowired
    FileUploadRepository repository;
    private final Logger log = LoggerFactory.getLogger(getClass().getName());

    @Override
    public ResponseEntity<?> downloadFile(String fileName) {
        try {
            storageService.downloadFile(fileName);
            Path tmpPath = Paths.get(Constants.LOCAL_BASE_PATH + fileName);
            ByteArrayResource resource = new ByteArrayResource(Files.readAllBytes(tmpPath));
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"");
            return ResponseEntity.ok()
                    .headers(headers)
                    .contentLength(tmpPath.toFile().length())
                    .contentType(MediaType.parseMediaType(MediaType.MULTIPART_FORM_DATA_VALUE))
                    .body(resource);
        } catch (IOException e) {
            log.error("Failed to read the downloaded file: " + fileName + ", Exception: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } finally {
            try {
                File file = new File(Constants.LOCAL_BASE_PATH + fileName);
                if (file.exists()) {
                    file.delete();
                }
            } catch (Exception e1) {
            }
        }
    }

    @Override
    public ResponseDto bulkUpload(MultipartFile mFile, String userId) {
        ResponseDto response = new ResponseDto(Constants.API_USER_BULK_UPLOAD);
        try {
            ResponseDto uploadResponse = storageService.uploadFile(mFile, serverConfig.getBulkUploadContainerName());
            if (!HttpStatus.OK.equals(uploadResponse.getResponseCode())) {
                setErrorData(response, String.format("Failed to upload file. Error: %s",
                        uploadResponse.getParams().getErrmsg()));
                return response;
            }

            Map<String, Object> uploadedFile = new HashMap<>();
            uploadedFile.put(Constants.IDENTIFIER, UUID.randomUUID().toString());
            uploadedFile.put(Constants.FILE_NAME, uploadResponse.getResult().get(Constants.NAME));
            uploadedFile.put(Constants.FILE_PATH, uploadResponse.getResult().get(Constants.URL));
            uploadedFile.put(Constants.DATE_CREATED_ON, new Timestamp(System.currentTimeMillis()));
            uploadedFile.put(Constants.STATUS, Constants.INITIATED_CAPITAL);
            uploadedFile.put(Constants.COMMENT, StringUtils.EMPTY);
            uploadedFile.put(Constants.CREATED_BY, userId);

            UploadedFile UploadedFileData = new UploadedFile();
            UploadedFileData.setIdentifier(UUID.randomUUID().toString());
            UploadedFileData.setFileName((String) uploadedFile.get(Constants.FILE_NAME));
            UploadedFileData.setFilePath((String) uploadedFile.get(Constants.FILE_PATH));
            UploadedFileData.setDateCreatedOn(new Timestamp(System.currentTimeMillis()));
            UploadedFileData.setStatus(Constants.INITIATED_CAPITAL);
            UploadedFileData.setComment(StringUtils.EMPTY);
            UploadedFileData.setCreatedBy(userId);

            //If needed will add a postgres table and insert data into it

            ResponseDto insertResponse = repository.save(UploadedFileData);

            if (!Constants.SUCCESS.equalsIgnoreCase((String) insertResponse.get(Constants.RESPONSE))) {
                setErrorData(response, "Failed to update database with user bulk upload file details.");
                return response;
            }

            response.getParams().setStatus(Constants.SUCCESS);
            response.setResponseCode(HttpStatus.OK);
            response.getResult().putAll(uploadedFile);
//            uploadedFile.put(Constants.ORG_NAME, channel);
//            kafkaProducer.push(serverConfig.getUserBulkUploadTopic(), uploadedFile);
        } catch (Exception e) {
            setErrorData(response,
                    String.format("Failed to process user bulk upload request. Error: ", e.getMessage()));
        }
        return response;
    }

    private void setErrorData(ResponseDto response, String errMsg) {
        response.getParams().setStatus(Constants.FAILED);
        response.getParams().setErrmsg(errMsg);
        response.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
