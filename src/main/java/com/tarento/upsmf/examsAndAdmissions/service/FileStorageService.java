package com.tarento.upsmf.examsAndAdmissions.service;

import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.storage.*;
import com.tarento.upsmf.examsAndAdmissions.enums.DocumentType;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Objects;
import java.util.UUID;

@Service
@Slf4j
public class FileStorageService {

    @Value("${gcp.bucket.name}")
    private String gcpBucketName;

    @Value("${gcp.project.id}")
    private String gcpProjectId;

    @Value("${gcp.client.id}")
    private String gcpClientId;

    @Value("${gcp.client.email}")
    private String gcpClientEmail;

    @Value("${gcp.pkcs.key}")
    private String gcpPkcsKey;

    @Value("${gcp.private.key.id}")
    private String gcpPrivateKeyId;

        public String storeFile(MultipartFile file, DocumentType docType) throws IOException {
            validateFileContent(file);
            Path tempFilePath = createTempFile(file, docType);
            try {
                return uploadToGCP(tempFilePath, docType);
            } finally {
                Files.delete(tempFilePath);
            }
        }

        private void validateFileContent(MultipartFile file) throws IOException {
            Tika tika = new Tika();
            String mimeType = tika.detect(file.getBytes());
            if (!mimeType.equalsIgnoreCase("application/pdf") && !mimeType.startsWith("image")) {
                throw new RuntimeException("Invalid file type. Supported files are PDF and Images.");
            }
        }

    private Path createTempFile(MultipartFile file, DocumentType docType) throws IOException {
        String originalFilename = file.getOriginalFilename();
        String prefix = originalFilename != null ? originalFilename.substring(0, Math.min(originalFilename.length(), 10)) : "tempfile"; // use the first 10 chars of original file name or default to "tempfile"
        String extension = originalFilename != null ? originalFilename.substring(originalFilename.lastIndexOf(".")) : ".tmp";

        Path dir = Paths.get("/tmp/hall_tickets/"); // ensure this directory exists
        Files.createDirectories(dir); // creates directory if it doesn't exist

        Path filePath = Files.createTempFile(dir, prefix + "_", extension);
        file.transferTo(filePath);
        return filePath;
    }

    private String uploadToGCP(Path tempFilePath, DocumentType docType) throws IOException {
            ServiceAccountCredentials credentials = ServiceAccountCredentials.fromPkcs8(gcpClientId, gcpClientEmail,
                    gcpPkcsKey, gcpPrivateKeyId, new ArrayList<>());
            Storage storage = StorageOptions.newBuilder().setProjectId(gcpProjectId).setCredentials(credentials).build().getService();

            String cloudPath = String.format("%s/%d_%s", docType.getFolderName(), System.currentTimeMillis(), tempFilePath.getFileName());
            BlobId blobId = BlobId.of(gcpBucketName, cloudPath);
            BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();

            try (FileInputStream fileInputStream = new FileInputStream(tempFilePath.toFile())) {
                Blob blob = storage.create(blobInfo, fileInputStream);
                return blob.getMediaLink();
            }
        }
    }