package com.tarento.upsmf.examsAndAdmissions.service;

import com.tarento.upsmf.examsAndAdmissions.enums.ApprovalStatus;
import com.tarento.upsmf.examsAndAdmissions.enums.ExamCycleStatus;
import com.tarento.upsmf.examsAndAdmissions.exception.InvalidRequestException;
import com.tarento.upsmf.examsAndAdmissions.model.*;
import com.tarento.upsmf.examsAndAdmissions.model.dto.ExamCycleDTO;
import com.tarento.upsmf.examsAndAdmissions.model.dto.SearchExamCycleDTO;
import com.tarento.upsmf.examsAndAdmissions.repository.*;
import com.tarento.upsmf.examsAndAdmissions.util.Constants;
import lombok.extern.slf4j.Slf4j;
import org.codehaus.jettison.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.ConstraintViolationException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ExamCycleService {

    @Autowired
    private ExamCycleRepository repository;
    @Autowired
    ExamEntityRepository examEntityRepository;
    @Autowired
    private ExamRepository examRepository;
    @Autowired
    private InstituteRepository instituteRepository;
    @Autowired
    private ExamCenterRepository examCenterRepository;
    @Autowired
    private CourseRepository courseRepository;
    @Autowired
    private DataImporterService dataImporterService;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    Map<String, Class<?>> columnConfig = Map.of(
            "Start Date", Date.class,
            "End Date", Date.class,
            "Start Time", Date.class,
            "End Time", Date.class
            // Add other columns and their data types as needed
    );

    public ResponseDto createExamCycle(ExamCycle examCycle, String userId) {
        ResponseDto response = new ResponseDto(Constants.API_EXAM_CYCLE_ADD);

        try {
            ExamCycle existingExamCycle = repository.findByExamCycleNameAndCourseAndStartDateAndEndDate(
                    examCycle.getExamCycleName(),
                    examCycle.getCourse(),
                    examCycle.getStartDate(),
                    examCycle.getEndDate()
            );

            if (existingExamCycle != null) {
                ResponseDto.setErrorResponse(response, "EXAM_CYCLE_ALREADY_EXISTS", "ExamCycle with the same details already exists.", HttpStatus.CONFLICT);
                return response;
            }

            log.info("Creating new ExamCycle: {}", examCycle);
            Course course = courseRepository.findById(examCycle.getCourse().getId())
                    .orElseThrow(() -> new RuntimeException("Course not found"));

            examCycle.setObsolete(0);
            examCycle.setCreatedOn(LocalDateTime.now());
            examCycle.setStatus(ExamCycleStatus.DRAFT);
            examCycle.setCreatedBy(userId);
            examCycle.setCourse(course);
            examCycle = repository.save(examCycle);

            List<Institute> allInstitutes = instituteRepository.findAll();

            // Register each institute as a potential exam center for this exam cycle
            for (Institute institute : allInstitutes) {
                ExamCenter examCenter = new ExamCenter();
                examCenter.setInstitute(institute);
                examCenter.setExamCycle(examCycle);
                examCenter.setApprovalStatus(ApprovalStatus.PENDING); // marking as pending
                examCenter.setDistrict(institute.getDistrict());
                examCenter.setAddress(institute.getAddress());
                examCenter.setName(institute.getInstituteName());
                examCenter.setInstituteCode(institute.getInstituteCode());
                examCenter.setAllowedForExamCentre(institute.isAllowedForExamCentre());
                examCenterRepository.save(examCenter);
            }

            response.put(Constants.MESSAGE, Constants.SUCCESSMESSAGE);
            response.put(Constants.RESPONSE, examCycle);
            response.setResponseCode(HttpStatus.CREATED);
        } catch (Exception e) {
            log.error("Error while creating ExamCycle", e);
            ResponseDto.setErrorResponse(response, "EXAM_CYCLE_CREATION_FAILED", "An error occurred while creating the ExamCycle.", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return response;
    }

    public ResponseDto getAllExamCycles() {
        ResponseDto response = new ResponseDto(Constants.API_EXAM_CYCLE_GET_ALL);
        List<ExamCycle> examCycles = repository.findByObsolete(0);

        if (!examCycles.isEmpty()) {
            response.put(Constants.MESSAGE, Constants.SUCCESSFUL);
            response.put(Constants.RESPONSE, examCycles);
            response.setResponseCode(HttpStatus.OK);
        } else {
            ResponseDto.setErrorResponse(response, "NO_ACTIVE_EXAM_CYCLES", "No active ExamCycles found.", HttpStatus.NOT_FOUND);
        }

        return response;
    }

    public ResponseDto getExamCycleById(Long id) {
        ResponseDto response = new ResponseDto(Constants.API_EXAM_CYCLE_GET_BY_ID);
        Optional<ExamCycle> examCycleOptional = repository.findByIdAndObsolete(id, 0);

        if (examCycleOptional.isPresent()) {
            ExamCycle examCycle = examCycleOptional.get();
            response.put(Constants.MESSAGE, Constants.SUCCESSFUL);
            response.put(Constants.RESPONSE, examCycle);
            response.setResponseCode(HttpStatus.OK);
        } else {
            ResponseDto.setErrorResponse(response, "EXAM_CYCLE_NOT_FOUND", "ExamCycle not found.", HttpStatus.NOT_FOUND);
        }

        return response;
    }

    public ResponseDto updateExamCycle(Long id, ExamCycle updatedExamCycle, String userId) {
        ResponseDto response = new ResponseDto(Constants.API_EXAM_CYCLE_UPDATE);

        // 1. Check if the exam cycle exists.
        ExamCycle existingExamCycle = repository.findByIdAndObsolete(id, 0).orElse(null);
        if (existingExamCycle == null) {
            ResponseDto.setErrorResponse(response, "EXAM_CYCLE_NOT_FOUND", "ExamCycle not found.", HttpStatus.NOT_FOUND);
            return response;
        }

        // 2. Check if the course exists.
        Course course = courseRepository.findById(updatedExamCycle.getCourse().getId()).orElse(null);
        if (course == null) {
            ResponseDto.setErrorResponse(response, "COURSE_NOT_FOUND", "Course with code " + updatedExamCycle.getCourse().getId() + " not found.", HttpStatus.BAD_REQUEST);
            return response;
        }

        try {
            existingExamCycle.setExamCycleName(updatedExamCycle.getExamCycleName());
            existingExamCycle.setCourse(updatedExamCycle.getCourse());
            existingExamCycle.setStartDate(updatedExamCycle.getStartDate());
            existingExamCycle.setEndDate(updatedExamCycle.getEndDate());
            existingExamCycle.setModifiedBy(userId);
            existingExamCycle.setModifiedOn(LocalDateTime.now());
            repository.save(existingExamCycle);

            response.put(Constants.MESSAGE, Constants.SUCCESSMESSAGE);
            response.put(Constants.RESPONSE, existingExamCycle);
            response.setResponseCode(HttpStatus.OK);
            return response;

        } catch (DataIntegrityViolationException e) {
            if (e.getCause() instanceof ConstraintViolationException) {
                ResponseDto.setErrorResponse(response, "INVALID_REFERENCE", "Invalid reference in data. Please check foreign key references.", HttpStatus.BAD_REQUEST);
                return response;
            }
            throw e;  // re-throw the exception if it's not the one we're handling
        }
    }

    public ResponseDto deleteExamCycle(Long id) {
        ResponseDto response = new ResponseDto(Constants.API_EXAM_CYCLE_DELETE);
        ExamCycle examCycle = repository.findById(id).orElse(null);
        if (examCycle != null) {
            examCycle.setObsolete(1);
            repository.save(examCycle);
            response.put(Constants.MESSAGE, "ExamCycle soft-deleted successfully.");
            response.put(Constants.RESPONSE, Constants.SUCCESSMESSAGE);
            response.setResponseCode(HttpStatus.OK);
        } else {
            ResponseDto.setErrorResponse(response, "EXAM_CYCLE_NOT_FOUND", "ExamCycle not found for deletion.", HttpStatus.NOT_FOUND);
        }
        return response;
    }

    public ResponseDto restoreExamCycle(Long id) {
        ResponseDto response = new ResponseDto(Constants.API_EXAM_CYCLE_RESTORE);
        ExamCycle examCycle = repository.findById(id).orElse(null);
        if (examCycle != null && examCycle.getObsolete() == 1) {
            examCycle.setObsolete(0);
            repository.save(examCycle);
            response.put(Constants.MESSAGE, "ExamCycle restored successfully.");
            response.put(Constants.RESPONSE, Constants.SUCCESSMESSAGE);
            response.setResponseCode(HttpStatus.OK);
        } else {
            ResponseDto.setErrorResponse(response, "EXAM_CYCLE_NOT_FOUND_OR_NOT_MARKED", "ExamCycle not found or not marked for restoration.", HttpStatus.NOT_FOUND);
        }
        return response;
    }

    public ResponseDto addExamsToCycle(Long id, List<Exam> exams, String userId) {
        ResponseDto response = new ResponseDto(Constants.API_EXAM_CYCLE_ADD_EXAMS);
        ExamCycle examCycle = repository.findById(id).orElse(null);
        if (examCycle == null) {
            ResponseDto.setErrorResponse(response, "EXAM_CYCLE_NOT_FOUND", "ExamCycle not found.", HttpStatus.NOT_FOUND);
            return response;
        }

        for (Exam exam : exams) {
            Optional<Exam> existingExam = examRepository.findByExamNameAndExamDateAndStartTimeAndEndTime(
                    exam.getExamName(), exam.getExamDate(), exam.getStartTime(), exam.getEndTime());
            if (existingExam.isPresent()) {
                ResponseDto.setErrorResponse(response, "EXAM_ALREADY_EXISTS", "Exam already exists with same details: " + exam.getExamName(), HttpStatus.CONFLICT);
                return response;
            }

            Course course = courseRepository.findById(exam.getCourse().getId()).orElse(null);
            if (course == null) {
                ResponseDto.setErrorResponse(response, "COURSE_NOT_FOUND", "Course not found with ID: " + exam.getCourse().getId(), HttpStatus.NOT_FOUND);
                return response;
            }

            exam.setCourse(course);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
            exam.setStartTime(LocalTime.parse(exam.getStartTime().format(formatter)));
            exam.setEndTime(LocalTime.parse(exam.getEndTime().format(formatter)));
            exam.setCreatedBy(userId);
            exam.setCreatedOn(LocalDateTime.now());
            exam.setExamCycleId(examCycle.getId());
            examRepository.save(exam);
        }

        response.put(Constants.MESSAGE, Constants.SUCCESSMESSAGE);
        response.put(Constants.RESPONSE, examCycle);
        response.setResponseCode(HttpStatus.OK);
        return response;
    }

    public ResponseDto removeExamFromCycle(Long id, Exam exam) {
        ResponseDto response = new ResponseDto(Constants.API_EXAM_CYCLE_REMOVE_EXAM);
        ExamCycle examCycle = repository.findById(id).orElse(null);

        if (examCycle == null) {
            ResponseDto.setErrorResponse(response, "EXAM_CYCLE_NOT_FOUND", "ExamCycle not found.", HttpStatus.NOT_FOUND);
            return response;
        }

        if (exam.getExamCycleId() == null || !exam.getExamCycleId().equals(examCycle.getId())) {
            ResponseDto.setErrorResponse(response, "EXAM_NOT_ASSOCIATED", "The provided exam is not associated with the given ExamCycle.", HttpStatus.BAD_REQUEST);
            return response;
        }

        exam.setExamCycleId(null); // Remove the association of exam with the exam cycle
        examRepository.save(exam);

        response.put(Constants.MESSAGE, Constants.SUCCESSMESSAGE);
        response.put(Constants.RESPONSE, examCycle);
        response.setResponseCode(HttpStatus.OK);
        return response;
    }

    public ResponseDto publishExamCycle(Long id) {
        ResponseDto response = new ResponseDto(Constants.API_EXAM_CYCLE_PUBLISH);
        Optional<ExamCycle> optionalExamCycle = repository.findById(id);
        if (!optionalExamCycle.isPresent()) {
            ResponseDto.setErrorResponse(response, "EXAM_CYCLE_NOT_FOUND", "ExamCycle not found.", HttpStatus.NOT_FOUND);
            return response;
        }
        ExamCycle examCycle = optionalExamCycle.get();
        examCycle.setStatus(ExamCycleStatus.PUBLISHED);
        repository.save(examCycle);
        response.put(Constants.MESSAGE, Constants.SUCCESSMESSAGE);
        response.put(Constants.RESPONSE, examCycle);
        response.setResponseCode(HttpStatus.OK);
        return response;
    }

    public ExamCycleDTO toDTO(ExamCycle examCycle) {
        ExamCycleDTO dto = new ExamCycleDTO();
        dto.setId(examCycle.getId());
        dto.setExamCycleName(examCycle.getExamCycleName());

        // Set Course related fields
        if (examCycle.getCourse() != null) {
            dto.setCourseId(examCycle.getCourse().getId());
            dto.setCourseCode(examCycle.getCourse().getCourseCode());
            dto.setCourseName(examCycle.getCourse().getCourseName());
        }

        dto.setStartDate(examCycle.getStartDate());
        dto.setEndDate(examCycle.getEndDate());
        dto.setCreatedBy(examCycle.getCreatedBy());
        dto.setCreatedOn(examCycle.getCreatedOn());
        dto.setModifiedBy(examCycle.getModifiedBy());
        dto.setModifiedOn(examCycle.getModifiedOn());
        dto.setStatus(examCycle.getStatus());
        dto.setObsolete(examCycle.getObsolete());

        return dto;
    }
    public ResponseDto updateExamsForCycle(Long cycleId, List<Exam> examsFromUser) {
        Optional<ExamCycle> existingCycle = repository.findById(cycleId);
        ResponseDto response = new ResponseDto();

        if (existingCycle.isPresent()) {
            // Fetch all existing exams for the given exam cycle
            List<Exam> existingExams = examRepository.findByExamCycleId(cycleId);

            // Create a map of existing exams by their ID
            Map<Long, Exam> existingExamsMap = existingExams.stream()
                    .collect(Collectors.toMap(Exam::getId, Function.identity()));

            // Process the list from the user
            for (Exam exam : examsFromUser) {
                if (exam.getId() != null && existingExamsMap.containsKey(exam.getId())) {
                    // This is an existing exam. Update its details.
                    Exam existingExam = existingExamsMap.get(exam.getId());
                    existingExam.setExamDate(exam.getExamDate());
                    existingExam.setStartTime(exam.getStartTime());
                    existingExam.setEndTime(exam.getEndTime());
                    // ... update other fields as necessary
                    existingExamsMap.remove(exam.getId()); // Remove this ID from the map
                } else {
                    // This is a new exam. Create it.
                    exam.setExamCycleId(cycleId);
                    examRepository.save(exam);
                }
            }

            // Any remaining exams in the map are not in the user's list and should be deleted
            for (Exam remainingExam : existingExamsMap.values()) {
                examRepository.delete(remainingExam);
            }

            // Construct a success response
            response.setResponseCode(HttpStatus.OK);
            response.setResult(Collections.singletonMap("message", "Exams updated successfully"));
        } else {
            // Construct an error response since the exam cycle was not found
            ResponseDto.setErrorResponse(response, "EXAM_CYCLE_NOT_FOUND", "The specified exam cycle was not found.", HttpStatus.NOT_FOUND);
        }
        return response;
    }

    public ResponseDto searchExamCycle(SearchExamCycleDTO searchExamCycleDTO) {
        //validate request
        validateSearchExamCyclePayload(searchExamCycleDTO);
        ResponseDto response = new ResponseDto(Constants.API_EXAM_CYCLE_SEARCH);
        // search for exam cycle
        List<ExamCycleDTO> examCycleDTOs = Collections.EMPTY_LIST;
        StringBuilder stringBuilder = new StringBuilder("select * from exam_cycle where date_part('year', start_date) = '")
                .append(searchExamCycleDTO.getStartYear()).append("' and obsolete = 0 ");
        try {
            if (searchExamCycleDTO.getCourseId() != null && !searchExamCycleDTO.getCourseId().isBlank()) {
                stringBuilder.append(" and course_id = '").append(searchExamCycleDTO.getCourseId()).append("' ");
            }
            if (searchExamCycleDTO.getEndYear() != null && searchExamCycleDTO.getEndYear() > 0) {
                stringBuilder.append("and date_part('year', end_date) <= '").append(searchExamCycleDTO.getEndYear()).append("' ");
            }
            List<ExamCycle> examCycles = jdbcTemplate.query(stringBuilder.toString(), new ResultSetExtractor<List<ExamCycle>>() {
                @Override
                public List<ExamCycle> extractData(ResultSet rs) throws SQLException, DataAccessException {
                    List<ExamCycle> examCycleList = new ArrayList<>();
                    while (rs.next()) {
                        examCycleList.add(ExamCycle.builder().examCycleName(rs.getString("exam_cycle_name")).id(rs.getLong("id")).build());
                    }
                    return examCycleList;
                }
            });
            if (examCycles != null && !examCycles.isEmpty()) {
                examCycleDTOs = examCycles.stream().map(record -> toDTO(record)).collect(Collectors.toList());
            }
            response.put(Constants.MESSAGE, Constants.SUCCESS);
            response.put(Constants.RESPONSE, examCycleDTOs);
            response.setResponseCode(HttpStatus.OK);
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            ResponseDto.setErrorResponse(response, "ERROR_IN_PROCESSING_REQUEST", "Error in Searching Exam Cycles", HttpStatus.INTERNAL_SERVER_ERROR);
            return response;
        }
    }

    private void validateSearchExamCyclePayload(SearchExamCycleDTO searchExamCycleDTO) {
        if (searchExamCycleDTO == null) {
            throw new InvalidRequestException(Constants.INVALID_REQUEST_ERROR_MESSAGE);
        }
        if (searchExamCycleDTO.getStartYear() == null || searchExamCycleDTO.getStartYear() <= 0) {
            throw new InvalidRequestException(Constants.MISSING_SEARCH_PARAM_START_ACADEMIC_YEAR);
        }
    }

    public ResponseEntity<ResponseDto> processBulkUpload(MultipartFile file, String fileType) {
        JSONArray jsonArray = null;
        ResponseDto response = new ResponseDto(Constants.API_EXAMCYCLE_BULK_UPLOAD);
        Class<ExamUploadData> dtoClass = ExamUploadData.class;
        try {
            switch (fileType.toLowerCase()) {
                case Constants.CSV:
                    jsonArray = dataImporterService.csvToJson(file, columnConfig);
                    break;
                case Constants.EXCEL:
                    jsonArray = dataImporterService.excelToJson(file);
                    break;
                default:
                    // Handle unsupported file type
                    response.put("error", "Unsupported file type");
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            List<ExamUploadData> dtoList = dataImporterService.convertCSVJsonToDtoList(jsonArray, ExamUploadData.class);
            Boolean success = dataImporterService.saveDtoListToPostgres(dtoList, examEntityRepository);

            if (success) {
                Map<String, Object> result = new HashMap<>();

                response.setResult(result);
                response.setResponseCode(HttpStatus.OK);

                return ResponseEntity.ok(response);
            } else {
                response.put("error", "File processing failed.");
                response.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
        } catch (Exception e) {
            response.put("error", "An error occurred while processing the file: " + e.getMessage());
            response.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    public List<ExamCycle> getExamCyclesByExamCycleAndCourse(Long instituteId) {
        List<Course> courses = courseRepository.findAllByInstituteId(instituteId);
        LocalDate currentDate = LocalDate.now();
        return repository.findByCourseInAndEndDateAfter(courses, currentDate);
    }
}
