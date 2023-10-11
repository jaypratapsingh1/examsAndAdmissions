package com.tarento.upsmf.examsAndAdmissions.service;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.tarento.upsmf.examsAndAdmissions.enums.ApprovalStatus;
import com.tarento.upsmf.examsAndAdmissions.enums.DocumentType;
import com.tarento.upsmf.examsAndAdmissions.model.*;
import com.tarento.upsmf.examsAndAdmissions.model.dto.DataCorrectionRequest;
import com.tarento.upsmf.examsAndAdmissions.model.dto.PendingDataDto;
import com.tarento.upsmf.examsAndAdmissions.repository.*;
import com.tarento.upsmf.examsAndAdmissions.util.Constants;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityManager;
import javax.persistence.criteria.*;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Slf4j
public class HallTicketService {

    @Autowired
    HallTicketRepository hallTicketRepository;
    @Autowired
    DataCorrectionRequestRepository dataCorrectionRequestRepository;
    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private StudentExamRegistrationRepository studentExamRegistrationRepository;

    @Autowired
    private ExamCenterRepository examCenterRepository;

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private ExamRepository examRepository;

    @Autowired
    private EntityManager entityManager;
    @Autowired
    private StudentService studentService;
    @Autowired
    private FileStorageService fileStorageService;

