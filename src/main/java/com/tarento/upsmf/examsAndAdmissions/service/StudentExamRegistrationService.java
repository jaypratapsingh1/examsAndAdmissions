package com.tarento.upsmf.examsAndAdmissions.service;

import com.tarento.upsmf.examsAndAdmissions.exception.ExamCycleNotFoundException;
import com.tarento.upsmf.examsAndAdmissions.model.*;
import com.tarento.upsmf.examsAndAdmissions.model.dto.StudentExamRegistrationDTO;
import com.tarento.upsmf.examsAndAdmissions.repository.*;
import com.tarento.upsmf.examsAndAdmissions.util.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
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
    public ResponseDto registerStudentsForExams(List<StudentExamRegistrationDTO> requests, String userId)
    {
        ResponseDto response = new ResponseDto(Constants.API_REGISTER_STUDENTS_FOR_EXAMS);

        // Validate input
        if (requests == null || requests.isEmpty()) {
            log.error("Registration request list is null or empty.");
            ResponseDto.setErrorResponse(response, "INVALID_INPUT", "Registration request list is empty.", HttpStatus.BAD_REQUEST);
            return response;
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

        // Fetch all registrations for the given student and exam cycle IDs
        List<StudentExamRegistration> allRegistrationsForStudents = registrationRepository.findByStudentIdInAndExamCycleIdIn(studentIds, new ArrayList<>(examCycleIds));

        // Convert this list into a map for efficient lookups
        Map<Long, Set<Long>> studentToRegisteredExams = new HashMap<>();
        for (StudentExamRegistration registration : allRegistrationsForStudents) {
            studentToRegisteredExams
                    .computeIfAbsent(registration.getStudent().getId(), k -> new HashSet<>())
                    .add(registration.getExam().getId());
        }

        // Filter out students who are registered for all exams and adjust available exams
        students = students.stream()
                .filter(student -> {
                    Set<Long> registeredExamIds = studentToRegisteredExams.getOrDefault(student.getId(), Collections.emptySet());
                    return registeredExamIds.size() != exams.size();
                })
                .collect(Collectors.toList());

        examIds.removeAll(allRegistrationsForStudents.stream().map(reg -> reg.getExam().getId()).collect(Collectors.toSet()));

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
            response.put(Constants.MESSAGE, "Students registered successfully.");
            response.setResponseCode(HttpStatus.OK);
        } else if (!newRegistrations.isEmpty()) {
            response.put(Constants.MESSAGE, "Students registered with some warnings: " + String.join(" ", alreadyRegisteredMessages));
            response.setResponseCode(HttpStatus.OK);
        } else {
            ResponseDto.setErrorResponse(response, "NO_NEW_REGISTRATIONS", "No new registrations were processed. " + String.join(" ", alreadyRegisteredMessages), HttpStatus.BAD_REQUEST);
        }
    } catch (Exception e) {
        log.error("Error during student registration.", e);
        ResponseDto.setErrorResponse(response, "REGISTRATION_ERROR", "An error occurred while processing registrations.", HttpStatus.INTERNAL_SERVER_ERROR);
    }

        return response;
    }

    @GetMapping
    public ResponseDto getAllRegistrations(Pageable pageable) {
        ResponseDto response = new ResponseDto(Constants.API_GET_ALL_REGISTRATIONS);
        try {
            // Fetch registrations with pagination
            Page<StudentExamRegistration> registrations = registrationRepository.findAll(pageable);

            // Convert to DTOs
            Page<StudentExamRegistrationDTO> registrationDTOs = registrations.map(this::convertToDto);

            if (!registrationDTOs.isEmpty()) {
                response.put(Constants.MESSAGE, "Registrations fetched successfully.");
                response.put(Constants.RESPONSE, registrationDTOs);
                response.setResponseCode(HttpStatus.OK);
            } else {
                ResponseDto.setErrorResponse(response, "NO_REGISTRATIONS_FOUND", "No registrations found.", HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            log.error("Error fetching registrations.", e);
            ResponseDto.setErrorResponse(response, "FETCH_ERROR", "An error occurred while fetching registrations.", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return response;
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
    public ResponseDto getAllRegistrationsByExamCycle(Long examCycleId) {
        ResponseDto response = new ResponseDto(Constants.API_GET_ALL_REGISTRATIONS_BY_EXAM_CYCLE);
        try {
            List<StudentExamRegistration> registrations = registrationRepository.findByExamCycleId(examCycleId);

            if (registrations.isEmpty()) {
                ResponseDto.setErrorResponse(response, "REGISTRATIONS_NOT_FOUND", "No registrations found for exam cycle ID: " + examCycleId, HttpStatus.NOT_FOUND);
            } else {
                List<StudentExamRegistrationDTO> dtoList = registrations.stream()
                        .map(this::convertToDto)
                        .collect(Collectors.toList());
                response.put(Constants.MESSAGE, "Registrations fetched successfully.");
                response.put(Constants.RESPONSE, dtoList);
                response.setResponseCode(HttpStatus.OK);
            }
        } catch (Exception e) {
            ResponseDto.setErrorResponse(response, "GENERAL_ERROR", "An unexpected error occurred: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return response;
    }
}