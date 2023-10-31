package com.tarento.upsmf.examsAndAdmissions.service;

import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.storage.*;
import com.tarento.upsmf.examsAndAdmissions.enums.DispatchStatus;
import com.tarento.upsmf.examsAndAdmissions.model.*;
import com.tarento.upsmf.examsAndAdmissions.model.dto.ExamDispatchStatusDto;
import com.tarento.upsmf.examsAndAdmissions.model.dto.InstituteDispatchStatusDto;
import com.tarento.upsmf.examsAndAdmissions.repository.*;
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
import java.net.URL;
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
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

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

    @Autowired
    private ExamCenterRepository examCenterRepository;

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
    public ResponseDto uploadDispatchProof(Long examCycleId, Long examId,Long examCenterId, MultipartFile dispatchProofFile) throws IOException {
        ResponseDto response = new ResponseDto(Constants.API_UPLOAD_DISPATCH_DETAILS);

        try {
            ExamCycle examCycle = examCycleRepository.findById(examCycleId).orElseThrow(() -> new EntityNotFoundException("Exam cycle not found"));
            Exam exam = examRepository.findById(examId).orElseThrow(() -> new EntityNotFoundException("Exam not found"));
            ExamCenter examCenter = examCenterRepository.findById(examCenterId).orElseThrow(() -> new EntityNotFoundException("Exam center invalid"));

            Path filePath = Files.createTempFile(dispatchProofFile.getOriginalFilename().split("\\.")[0], "." + dispatchProofFile.getOriginalFilename().split("\\.")[1]);
            dispatchProofFile.transferTo(filePath);
            Storage storage = getGcsStorage();
            String gcpFileName = gcpFolderName + "/" + Calendar.getInstance().getTimeInMillis() + "_" + dispatchProofFile.getOriginalFilename();
            BlobId blobId = BlobId.of(gcpBucketName, gcpFileName);
            BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();
            storage.create(blobInfo, new FileInputStream(filePath.toFile()));

            DispatchTracker dispatchTracker = new DispatchTracker();
            dispatchTracker.setExamCycle(examCycle);
            dispatchTracker.setExam(exam);
            dispatchTracker.setExamCenter(examCenter);
            dispatchTracker.setDispatchDate(LocalDate.now());
            dispatchTracker.setDispatchProofFileLocation(gcpFileName);
            dispatchTracker.setDispatchStatus(DispatchStatus.DISPATCHED);
            dispatchTracker.setDispatchLastDate(Constants.LAST_DATE_TO_UPLOAD);
            dispatchTrackerRepository.save(dispatchTracker);

            response.put(Constants.MESSAGE, "Dispatch proof uploaded successfully.");
            response.put(Constants.RESPONSE, dispatchTracker);
            response.setResponseCode(HttpStatus.OK);
        } catch (Exception e) {
            ResponseDto.setErrorResponse(response, "UPLOAD_FAILED", "Failed to upload dispatch proof due to: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return response;
    }

    public ResponseDto getDispatchListAll(Long examCycleId, Long examId) {
        ResponseDto response = new ResponseDto(Constants.API_DISPATCH_GET_FOR_INSTITUTE);

        List<DispatchTracker> result = dispatchTrackerRepository.findByExamCycleIdAndExamCycleId(examCycleId,examId);
        if (!result.isEmpty()) {
            Map<String, Object> dataMap = new HashMap<>();
            dataMap.put("data",result);
            response.put(Constants.MESSAGE, Constants.SUCCESSFUL);
            response.put(Constants.RESPONSE, dataMap);
            response.setResponseCode(HttpStatus.OK);
        } else {
            ResponseDto.setErrorResponse(response, "NO_DISPATCH_TRACKERS", "No dispatch trackers found for the given exam cycle ID and exam ID.", HttpStatus.NOT_FOUND);
        }
        return response;
    }

    public ResponseDto getDispatchList(Long examCycleId, Long examCenterId) {
        ResponseDto response = new ResponseDto(Constants.API_DISPATCH_GET_FOR_ADMIN);

        Optional<DispatchTracker> result = dispatchTrackerRepository.findByExamCycleId(examCycleId);
        System.out.println(result);
        if (result.isPresent()) {
            Map<String, Object> dataMap = new HashMap<>();
            dataMap.put("data",result);
            response.put(Constants.MESSAGE, Constants.SUCCESSFUL);
            response.put(Constants.RESPONSE, dataMap);
            response.setResponseCode(HttpStatus.OK);
        } else {
            ResponseDto.setErrorResponse(response, "NO_DISPATCH_TRACKERS", "No dispatch trackers found for the given exam cycle ID and examCenter ID.", HttpStatus.NOT_FOUND);
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
    public ResponseDto getDispatchStatusByExamCenterAndExamCycle(Long examCenterId, Long examCycleId) {
        ResponseDto response = new ResponseDto(Constants.API_DISPATCH_STATUS_BY_EXAM_AND_CENTER);

        List<Exam> allExamsForCycle = examRepository.findByExamCycleId(examCycleId);
        List<DispatchTracker> uploadedProofs = dispatchTrackerRepository.findByExamCenterIdAndExamCycleId(examCenterId, examCycleId);

        List<ExamDispatchStatusDto> result = new ArrayList<>();

        for (Exam exam : allExamsForCycle) {
            ExamDispatchStatusDto statusDto = new ExamDispatchStatusDto();
            statusDto.setExamId(exam.getId());
            statusDto.setExamName(exam.getExamName());

            DispatchTracker matchedDispatch = uploadedProofs.stream()
                    .filter(dispatch -> dispatch.getExam().getId().equals(exam.getId()))
                    .findFirst()
                    .orElse(null);

            if (matchedDispatch != null) {
                statusDto.setProofUploaded(true);
                statusDto.setUpdatedDate(matchedDispatch.getDispatchDate());
                statusDto.setDispatchProofFileLocation(generateSignedUrl(matchedDispatch.getDispatchProofFileLocation()));  // Use the method here
                statusDto.setLastDateToUpload(matchedDispatch.getDispatchLastDate()); // Assuming DispatchLastDate is of type Date
            } else {
                statusDto.setProofUploaded(false);
            }

            result.add(statusDto);
        }

        if (!result.isEmpty()) {
            response.put(Constants.MESSAGE, Constants.SUCCESSMESSAGE);
            response.put(Constants.RESPONSE, result);
            response.setResponseCode(HttpStatus.OK);
        } else {
            ResponseDto.setErrorResponse(response, "NO_DATA_FOUND", "No dispatch data found for given exam center and exam cycle.", HttpStatus.NOT_FOUND);
        }

        return response;
    }

    public ResponseDto getDispatchStatusForAllInstitutes(Long examCycleId, Long examId) {
        ResponseDto response = new ResponseDto(Constants.API_DISPATCH_STATUS_FOR_ALL_INSTITUTES);

        List<ExamCenter> allInstitutes = examCenterRepository.findByExamCycle_Id(examCycleId);
        List<DispatchTracker> uploadedProofsForExam = dispatchTrackerRepository.findByExamIdAndExamCycleId(examId, examCycleId);
        Exam exam = examRepository.findById(examId).orElse(null);  // Fetch the exam details

        // Creating a map of Institute ID to DispatchTracker for quick lookup
        Map<Long, DispatchTracker> dispatchTrackerMap = uploadedProofsForExam.stream()
                .collect(Collectors.toMap(dispatch -> dispatch.getExamCenter().getId(), Function.identity()));

        List<InstituteDispatchStatusDto> result = new ArrayList<>();

        for (ExamCenter institute : allInstitutes) {
            InstituteDispatchStatusDto statusDto = new InstituteDispatchStatusDto();
            statusDto.setInstituteId(institute.getId());
            statusDto.setInstituteName(institute.getName());
            statusDto.setExamName(exam != null ? exam.getExamName() : null);

            DispatchTracker matchedDispatch = dispatchTrackerMap.get(institute.getId());

            if (matchedDispatch != null) {
                statusDto.setProofUploaded(true);
                statusDto.setUpdatedDate(matchedDispatch.getDispatchDate());
                statusDto.setDispatchProofFileLocation(generateSignedUrl(matchedDispatch.getDispatchProofFileLocation()));
            } else {
                statusDto.setProofUploaded(false);
                // Here you can set default values or placeholders for other fields if needed
            }

            result.add(statusDto);
        }

        if (!result.isEmpty()) {
            response.put(Constants.MESSAGE, Constants.SUCCESSMESSAGE);
            response.put(Constants.RESPONSE, result);
            response.setResponseCode(HttpStatus.OK);
        } else {
            ResponseDto.setErrorResponse(response, "NO_DATA_FOUND", "No dispatch data found for the given exam and exam cycle across all institutes.", HttpStatus.NOT_FOUND);
        }

        return response;
    }


    private String generateSignedUrl(String blobName) {
        try {
            // Define resource
            BlobId blobId = BlobId.of(gcpBucketName, blobName);
            BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();

            // Define signed URL options
            long durationMinutes = 15; // Duration for which the URL is valid (e.g., 15 minutes)
            URL signedUrl = getGcsStorage().signUrl(blobInfo, durationMinutes, TimeUnit.MINUTES,
                    Storage.SignUrlOption.httpMethod(HttpMethod.GET),
                    Storage.SignUrlOption.withV4Signature(),
                    Storage.SignUrlOption.withVirtualHostedStyle());

            return signedUrl.toString();

        } catch (Exception e) {
            log.error("Error generating signed URL for blob: " + blobName, e);
            return null;
        }
    }
    private Storage getGcsStorage() throws IOException {
        ServiceAccountCredentials credentials = ServiceAccountCredentials.fromPkcs8(gcpClientId, gcpClientEmail,
                gcpPkcsKey, gcpPrivateKeyId, new ArrayList<>());
        return StorageOptions.newBuilder().setProjectId(gcpProjectId).setCredentials(credentials).build().getService();
    }

}
