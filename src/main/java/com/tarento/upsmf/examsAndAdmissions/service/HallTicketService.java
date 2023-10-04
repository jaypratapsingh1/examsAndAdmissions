package com.tarento.upsmf.examsAndAdmissions.service;

import com.tarento.upsmf.examsAndAdmissions.enums.ApprovalStatus;
import com.tarento.upsmf.examsAndAdmissions.model.*;
import com.tarento.upsmf.examsAndAdmissions.model.dto.DataCorrectionRequest;
import com.tarento.upsmf.examsAndAdmissions.model.dto.PendingDataDto;
import com.tarento.upsmf.examsAndAdmissions.repository.*;
import com.tarento.upsmf.examsAndAdmissions.util.Constants;
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
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
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
    private EntityManager entityManager;
    @Autowired
    private StudentService studentService;

    public ResponseEntity<byte[]> getHallTicket(Long id, String dateOfBirth) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

        Date dob;
        LocalDate localDate;
        try {
            dob = formatter.parse(dateOfBirth);
            localDate = dob.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        } catch (ParseException e) {
            throw new RuntimeException("Invalid date format", e);
        }

        Optional<StudentExamRegistration> studentOptional = studentExamRegistrationRepository.findByIdAndStudent_DateOfBirth(id, localDate);

        if (studentOptional.isPresent()) {
            StudentExamRegistration registration = studentOptional.get();  // Extracting the StudentExamRegistration object from the Optional
            Student student = studentOptional.get().getStudent();

            if (studentOptional.get().isFeesPaid() && studentOptional.get().getExamCenter()!=null) {
                byte[] hallTicketData = generateHallTicket(registration);  // This is where you generate the hall ticket data dynamically
                return ResponseEntity.ok(hallTicketData);
            } else {
                // User doesn't meet the criteria for hall ticket issuance
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .header("Content-Type", "text/plain; charset=utf-8")
                        .body("You don't meet the criteria for hall ticket issuance.".getBytes());
            }
        } else {
            // No student record found for the provided details
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .header("Content-Type", "text/plain; charset=utf-8")
                    .body("No student record found for the provided details.".getBytes());
        }
    }
    private byte[] generateHallTicket(StudentExamRegistration studentOptional) { // Added an Exam parameter to provide exam details
        PDDocument document = new PDDocument();
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
            contentStream.showText("Name: " + studentOptional.getStudent().getFirstName() + " " + studentOptional.getStudent().getSurname());
            contentStream.newLineAtOffset(0, -20);
            contentStream.showText("Exam Enrollment Number: " + studentOptional.getStudent().getEnrollmentNumber());
            contentStream.newLineAtOffset(0, -20);
            contentStream.showText("Date of Birth: " + studentOptional.getStudent().getDateOfBirth());
            contentStream.endText();

            // Exam Details (Assuming an Exam object has these methods)
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 14);
            contentStream.beginText();
            contentStream.newLineAtOffset(50, 620);
            contentStream.showText("Exam: " + studentOptional.getExam().getExamName());
            contentStream.newLineAtOffset(0, -20);
            contentStream.showText("Date: " + studentOptional.getExam().getExamDate());
            contentStream.newLineAtOffset(0, -20);
            contentStream.showText("Time: " + studentOptional.getExam().getStartTime() + " - " + studentOptional.getExam().getEndTime());
            contentStream.newLineAtOffset(0, -20);
            contentStream.showText("Venue: " + studentOptional.getExamCenter().getAddress());
            contentStream.endText();

            // You can continue adding more details as necessary

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

    private boolean hasCCTVVerification(StudentExamRegistration registration) {
        return registration.getExamCenter().getVerified();
    }
    public void requestHallTicketDataCorrection(Long studentId, String correctionDetails, @RequestParam("file") MultipartFile proof) throws IOException {
        DataCorrectionRequest request = new DataCorrectionRequest();
        request.setStudentId(studentId);
        request.setRequestedCorrection(correctionDetails);
        request.setStatus("NEW");
        if (proof != null && !proof.isEmpty()) {
            String path = studentService.storeFile(proof);
            request.setProofAttachmentPath(path);
        }
        dataCorrectionRequestRepository.save(request);
    }

    public List<DataCorrectionRequest> getAllDataCorrectionRequests() {
        return dataCorrectionRequestRepository.findAll();
    }

    public void approveDataCorrection(Long requestId) {
        DataCorrectionRequest request = dataCorrectionRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));
        request.setStatus("APPROVED");
        dataCorrectionRequestRepository.save(request);
    }

    public void rejectDataCorrection(Long requestId, String rejectionReason) {
        DataCorrectionRequest request = dataCorrectionRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));
        request.setStatus("REJECTED");
        request.setRejectionReason(rejectionReason);  // Assuming there's a field to store rejection reason
        dataCorrectionRequestRepository.save(request);
    }

    public ResponseDto getPendingDataForHallTickets(Long courseId, Long examCycleId, Long instituteId) {
        ResponseDto response = new ResponseDto(Constants.API_HALLTICKET_GET_PENDING_DATA);

        List<StudentExamRegistration> registrations = fetchPendingDataForHallTickets(courseId, examCycleId, instituteId);
        List<PendingDataDto> pendingDataDtos = registrations.stream().map(this::toPendingDataDto).collect(Collectors.toList());

        if (!pendingDataDtos.isEmpty()) {
            response.put(Constants.MESSAGE, Constants.SUCCESSFUL);
            response.put(Constants.RESPONSE, pendingDataDtos);
            response.setResponseCode(HttpStatus.OK);
        } else {
            response.put(Constants.MESSAGE, "No pending hall tickets found for the provided filters.");
            response.put(Constants.RESPONSE, Constants.FAILUREMESSAGE);
            response.setResponseCode(HttpStatus.NOT_FOUND);
        }
        return response;
    }

    private PendingDataDto toPendingDataDto(StudentExamRegistration registration) {
        PendingDataDto dto = new PendingDataDto();
        dto.setFirstName(registration.getStudent().getFirstName());
        dto.setLastName(registration.getStudent().getSurname());
        dto.setCourseName(registration.getExam().getCourse().getCourseName());
        dto.setStudentEnrollmentNumber(registration.getStudent().getEnrollmentNumber());
        dto.setRegistrationDate(registration.getRegistrationDate());
        dto.setStatus(registration.getStatus());
        dto.setRemarks(registration.getRemarks());
        dto.setFeesPaid(registration.isFeesPaid());
        return dto;
    }

    private List<StudentExamRegistration> fetchPendingDataForHallTickets(Long courseId, Long examCycleId, Long instituteId) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<StudentExamRegistration> criteriaQuery = criteriaBuilder.createQuery(StudentExamRegistration.class);
        Root<StudentExamRegistration> registrationRoot = criteriaQuery.from(StudentExamRegistration.class);

        List<Predicate> predicates = new ArrayList<>();

        if (courseId != null) {
            predicates.add(criteriaBuilder.equal(registrationRoot.get("exam").get("course").get("id"), courseId));
        }
        if (examCycleId != null) {
            predicates.add(criteriaBuilder.equal(registrationRoot.get("examCycle").get("id"), examCycleId));
        }
        if (instituteId != null) {
            predicates.add(criteriaBuilder.equal(registrationRoot.get("institute").get("id"), instituteId));
        }

        criteriaQuery.where(predicates.toArray(new Predicate[0]));

        return entityManager.createQuery(criteriaQuery).getResultList();
    }
    public ResponseEntity<Resource> downloadProofForDataCorrectionRequest(Long requestId) {
        DataCorrectionRequest request = dataCorrectionRequestRepository.findById(requestId).orElse(null);

        if (request == null) {
            return ResponseEntity.notFound().build();
        }

        String proofPath = request.getProofAttachmentPath();
        Resource file;
        file = loadProofAsStreamResource(proofPath);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFilename() + "\"")
                .body(file);
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
    public String getProofUrlByRequestId(Long requestId) {
        DataCorrectionRequest request = dataCorrectionRequestRepository.findById(requestId).orElse(null);
        if (request != null) {
            return request.getProofAttachmentPath();
        }
        // handle case where request is null, maybe throw an exception or return a default value
        return null;
    }
}
