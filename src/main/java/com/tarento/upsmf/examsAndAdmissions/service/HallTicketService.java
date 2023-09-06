package com.tarento.upsmf.examsAndAdmissions.service;

import com.tarento.upsmf.examsAndAdmissions.model.Student;
import com.tarento.upsmf.examsAndAdmissions.model.StudentExamRegistration;
import com.tarento.upsmf.examsAndAdmissions.model.dto.DataCorrectionRequest;
import com.tarento.upsmf.examsAndAdmissions.repository.*;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Optional;

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

    public ResponseEntity<byte[]> getHallTicket(String examRegistrationNumber, String dateOfBirth) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

        Date dob;
        LocalDate localDate;
        try {
            dob = formatter.parse(dateOfBirth);
            localDate = dob.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        } catch (ParseException e) {
            throw new RuntimeException("Invalid date format", e);
        }

        Optional<StudentExamRegistration> studentOptional = studentExamRegistrationRepository.findByIdAndStudent_DateOfBirth(examRegistrationNumber, localDate);

        if (studentOptional.isPresent()) {
            Student student = studentOptional.get().getStudent();

            if (hasPaidFees(student.getId()) && student.getExamCenter()!=null) {
                byte[] hallTicketData = generateHallTicket(student);  // This is where you generate the hall ticket data dynamically
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

    private byte[] generateHallTicket(Student student) {
        PDDocument document = new PDDocument();
        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);

        try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 16);
            contentStream.beginText();
            contentStream.newLineAtOffset(50, 750);
            contentStream.showText("Hall Ticket");
            contentStream.endText();

            contentStream.setFont(PDType1Font.HELVETICA, 12);
            contentStream.beginText();
            contentStream.newLineAtOffset(50, 720);
            contentStream.showText("Name: " + student.getFirstName()+student.getSurname());
            contentStream.newLineAtOffset(0, -15);
            contentStream.showText("Exam Enrollment Number: " + student.getEnrollmentNumber());
            contentStream.newLineAtOffset(0, -15);
            contentStream.showText("Date of Birth: " + student.getDateOfBirth());
            contentStream.endText();
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

    private boolean hasPaidFees(Long registrationId) {
        Optional<StudentExamRegistration> registrationOptional = studentExamRegistrationRepository.findById(registrationId);

        if (registrationOptional.isPresent()) {
            return registrationOptional.get().isFeesPaid();
        } else {
            throw new RuntimeException("Registration not found with ID: " + registrationId);
        }
    }
    private boolean hasCCTVVerification(StudentExamRegistration registration) {
        return registration.getAssignedExamCenter().isCctvVerified();
    }
    public void requestHallTicketDataCorrection(Long studentId, String correctionDetails) {
        DataCorrectionRequest request = new DataCorrectionRequest();
        request.setStudentId(studentId);
        request.setRequestedCorrection(correctionDetails);
        // Assuming there is a field in DataCorrectionRequest to mark if it's new or processed
        request.setStatus("NEW");
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
}
