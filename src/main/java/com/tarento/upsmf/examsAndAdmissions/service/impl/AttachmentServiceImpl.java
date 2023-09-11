package com.tarento.upsmf.examsAndAdmissions.service.impl;

import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.storage.*;
import com.tarento.upsmf.examsAndAdmissions.model.QuestionPaper;
import com.tarento.upsmf.examsAndAdmissions.model.ResponseDto;
import com.tarento.upsmf.examsAndAdmissions.model.dto.DownloadDto;
import com.tarento.upsmf.examsAndAdmissions.repository.QuestionPaperRepository;
import com.tarento.upsmf.examsAndAdmissions.service.AttachmentService;
import com.tarento.upsmf.examsAndAdmissions.util.Constants;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Timestamp;
import java.time.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;


@Service
@Slf4j
public class AttachmentServiceImpl implements AttachmentService {

    @Value("${gcp.config.file.path}")
    private String gcpConfigFilePath;

    @Value("${gcp.bucket.name}")
    private String gcpBucketName;

    @Value("${gcp.bucket.folder.name}")
    private String gcpFolderName;

    @Value("${gcp.max.file.size}")
    private String gcpMaxFileSize;

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

    @Value("${gcp.sub.folder.path}")
    private String subFolderPath;

    @Value("${question.paper.total.marks}")
    private Long totalMarks;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private QuestionPaperRepository questionPaperRepository;

