package com.tarento.upsmf.examsAndAdmissions.service;

import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.storage.*;
import com.tarento.upsmf.examsAndAdmissions.model.*;
import com.tarento.upsmf.examsAndAdmissions.repository.CourseRepository;
import com.tarento.upsmf.examsAndAdmissions.repository.DispatchTrackerRepository;
import com.tarento.upsmf.examsAndAdmissions.repository.ExamCycleRepository;
import com.tarento.upsmf.examsAndAdmissions.repository.ExamRepository;
import com.tarento.upsmf.examsAndAdmissions.util.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityNotFoundException;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.*;

@Service
@Slf4j
public class DispatchTrackerService {

    @Autowired
    private DispatchTrackerRepository dispatchTrackerRepository;

    @Autowired
    private ExamRepository examRepository;

    @Autowired
    private ExamCycleRepository examCycleRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Value("${gcp.bucket.folder.name}")
    private String gcpFolderName;
    @Value("${gcp.bucket.name}")
    private String gcpBucketName;
    @Value("${gcp.client.id}")
    private String gcpClientId;
    @Value("${gcp.client.email}")
    private String gcpClientEmail;
    @Value("${gcp.pkcs.key}")
    private String gcpPkcsKey;
    @Value("${gcp.private.key.id}")
    private String gcpPrivateKeyId;
    @Value("${gcp.project.id}")
    private String gcpProjectId;
    public ResponseDto uploadDispatchProof(Long examCycleId, Long examId, MultipartFile dispatchProofFile, String dispatchDate) throws IOException {
        ResponseDto response = new ResponseDto(Constants.API_UPLOAD_DISPATCH_DETAILS);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        Date dob;
        LocalDate localDate;
        try {
            dob = formatter.parse(dispatchDate);
            localDate = dob.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        } catch (ParseException e) {
            ResponseDto.setErrorResponse(response, "INVALID_DATE_FORMAT", "Invalid date format", HttpStatus.BAD_REQUEST);
            return response;
        }

        try {
            ExamCycle examCycle = examCycleRepository.findById(examCycleId).orElseThrow(() -> new EntityNotFoundException("Exam cycle not found"));
            Exam exam = examRepository.findById(examId).orElseThrow(() -> new EntityNotFoundException("Exam not found"));

            Path filePath = Files.createTempFile(dispatchProofFile.getOriginalFilename().split("\\.")[0], "." + dispatchProofFile.getOriginalFilename().split("\\.")[1]);
            dispatchProofFile.transferTo(filePath);

            ServiceAccountCredentials credentials = ServiceAccountCredentials.fromPkcs8(gcpClientId, gcpClientEmail,
                    gcpPkcsKey, gcpPrivateKeyId, new ArrayList<String>());
            Storage storage = StorageOptions.newBuilder().setProjectId(gcpProjectId).setCredentials(credentials).build().getService();
            String gcpFileName = gcpFolderName + "/" + Calendar.getInstance().getTimeInMillis() + "_" + dispatchProofFile.getOriginalFilename();
            BlobId blobId = BlobId.of(gcpBucketName, gcpFileName);
            BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();
            storage.create(blobInfo, new FileInputStream(filePath.toFile()));

            DispatchTracker dispatchTracker = new DispatchTracker();
            dispatchTracker.setExamCycle(examCycle);
            dispatchTracker.setExam(exam);
            dispatchTracker.setDispatchDate(localDate);
            dispatchTracker.setDispatchProofFileLocation(gcpFileName);

            dispatchTrackerRepository.save(dispatchTracker);

            response.put(Constants.MESSAGE, "Dispatch proof uploaded successfully.");
            response.put(Constants.RESPONSE, dispatchTracker);
            response.setResponseCode(HttpStatus.OK);
        } catch (Exception e) {
            ResponseDto.setErrorResponse(response, "UPLOAD_FAILED", "Failed to upload dispatch proof due to: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return response;
    }

    public ResponseDto getDispatchList(Long examCycleId, Long examId) {
        ResponseDto response = new ResponseDto(Constants.API_DISPATCH_GET); // Replace with the appropriate constant for this API

        List<DispatchTracker> result = dispatchTrackerRepository.findByExamCycleIdAndExamId(examCycleId, examId);

        if (!result.isEmpty()) {
            ExamCycle examCycleDetails = result.get(0).getExamCycle();
            Map<String, Object> dataMap = new HashMap<>();

            if (examCycleDetails != null && examCycleDetails.getCourse() != null) { // Check if course is not null
                Long courseId = examCycleDetails.getCourse().getId();
                Optional<Course> course = courseRepository.findById(courseId);

                if (course.isPresent()) {
                    Institute institute = course.get().getInstitute();
                    dataMap.put("data", result);
                    dataMap.put("lastDateToUpload", Constants.LAST_DATE_TO_UPLOAD);
                    dataMap.put("instituteName", institute.getInstituteName());
                    dataMap.put("instituteId", institute.getId());
                    dataMap.put("examName", result.get(0).getExam().getExamName());
                    dataMap.put("dispatchStatus", result.get(0).getDispatchStatus());
                }
            }
            response.put(Constants.MESSAGE, Constants.SUCCESSFUL);
            response.put(Constants.RESPONSE, dataMap);
            response.setResponseCode(HttpStatus.OK);
        } else {
            ResponseDto.setErrorResponse(response, "NO_DISPATCH_TRACKERS", "No dispatch trackers found for the given exam cycle ID and exam ID.", HttpStatus.NOT_FOUND);
        }

        return response;
    }

    public ResponseDto getDispatchListByExamCycle(Long examCycleId) {
        ResponseDto response = new ResponseDto(Constants.API_DISPATCH_GET); // Assuming you have a constant for this API

        Optional<DispatchTracker> optionalResult = dispatchTrackerRepository.findByExamCycleId(examCycleId);

        if (optionalResult.isPresent()) {
            DispatchTracker dispatchTracker = optionalResult.get();
            ExamCycle examCycleDetails = dispatchTracker.getExamCycle();
            List<Exam> exams = examRepository.findByExamCycleId(examCycleId);

            Map<String, Object> dataMap = new HashMap<>();

            if (examCycleDetails != null) {
                Long courseId = examCycleDetails.getCourse().getId();
                Optional<Course> course = courseRepository.findById(courseId);
                if (course.isPresent()) {
                    Institute institute = course.get().getInstitute();
                    dataMap.put("examCycle", dispatchTracker);
                    dataMap.put("exams", exams);
                    dataMap.put("lastDateToUpload", Constants.LAST_DATE_TO_UPLOAD);
                    dataMap.put("instituteName", institute.getInstituteName());
                    dataMap.put("instituteId", institute.getId());
                    dataMap.put("examName", dispatchTracker.getExam().getExamName());
                    dataMap.put("dispatchStatus", dispatchTracker.getDispatchStatus());
                }
            }
            response.put(Constants.MESSAGE, Constants.SUCCESSFUL);
            response.put(Constants.RESPONSE, dataMap);
            response.setResponseCode(HttpStatus.OK);
        } else {
            ResponseDto.setErrorResponse(response, "NO_DISPATCH_TRACKERS", "No dispatch trackers found for the given exam cycle ID.", HttpStatus.NOT_FOUND);
        }

        return response;
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
    public ResponseDto getPreviewUrl(Long dispatchTrackerId) {
        ResponseDto response = new ResponseDto(Constants.API_DISPATCH_PROOF_PREVIEW);
        //Query to get gcpFileName
        DispatchTracker dispatchTracker = dispatchTrackerRepository.findById(dispatchTrackerId).orElse(null);
        assert dispatchTracker != null;
        String fileName = dispatchTracker.getDispatchProofFileLocation();
        try {
            Blob blob = getBlob(fileName);
            if (blob != null) {
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
}
