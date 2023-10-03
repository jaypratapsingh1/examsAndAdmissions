package com.tarento.upsmf.examsAndAdmissions.service;

import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.tarento.upsmf.examsAndAdmissions.model.DispatchTracker;
import com.tarento.upsmf.examsAndAdmissions.model.Exam;
import com.tarento.upsmf.examsAndAdmissions.model.ExamCycle;
import com.tarento.upsmf.examsAndAdmissions.model.ResponseDto;
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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

@Service
@Slf4j
public class DispatchTrackerService {

    @Autowired
    private DispatchTrackerRepository dispatchTrackerRepository;

    @Autowired
    private ExamRepository examRepository;

    @Autowired
    private ExamCycleRepository examCycleRepository;

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
    public ResponseDto uploadDispatchProof(Long examCycleId, Long examId, MultipartFile dispatchProofFile, LocalDate dispatchDate) throws IOException {
        ResponseDto response = new ResponseDto(Constants.API_UPLOAD_DISPATCH_DETAILS);
        ExamCycle examCycle = examCycleRepository.findById(examCycleId).orElseThrow(() -> new EntityNotFoundException("Exam cycle not found"));
        Exam exam = examRepository.findById(examId).orElseThrow(() -> new EntityNotFoundException("Exam not found"));

        Path filePath = null;
        String fileName = dispatchProofFile.getOriginalFilename();
        filePath = Files.createTempFile(fileName.split("\\.")[0], fileName.split("\\.")[1]);
        dispatchProofFile.transferTo(filePath);

        ServiceAccountCredentials credentials = ServiceAccountCredentials.fromPkcs8(gcpClientId, gcpClientEmail,
                gcpPkcsKey, gcpPrivateKeyId, new ArrayList<String>());
        log.info("credentials created");
        Storage storage = StorageOptions.newBuilder().setProjectId(gcpProjectId).setCredentials(credentials).build().getService();
        log.info("storage object created");
        String gcpFileName = gcpFolderName + "/" + Calendar.getInstance().getTimeInMillis() + "_" + fileName;
        BlobId blobId = BlobId.of(gcpBucketName, gcpFileName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();
        storage.create(blobInfo, new FileInputStream(filePath.toFile()));

        DispatchTracker dispatchTracker = new DispatchTracker();
        dispatchTracker.setExamCycle(examCycle);
        dispatchTracker.setExam(exam);
        dispatchTracker.setDispatchDate(dispatchDate);
        dispatchTracker.setDispatchProofFileLocation(gcpFileName);

        dispatchTrackerRepository.save(dispatchTracker);
        return response;
    }

    public ResponseDto getDispatchList(Long examCycleId, Long examId) {
        ResponseDto response = new ResponseDto(Constants.API_GET_DISPATCH_LIST);
        List<DispatchTracker> list =  dispatchTrackerRepository.findByExamCycleIdAndExamId(examCycleId, examId);
        response.put(Constants.MESSAGE, Constants.SUCCESSFUL);
        response.put(Constants.RESPONSE, list);
        response.setResponseCode(HttpStatus.OK);
        return response;
    }
}