    @Override
    public ResponseEntity<?> downloadQuestionPaper(DownloadDto downloadDto) {
        LocalDate examDate = downloadDto.getExamDate();
        LocalTime examTime = downloadDto.getExamStartingTime();
        Long questionPaperId = downloadDto.getQuestionPaperId();
        //Query to get gcpFileName
        String fileName = jdbcTemplate.queryForObject(Constants.GCP_FILE_NAME_QUERY, new Object[]{questionPaperId}, String.class);
        try {
            Calendar currentCalendar = Calendar.getInstance();

            // Check if the current date is the same as the exam date
            if (currentCalendar.get(Calendar.YEAR) == examDate.getYear() &&
                    currentCalendar.get(Calendar.MONTH) == examDate.getMonthValue() &&
                    currentCalendar.get(Calendar.DAY_OF_MONTH) == examDate.getDayOfMonth()) {
                ZoneId zoneId = ZoneId.systemDefault();
                ZonedDateTime currentTime = ZonedDateTime.now(zoneId);
                LocalTime currentLocalTime = currentTime.toLocalTime();
                // Calculate the time difference

                Duration duration = Duration.between(currentLocalTime, examTime);

                // Check if the difference is less than 30 minutes
                if (duration.toMinutes() < 30) {
                    // Set the local file path where you want to save the downloaded file
                    Path filePath = Files.createTempFile(fileName.split("\\.")[0].substring(fileName.indexOf("/") + 1), fileName.split("\\.")[1]);
                    Blob blob = getBlob(fileName);
                    if (blob != null) {
                        blob.downloadTo(filePath);
                        log.info("File downloaded successfully to: " + filePath);
                        return ResponseEntity.ok().body("File downloaded successfully to: " + filePath);
                    } else {
                        log.info("File not found in the bucket: " + fileName);
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("File not found in the bucket");
                    }
                } else {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Institutes will not be allow to view the question paper on their portal 30 minutes before an exam");
                }
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Today is not the exam date.");
            }
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

    private Blob getBlob(String fileName) throws IOException {
        ServiceAccountCredentials credentials = ServiceAccountCredentials.fromPkcs8(gcpClientId, gcpClientEmail,
                gcpPkcsKey, gcpPrivateKeyId, new ArrayList<String>());

        // Initialize Google Cloud Storage client
        Storage storage = StorageOptions.newBuilder()
                .setProjectId(gcpProjectId)
                .setCredentials(credentials)
                .build()
                .getService();

        // Create BlobId for the object you want to download
        BlobId blobId = BlobId.of(gcpBucketName, fileName);

        // Download the object
        Blob blob = storage.get(blobId);
        return blob;
    }

    @Override
    public ResponseDto getPreviewUrl(Long questionPaperId) {
        ResponseDto response = new ResponseDto(Constants.API_QUESTION_PAPER_PREVIEW);
        //Query to get gcpFileName
        String fileName = jdbcTemplate.queryForObject(Constants.GCP_FILE_NAME_QUERY, new Object[]{questionPaperId}, String.class);
        try {
            Blob blob = getBlob(fileName);
            if (blob != null) {
                // Construct the URL for previewing the PDF
                //return "https://cdnjs.cloudflare.com/ajax/libs/pdf.js/2.13.25/web/viewer.html?file=" + blob.getMediaLink();
                response.put(Constants.MESSAGE, Constants.SUCCESSFUL);
                response.put(Constants.RESPONSE, blob.getMediaLink());
                response.setResponseCode(HttpStatus.OK);
            } else {
                log.info("Unable to retrieve the PDF file from GCS ");
                response.put(Constants.MESSAGE, "Unable to retrieve the PDF file from GCS");
                response.put(Constants.RESPONSE, Constants.MESSAGE);
                response.setResponseCode(HttpStatus.NOT_FOUND);
            }
        } catch (IOException e) {
            log.error("Failed to preview the file: " + fileName + ", Exception: ", e);
            response.put(Constants.MESSAGE, "Failed to preview the file");
            response.put(Constants.RESPONSE, Constants.MESSAGE);
            response.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return response;
    }

    @Override
    public ResponseDto upload(QuestionPaper questionPaper, String userId, MultipartFile file) {
        ResponseDto response = new ResponseDto(Constants.API_USER_BULK_UPLOAD);
        Path filePath = null;
        try {
            // validate file
            String fileName = file.getOriginalFilename();
            filePath = Files.createTempFile(fileName.split("\\.")[0], fileName.split("\\.")[1]);
            file.transferTo(filePath);
            validateFile(filePath);
            // create credentials
            ServiceAccountCredentials credentials = ServiceAccountCredentials.fromPkcs8(gcpClientId, gcpClientEmail,
                    gcpPkcsKey, gcpPrivateKeyId, new ArrayList<String>());
            log.info("credentials created");
            Storage storage = StorageOptions.newBuilder().setProjectId(gcpProjectId).setCredentials(credentials).build().getService();
            log.info("storage object created");
            String gcpFileName = gcpFolderName + "/" + Calendar.getInstance().getTimeInMillis() + "_" + fileName;
            BlobId blobId = BlobId.of(gcpBucketName, gcpFileName);
            BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();
            storage.create(blobInfo, new FileInputStream(filePath.toFile()));

            Map<String, Object> uploadedFile = new HashMap<>();
            uploadedFile.put(Constants.FILE_NAME, fileName);
            uploadedFile.put(Constants.GCP_FILE_NAME, gcpFileName);
            uploadedFile.put(Constants.EXAM_CYCLE_ID, questionPaper.getExamCycleId());
            uploadedFile.put(Constants.EXAM_CYCLE_NAME, questionPaper.getExamCycleName());
            uploadedFile.put(Constants.EXAM_DATE, questionPaper.getExamDate());
            uploadedFile.put(Constants.COURSE_NAME, questionPaper.getCourseName());
            uploadedFile.put(Constants.EXAM_NAME, questionPaper.getExamName());
            uploadedFile.put(Constants.FILE_PATH, filePath);
            uploadedFile.put(Constants.CREATED_BY, userId);
            uploadedFile.put(Constants.DATE_CREATED_ON, new Timestamp(System.currentTimeMillis()));
            uploadedFile.put(Constants.MODIFIED_BY, userId);
            uploadedFile.put(Constants.DATE_MODIFIED_ON, new Timestamp(System.currentTimeMillis()));
            uploadedFile.put(Constants.TOTAL_MARKS, totalMarks);

            QuestionPaper uploadData = new QuestionPaper();
            uploadData.setFileName(fileName);
            uploadData.setGcpFileName(gcpFileName);
            uploadData.setExamDate(questionPaper.getExamDate());
            uploadData.setExamCycleId(questionPaper.getExamCycleId());
            uploadData.setExamCycleName(questionPaper.getExamCycleName());
            uploadData.setCourseName(questionPaper.getCourseName());
            uploadData.setExamName(questionPaper.getExamName());
            uploadData.setFilePath(filePath.toString());
            uploadData.setCreatedOn(new Timestamp(System.currentTimeMillis()));
            uploadData.setModifiedOn(new Timestamp(System.currentTimeMillis()));
            uploadData.setCreatedBy(userId);
            uploadData.setModifiedBy(userId);
            uploadData.setTotalMarks(totalMarks);

            questionPaperRepository.save(uploadData);

            response.getParams().setStatus(Constants.SUCCESSFUL);
            response.setResponseCode(HttpStatus.OK);
            response.getResult().putAll(uploadedFile);
        } catch (IOException e) {
            log.error("Error while uploading attachment", e);
            setErrorData(response,
                    String.format("Error while uploading file.", e.getMessage()));
        } finally {
            if (filePath != null) {
                try {
                    Files.delete(filePath);
                } catch (IOException e) {
                    log.error("Unable to delete temp file", e);
                }
            }
        }
        return response;
    }

    private void setErrorData(ResponseDto response, String errMsg) {
        response.getParams().setStatus(Constants.FAILED);
        response.getParams().setErrmsg(errMsg);
        response.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private boolean validateFile(Path path) throws IOException {
        if (Files.isExecutable(path)) {
            throw new RuntimeException("Invalid file");
        }
        Tika tika = new Tika();
        String fileExt = tika.detect(path);
        if (fileExt.equalsIgnoreCase("application/pdf")) {
            return true;
        } else if (fileExt.startsWith("image")) {
            return true;
        }
        throw new RuntimeException("Invalid file type. Supported files are PDF and Images.");
    }
}
