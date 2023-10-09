package com.tarento.upsmf.examsAndAdmissions.service;

import com.tarento.upsmf.examsAndAdmissions.exception.ExamCycleNotFoundException;
import com.tarento.upsmf.examsAndAdmissions.model.*;
import com.tarento.upsmf.examsAndAdmissions.model.dto.StudentExamRegistrationDTO;
import com.tarento.upsmf.examsAndAdmissions.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
public class StudentExamRegistrationService {

    @Autowired
    private StudentExamRegistrationRepository registrationRepository;

    @Autowired
    private ExamRepository examRepository;
    @Autowired
    private ExamCycleRepository examCycleRepository;
    @Autowired
    private StudentRepository studentRepository;
    @Transactional
    public ResponseEntity<?> registerStudentsForExams(List<StudentExamRegistrationDTO> requests, String userId)
    {
    // Validate input
    if (requests == null || requests.isEmpty()) {
        log.error("Registration request list is null or empty.");
        return ResponseEntity.badRequest().body("Registration request list is empty.");
    }

    try {
        // Extract and validate student, exam, and exam cycle IDs
        Set<Long> studentIds = requests.stream().map(StudentExamRegistrationDTO::getStudentId).collect(Collectors.toSet());
        Set<Long> examIds = requests.stream().flatMap(r -> r.getExamIds().stream()).collect(Collectors.toSet());
        Set<Long> examCycleIds = requests.stream().map(StudentExamRegistrationDTO::getExamCycleId).collect(Collectors.toSet());

        // Fetch entities
        List<Student> students = studentRepository.findAllById(studentIds);
        List<Exam> exams = examRepository.findAllById(examIds);
        List<ExamCycle> examCycles = examCycleRepository.findAllById(examCycleIds);

        // Convert to maps for easy lookup
        Map<Long, Student> studentMap = students.stream().collect(Collectors.toMap(Student::getId, Function.identity()));
        // Create a map of studentId to their associated Institute
        Map<Long, Institute> studentInstituteMap = students.stream().collect(Collectors.toMap(Student::getId, Student::getInstitute));
        Map<Long, Exam> examMap = exams.stream().collect(Collectors.toMap(Exam::getId, Function.identity()));
        Map<Long, ExamCycle> examCycleMap = examCycles.stream().collect(Collectors.toMap(ExamCycle::getId, Function.identity()));

        // Fetch existing registrations in bulk
        List<StudentExamRegistration> existingRegistrations = registrationRepository.findByStudentIdInAndExamIdIn(studentIds, examIds);
        Set<String> existingRegistrationKeys = existingRegistrations.stream()
                .map(r -> r.getStudent().getId() + "-" + r.getExam().getId())
                .collect(Collectors.toSet());

        // Generate keys for all the new requests
        Set<String> newRegistrationKeys = new HashSet<>();
        for (StudentExamRegistrationDTO request : requests) {
            Long studentId = request.getStudentId();
            for (Long examId : request.getExamIds()) {
                newRegistrationKeys.add(studentId + "-" + examId);
            }
        }
        // Accumulate existing registrations for feedback
        List<String> alreadyRegisteredMessages = new ArrayList<>();
        newRegistrationKeys.forEach(key -> {
            if (existingRegistrationKeys.contains(key)) {
                String[] parts = key.split("-");
                Long studentId = Long.parseLong(parts[0]);
                Long examId = Long.parseLong(parts[1]);
                alreadyRegisteredMessages.add("Student with ID " + studentId + " is already registered for exam with ID " + examId + ".");
            }
        });

        // Subtract existing keys from new request keys to find the truly new registrations
        newRegistrationKeys.removeAll(existingRegistrationKeys);

        List<StudentExamRegistration> newRegistrations = new ArrayList<>();
        newRegistrationKeys.forEach(key -> {
            String[] parts = key.split("-");
            Long studentId = Long.parseLong(parts[0]);
            Long examId = Long.parseLong(parts[1]);

            if (!studentMap.containsKey(studentId) || !examMap.containsKey(examId)) {
                log.error("Missing entities for studentId {} or examId {}", studentId, examId);
                throw new RuntimeException("Invalid entities references encountered.");
            }

            StudentExamRegistrationDTO request = requests.stream()
                    .filter(r -> r.getStudentId().equals(studentId))
                    .findFirst()
                    .orElseThrow(() -> {
                        log.error("Missing request for student ID: {}", studentId);
                        return new RuntimeException("Missing request for student ID: " + studentId);
                    });

            StudentExamRegistration registration = new StudentExamRegistration();
            registration.setStudent(studentMap.get(studentId));
            registration.setInstitute(studentInstituteMap.get(studentId));
            registration.setExam(examMap.get(examId));
            registration.setExamCycle(examCycleMap.get(request.getExamCycleId()));
            registration.setRegistrationDate(request.getRegistrationDate());
            registration.setStatus(request.getStatus());
            registration.setRemarks(request.getRemarks());
            registration.setUpdatedBy(userId);

            newRegistrations.add(registration);
        });

        // Bulk save new registrations
        if (!newRegistrations.isEmpty()) {
            registrationRepository.saveAll(newRegistrations);
        }

        if (!newRegistrations.isEmpty() && alreadyRegisteredMessages.isEmpty()) {
            return ResponseEntity.ok("Students registered successfully.");
        } else if (!newRegistrations.isEmpty()) {
            return ResponseEntity.ok("Students registered with some warnings: " + String.join(" ", alreadyRegisteredMessages));
        } else {
            return ResponseEntity.status(400).body("No new registrations were processed. " + String.join(" ", alreadyRegisteredMessages));
        }
    } catch (Exception e) {
        log.error("Error during student registration.", e);
        return ResponseEntity.status(500).body("An error occurred while processing registrations.");
    }
}

    @GetMapping
    public ResponseEntity<Page<StudentExamRegistrationDTO>> getAllRegistrations(Pageable pageable) {
        try {
            // Fetch registrations with pagination
            Page<StudentExamRegistration> registrations = registrationRepository.findAll(pageable);

            // Convert to DTOs
            Page<StudentExamRegistrationDTO> registrationDTOs = registrations.map(this::convertToDto);

            return ResponseEntity.ok(registrationDTOs);
        } catch (Exception e) {
            log.error("Error fetching registrations.", e);
            return ResponseEntity.status(500).body(null);
        }
    }

    private StudentExamRegistrationDTO convertToDto(StudentExamRegistration entity) {
        StudentExamRegistrationDTO dto = new StudentExamRegistrationDTO();

        dto.setId(entity.getId());
        dto.setStudentId(entity.getStudent().getId());
        dto.setExamIds(new HashSet<>(Collections.singletonList(entity.getExam().getId())));
        dto.setExamCycleId(entity.getExamCycle().getId());
        dto.setRegistrationDate(entity.getRegistrationDate());
        dto.setStatus(entity.getStatus());
        dto.setRemarks(entity.getRemarks());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setUpdatedBy(entity.getUpdatedBy());

        return dto;
    }
    public List<StudentExamRegistrationDTO> getAllRegistrationsByExamCycle(Long examCycleId) {
        List<StudentExamRegistration> registrations = registrationRepository.findByExamCycleId(examCycleId);

        if (registrations.isEmpty()) {
            log.error("Registrations not found for  exam cycle ID: {}", examCycleId);
            throw new ExamCycleNotFoundException(examCycleId);
        }

        return registrations.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
}