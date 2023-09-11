package com.tarento.upsmf.examsAndAdmissions.service;

import com.tarento.upsmf.examsAndAdmissions.model.Exam;
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
import java.util.Locale;
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