    public ResponseDto generateAndSaveHallTicketsForMultipleStudents(List<Long> studentRegistrationIds) {
        ResponseDto responseDto = new ResponseDto(Constants.API_GENERATE_AND_SAVE_HALL_TICKETS_FOR_MULTIPLE_STUDENTS);
        int successCount = 0;

        for (Long id : studentRegistrationIds) {
            try {
                Optional<StudentExamRegistration> registrationOptional = studentExamRegistrationRepository.findById(id);
                if (registrationOptional.isEmpty()) {
                    log.warn("No registration found for student ID: {}", id);
                    continue;
                }

                StudentExamRegistration registration = registrationOptional.get();
                byte[] hallTicketData = generateHallTicket(registration);
                if (hallTicketData.length == 0) {
                    log.warn("Hall ticket data is empty for student ID: {}", id);
                    continue;
                }

                MultipartFile hallTicket = new ByteArrayMultipartFile(hallTicketData, "hallticket_" + id + ".pdf");
                if (hallTicket.isEmpty()) {
                    log.warn("Converted MultipartFile is empty for student ID: {}", id);
                    continue;
                }

                String path = fileStorageService.storeFile(hallTicket, DocumentType.HALL_TICKET);
                if (path == null) {
                    setErrorResponse(responseDto, "STORAGE_ERROR", "Failed to store hall ticket for student ID: " + id, HttpStatus.INTERNAL_SERVER_ERROR);
                    return responseDto;
                } else {
                    successCount++;
                    registration.setHallTicketPath(path);
                    studentExamRegistrationRepository.save(registration);
                }

            } catch (Exception e) {
                log.error("Error processing hall ticket for student ID: " + id, e);
                setErrorResponse(responseDto, "HALLTICKET_GENERATION_ERROR", "Failed to generate hall ticket for student ID: " + id, HttpStatus.INTERNAL_SERVER_ERROR);
                return responseDto;
            }
        }
        if (successCount == studentRegistrationIds.size()) {
            responseDto.put(Constants.MESSAGE, "Hall tickets generated and saved successfully for all students.");
            responseDto.setResponseCode(HttpStatus.OK);
        } else if (successCount > 0) {
            setErrorResponse(responseDto, "PARTIAL_SUCCESS", "Hall tickets generated for " + successCount + " out of " + studentRegistrationIds.size() + " students.", HttpStatus.PARTIAL_CONTENT);
        } else {
            setErrorResponse(responseDto, "HALLTICKET_GENERATION_ERROR", "Failed to generate and save hall tickets for all students.", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return responseDto;
    }
    public ResponseDto getHallTicketForStudent(Long id, String dateOfBirth) {
        ResponseDto response = new ResponseDto(Constants.API_HALLTICKET_GET);

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        Date dob;
        LocalDate localDate;
        try {
            dob = formatter.parse(dateOfBirth);
            localDate = dob.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        } catch (ParseException e) {
            setErrorResponse(response, "INVALID_DATE_FORMAT", "Invalid date format", HttpStatus.BAD_REQUEST);
            return response;
        }

        Optional<StudentExamRegistration> registrationOptional = studentExamRegistrationRepository.findByIdAndStudent_DateOfBirth(id, localDate);
        if (registrationOptional.isEmpty()) {
            setErrorResponse(response, "STUDENT_NOT_FOUND", "No student record found for the provided details.", HttpStatus.NOT_FOUND);
            return response;
        }

        StudentExamRegistration registration = registrationOptional.get();
        String hallTicketPath = registration.getHallTicketPath();
        if (hallTicketPath == null || hallTicketPath.isEmpty()) {
            setErrorResponse(response, "HALL_TICKET_NOT_GENERATED", "Hall ticket not generated for this student.", HttpStatus.NOT_FOUND);
            return response;
        }
/*
        // Check if the hall ticket exists in Google Cloud Storage
        Blob blob = storage.get(BlobId.of("YOUR_BUCKET_NAME", hallTicketPath));
        if (blob == null || !blob.exists()) {
            setErrorResponse(response, "HALL_TICKET_NOT_FOUND_IN_STORAGE", "Hall ticket not found in storage.", HttpStatus.NOT_FOUND);
            return response;
        }

        String hallTicketUrl = blob.getMediaLink();  // Get the public URL of the stored hall ticket*/
        response.put(Constants.MESSAGE, Constants.SUCCESSMESSAGE);
        response.put(Constants.RESPONSE, hallTicketPath);  // Sending the hall ticket as a resource
        response.setResponseCode(HttpStatus.OK);
        return response;
    }
    public ResponseDto getHallTicketBlobResourcePath(Long id, String dateOfBirth) throws Exception {
        ResponseDto hallTicketResponse = getHallTicketForStudent(id, dateOfBirth);

        if (hallTicketResponse.getResponseCode().is2xxSuccessful()) {
            String hallTicketPath = (String) hallTicketResponse.getResult().get(Constants.RESPONSE);

            Blob blob = fileStorageService.getBlobFromGCP(hallTicketPath);
            if (blob == null || !blob.exists()) {
                log.error("Blob for hall ticket not found in GCS for path: {}", hallTicketPath);
                ResponseDto response = new ResponseDto(Constants.API_HALLTICKET_GET);
                ResponseDto.setErrorResponse(response, "BLOB_NOT_FOUND", "Error fetching hall ticket from storage.", HttpStatus.INTERNAL_SERVER_ERROR);
                return response;
            }

            // Generate a signed URL for direct download
            URL signedUrl = blob.signUrl(15, TimeUnit.MINUTES); // This URL will be valid for 15 minutes
            hallTicketResponse.getResult().put(Constants.RESPONSE, signedUrl);  // use the signed URL
            return hallTicketResponse;

        } else {
            ResponseDto response = new ResponseDto(Constants.API_HALLTICKET_GET);
            ResponseDto.setErrorResponse(response, "REQUEST_ERROR", "Error processing request: " + hallTicketResponse.getError().getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            return response;
        }
    }


    public ResponseDto requestHallTicketDataCorrection(Long studentId, String correctionDetails, @RequestParam("file") MultipartFile proof) throws IOException {
        ResponseDto response = new ResponseDto(Constants.API_HALLTICKET_REQUEST_DATA_CORRECTION);
        Optional<Student> optionalStudent = studentRepository.findById(studentId);
        if (!optionalStudent.isPresent()) {
            response.put(Constants.MESSAGE, "Invalid Student ID");
            response.setResponseCode(HttpStatus.BAD_REQUEST);
            return response;
        }

        DataCorrectionRequest request = new DataCorrectionRequest();
        request.setStudent(optionalStudent.get());
        request.setRequestedCorrection(correctionDetails);
        request.setStatus("NEW");
        if (proof != null && !proof.isEmpty()) {
            String path = studentService.storeFile(proof);
            request.setProofAttachmentPath(path);
        }
        dataCorrectionRequestRepository.save(request);
        response.put(Constants.MESSAGE, Constants.SUCCESSMESSAGE);
        response.put(Constants.RESPONSE, request);
        response.setResponseCode(HttpStatus.OK);
        return response;
    }

    public ResponseDto getAllDataCorrectionRequests() {
        ResponseDto response = new ResponseDto(Constants.API_HALLTICKET_GET_ALL_DATA_CORRECTION_REQUESTS);
        List<DataCorrectionRequest> requests = dataCorrectionRequestRepository.findAll();

        List<Map<String, Object>> formattedRequests = new ArrayList<>();

        for (DataCorrectionRequest request : requests) {
            Map<String, Object> formattedRequest = new HashMap<>();
            formattedRequest.put("requestId", request.getId());
            formattedRequest.put("requestedCorrection", request.getRequestedCorrection());
            formattedRequest.put("status", request.getStatus());
            formattedRequest.put("rejectionReason", request.getRejectionReason());
            formattedRequest.put("proofAttachmentPath", request.getProofAttachmentPath());

            if (request.getStudent() != null) {
                formattedRequest.put("studentName", request.getStudent().getFirstName() + " " + request.getStudent().getSurname());
                formattedRequest.put("enrollmentNumber", request.getStudent().getEnrollmentNumber());

                if (request.getStudent().getCourse() != null) {
                    formattedRequest.put("courseName", request.getStudent().getCourse().getCourseName());
                }
            }

            formattedRequests.add(formattedRequest);
        }

        if (!formattedRequests.isEmpty()) {
            response.put(Constants.MESSAGE, Constants.SUCCESSFUL);
            response.put(Constants.RESPONSE, formattedRequests);
            response.setResponseCode(HttpStatus.OK);
        } else {
            setErrorResponse(response, "NO_DATA_CORRECTION_REQUESTS", "No data correction requests found.", HttpStatus.NOT_FOUND);
        }
        return response;
    }

    public ResponseDto approveDataCorrection(Long requestId) {
        ResponseDto response = new ResponseDto(Constants.API_HALLTICKET_APPROVE_DATA_CORRECTION);
        DataCorrectionRequest request = dataCorrectionRequestRepository.findById(requestId).orElse(null);
        if (request != null) {
            request.setStatus("APPROVED");
            dataCorrectionRequestRepository.save(request);
            response.put(Constants.MESSAGE, Constants.SUCCESSMESSAGE);
            response.put(Constants.RESPONSE, request);
            response.setResponseCode(HttpStatus.OK);
        } else {
            setErrorResponse(response, "REQUEST_NOT_FOUND", "Request not found.", HttpStatus.NOT_FOUND);
        }
        return response;
    }

    public ResponseDto rejectDataCorrection(Long requestId, String rejectionReason) {
        ResponseDto response = new ResponseDto(Constants.API_HALLTICKET_REJECT_DATA_CORRECTION);
        DataCorrectionRequest request = dataCorrectionRequestRepository.findById(requestId).orElse(null);
        if (request != null) {
            request.setStatus("REJECTED");
            request.setRejectionReason(rejectionReason);
            dataCorrectionRequestRepository.save(request);
            response.put(Constants.MESSAGE, Constants.SUCCESSMESSAGE);
            response.put(Constants.RESPONSE, request);
            response.setResponseCode(HttpStatus.OK);
        } else {
            setErrorResponse(response, "REQUEST_NOT_FOUND", "Request not found.", HttpStatus.NOT_FOUND);
        }
        return response;
    }

    public ResponseDto getPendingDataForHallTickets(Long courseId, Long examCycleId, Long instituteId) {
        ResponseDto response = new ResponseDto(Constants.API_HALLTICKET_GET_PENDING_DATA);
        List<StudentExamRegistration> registrations = fetchPendingDataForHallTickets(courseId, examCycleId, instituteId);
        if (!registrations.isEmpty()) {
            List<PendingDataDto> pendingDataDtos = registrations.stream().map(this::toPendingDataDto).collect(Collectors.toList());
            response.put(Constants.MESSAGE, Constants.SUCCESSFUL);
            response.put(Constants.RESPONSE, pendingDataDtos);
            response.setResponseCode(HttpStatus.OK);
        } else {
            setErrorResponse(response, "NO_PENDING_HALLTICKETS", "No pending hall tickets found for the provided filters.", HttpStatus.NOT_FOUND);
        }
        return response;
    }

    public ResponseDto downloadProofForDataCorrectionRequest(Long requestId) {
        ResponseDto response = new ResponseDto(Constants.API_HALLTICKET_DOWNLOAD_PROOF);
        DataCorrectionRequest request = dataCorrectionRequestRepository.findById(requestId).orElse(null);
        if (request != null) {
            String proofPath = request.getProofAttachmentPath();
            Resource file = loadProofAsStreamResource(proofPath);
            response.put(Constants.MESSAGE, Constants.SUCCESSMESSAGE);
            response.put(Constants.RESPONSE, file);
            response.setResponseCode(HttpStatus.OK);
        } else {
            setErrorResponse(response, "PROOF_NOT_FOUND", "Proof not found for the request.", HttpStatus.NOT_FOUND);
        }
        return response;
    }

    public ResponseDto getProofUrlByRequestId(Long requestId) {
        ResponseDto response = new ResponseDto(Constants.API_HALLTICKET_GET_PROOF_URL_BY_REQUEST);
        DataCorrectionRequest request = dataCorrectionRequestRepository.findById(requestId).orElse(null);
        if (request != null) {
            String proofUrl = request.getProofAttachmentPath();
            response.put(Constants.MESSAGE, Constants.SUCCESSMESSAGE);
            response.put(Constants.RESPONSE, proofUrl);
            response.setResponseCode(HttpStatus.OK);
        } else {
            setErrorResponse(response, "PROOF_URL_NOT_FOUND", "Proof URL not found for the request.", HttpStatus.NOT_FOUND);
        }
        return response;
    }

    public void setErrorResponse(ResponseDto response, String code, String message, HttpStatus status) {
        ResponseDto.ErrorDetails errorDetails = new ResponseDto.ErrorDetails();
        errorDetails.setCode(code);
        errorDetails.setMessage(message);
        response.setError(errorDetails);
        response.setResponseCode(status);
    }
    private byte[] generateHallTicket(StudentExamRegistration registration) {
        PDDocument document = new PDDocument();
        // Get the exam cycle for the registration
        ExamCycle examCycle = registration.getExamCycle();
        if (examCycle == null) {
            throw new RuntimeException("Exam cycle not found for the registration.");
        }
        // Get all exams within the same exam cycle
        List<Exam> examsInCycle = examRepository.findByExamCycleId(examCycle.getId());

        if (examsInCycle == null || examsInCycle.isEmpty()) {
            throw new RuntimeException("No exams found in the exam cycle.");
        }

        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);

        try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
            // Title: Hall Ticket
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 20);
            contentStream.beginText();
            contentStream.newLineAtOffset(50, 750);
            contentStream.showText("Hall Ticket");
            contentStream.endText();

            // Student Details
            contentStream.setFont(PDType1Font.HELVETICA, 14);
            contentStream.beginText();
            contentStream.newLineAtOffset(50, 700);
            contentStream.showText("Name: " + registration.getStudent().getFirstName() + " " + registration.getStudent().getSurname());
            contentStream.newLineAtOffset(0, -20);
            contentStream.showText("Exam Enrollment Number: " + registration.getStudent().getEnrollmentNumber());
            contentStream.newLineAtOffset(0, -20);
            contentStream.showText("Date of Birth: " + registration.getStudent().getDateOfBirth());
            contentStream.newLineAtOffset(0, -20);
            contentStream.endText();

            int yOffset = 620;

            // Exam Details for each exam
            for (Exam exam : examsInCycle) {
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 14);
                contentStream.beginText();
                contentStream.newLineAtOffset(50, yOffset);
                contentStream.showText("Exam: " + exam.getExamName());
                yOffset -= 20;
                contentStream.newLineAtOffset(0, -20);
                contentStream.showText("Date: " + exam.getExamDate());
                yOffset -= 20;
                contentStream.newLineAtOffset(0, -20);
                contentStream.showText("Time: " + exam.getStartTime() + " - " + exam.getEndTime());
                yOffset -= 30; // Additional space between exams
                contentStream.endText();
            }

        } catch (IOException e) {
            throw new RuntimeException("Error generating hall ticket", e);
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            document.save(baos);
            document.close();
        } catch (IOException e) {
            throw new RuntimeException("Error saving hall ticket to byte array", e);
        }

        return baos.toByteArray();
    }
    private List<StudentExamRegistration> fetchPendingDataForHallTickets(Long courseId, Long examCycleId, Long instituteId) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<StudentExamRegistration> criteriaQuery = criteriaBuilder.createQuery(StudentExamRegistration.class);
        Root<StudentExamRegistration> registrationRoot = criteriaQuery.from(StudentExamRegistration.class);

        registrationRoot.fetch("examCycle", JoinType.INNER);
        registrationRoot.fetch("exam", JoinType.INNER);

        Join<StudentExamRegistration, ExamCycle> examCycleJoin = registrationRoot.join("examCycle");
        Join<StudentExamRegistration, Exam> examJoin = registrationRoot.join("exam");

        List<Predicate> predicates = new ArrayList<>();

        if (courseId != null) {
            predicates.add(criteriaBuilder.equal(examJoin.get("course").get("id"), courseId));
        }
        if (examCycleId != null) {
            predicates.add(criteriaBuilder.equal(examCycleJoin.get("id"), examCycleId));
        }
        if (instituteId != null) {
            predicates.add(criteriaBuilder.equal(registrationRoot.get("institute").get("id"), instituteId));
        }

        criteriaQuery.where(predicates.toArray(new Predicate[0]));

        return entityManager.createQuery(criteriaQuery).getResultList();
    }

    private PendingDataDto toPendingDataDto(StudentExamRegistration registration) {
        PendingDataDto dto = new PendingDataDto();

        // ... (existing fields mapping)
        dto.setId(registration.getId()); // setting the ID
        dto.setFirstName(registration.getStudent().getFirstName());
        dto.setLastName(registration.getStudent().getSurname());
        dto.setDob(registration.getStudent().getDateOfBirth()); // Assuming there's a dob field in Student
        dto.setCourseName(registration.getExam().getCourse().getCourseName());
        dto.setCourseYear(registration.getExam().getCourse().getCourseYear()); // Assuming there's a courseYear field in Course
        dto.setStudentEnrollmentNumber(registration.getStudent().getEnrollmentNumber());
        dto.setRegistrationDate(registration.getRegistrationDate());
        dto.setStatus(registration.getStatus());
        dto.setRemarks(registration.getRemarks());
        if (registration.getExamCenter() != null) {
            dto.setExamCenterName(registration.getExamCenter().getName());
        }
        dto.setFeesPaid(registration.isFeesPaid()); // Assuming there's a method to determine if fees are paid
        dto.setAttendancePercentage(computeAttendancePercentage(registration)); // Assuming there's a method to get attendance

        // Mapping ExamCycle details
        PendingDataDto.ExamCycleDetails examCycleDetails = new PendingDataDto.ExamCycleDetails();
        examCycleDetails.setId(registration.getExamCycle().getId());
        examCycleDetails.setName(registration.getExamCycle().getExamCycleName());
        examCycleDetails.setStartDate(registration.getExamCycle().getStartDate());
        examCycleDetails.setEndDate(registration.getExamCycle().getEndDate());

        // Fetch the exams associated with the exam cycle ID from the repository
        Long examCycleId = registration.getExamCycle().getId();
        List<Exam> examsForCycle = examRepository.findByExamCycleId(examCycleId);

        // Now, map these exams to your DTO
        List<PendingDataDto.ExamDetails> examDetailsList = examsForCycle.stream().map(exam -> {
            PendingDataDto.ExamDetails examDetails = new PendingDataDto.ExamDetails();
            examDetails.setExamName(exam.getExamName());
            examDetails.setExamDate(exam.getExamDate());
            examDetails.setStartTime(String.valueOf(exam.getStartTime()));
            examDetails.setEndTime(String.valueOf(exam.getEndTime()));
            //... map other relevant details
            return examDetails;
        }).collect(Collectors.toList());

        examCycleDetails.setExams(examDetailsList);
        dto.setExamCycle(examCycleDetails);

        return dto;
    }
    private double computeAttendancePercentage(StudentExamRegistration registration) {
        // Fetch the student enrollment number from the associated student
        String studentEnrollmentNumber = registration.getStudent().getEnrollmentNumber();

        // Check if an attendance record exists for this student enrollment number
        if (!attendanceRepository.existsByStudentEnrollmentNumber(studentEnrollmentNumber)) {
            return 0.0;
        }

        // Fetch the attendance record for the student
        AttendanceRecord attendanceRecord = attendanceRepository.findByStudentEnrollmentNumber(studentEnrollmentNumber);

        // Compute the attendance percentage based on the details in the attendance record
        double daysAttended = attendanceRecord.getPresentDays(); // Assuming this method exists
        double totalDays = attendanceRecord.getTotalDays(); // Assuming this method exists

        double attendancePercentage = (daysAttended / totalDays) * 100;

        return attendancePercentage;
    }

    private InputStreamResource loadProofAsStreamResource(String proofUrl) {
        RestTemplate restTemplate = new RestTemplate();

        ResponseEntity<InputStreamResource> response = restTemplate.exchange(
                proofUrl, HttpMethod.GET, null, InputStreamResource.class);

        if (response.getStatusCode() != HttpStatus.OK) {
            throw new RuntimeException("Failed to fetch proof: " + proofUrl);
        }

        return response.getBody();
    }
}
