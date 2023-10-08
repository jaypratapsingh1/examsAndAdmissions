package com.tarento.upsmf.examsAndAdmissions.service;

import com.tarento.upsmf.examsAndAdmissions.enums.ResultStatus;
import com.tarento.upsmf.examsAndAdmissions.enums.RetotallingStatus;
import com.tarento.upsmf.examsAndAdmissions.model.*;
import com.tarento.upsmf.examsAndAdmissions.model.dto.ExamResultDTO;
import com.tarento.upsmf.examsAndAdmissions.repository.*;
import com.tarento.upsmf.examsAndAdmissions.util.Constants;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.codehaus.jettison.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class StudentResultService {

    @Autowired
    private StudentResultRepository studentResultRepository;
    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private ExamRepository examRepository;
    @Autowired
    private RetotallingRequestRepository retotallingRequestRepository;
    @Autowired
    private DataImporterService dataImporterService;

    public ResponseDto importInternalMarksFromExcel(MultipartFile file) {
        ResponseDto response = new ResponseDto("API_IMPORT_INTERNAL_MARKS");

        try {
            // Parse the Excel file
            List<StudentResult> results = parseExcel(file);

            // For each result, update or create the record with internal marks
            for (StudentResult result : results) {
                Optional<StudentResult> existingResult = Optional.ofNullable(studentResultRepository.findByStudent_EnrollmentNumber(result.getStudent().getEnrollmentNumber()));

                if (existingResult.isPresent()) {
                    StudentResult dbResult = existingResult.get();
                    dbResult.setInternalMarksObtained(result.getInternalMarksObtained());
                    dbResult.setPracticalMarksObtained(result.getPracticalMarksObtained());
                    studentResultRepository.save(dbResult);
                } else {
                    studentResultRepository.save(result);
                }
            }

            response.put(Constants.MESSAGE, "Internal marks imported successfully.");
            response.put(Constants.RESPONSE, "Internal marks for " + results.size() + " students have been imported or updated.");
            response.setResponseCode(HttpStatus.OK);
        } catch (IOException e) {
            log.error("Failed to import internal marks from Excel", e);
            ResponseDto.setErrorResponse(response, "IMPORT_FAILED", "Failed to import internal marks from Excel.", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return response;
    }


    public ResponseDto importExternalMarksFromExcel(MultipartFile file) {
        ResponseDto response = new ResponseDto("API_IMPORT_EXTERNAL_MARKS");

        try {
            // Parse the Excel file
            List<StudentResult> results = parseExcel(file);

            // For each result, update or create the record with external marks
            for (StudentResult result : results) {
                Optional<StudentResult> existingResult = Optional.ofNullable(studentResultRepository.findByStudent_EnrollmentNumber(result.getStudent().getEnrollmentNumber()));

                if (existingResult.isPresent()) {
                    StudentResult dbResult = existingResult.get();
                    dbResult.setExternalMarksObtained(result.getExternalMarksObtained());
                    studentResultRepository.save(dbResult);
                } else {
                    studentResultRepository.save(result);
                }
            }

            response.put(Constants.MESSAGE, "External marks imported successfully.");
            response.put(Constants.RESPONSE, "External marks for " + results.size() + " students have been imported or updated.");
            response.setResponseCode(HttpStatus.OK);
        } catch (IOException e) {
            log.error("Failed to import external marks from Excel", e);
            ResponseDto.setErrorResponse(response, "IMPORT_FAILED", "Failed to import external marks from Excel.", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return response;
    }

    private List<StudentResult> parseExcel(MultipartFile file) throws IOException {
        List<StudentResult> resultList = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        try (XSSFWorkbook workbook = new XSSFWorkbook(file.getInputStream())) {
            XSSFSheet sheet = workbook.getSheetAt(0);

            Iterator<Row> rowIterator = sheet.iterator();
            if (rowIterator.hasNext()) rowIterator.next(); // skip header row

            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                if (isRowEmpty(row)) {
                    continue;
                }
                StudentResult result = new StudentResult();

                try {
                    ResponseDto studentResponse = fetchStudentByEnrollmentNumber(getStringValue(row.getCell(2)));
                    if (studentResponse.getResponseCode() != HttpStatus.OK) {
                        throw new RuntimeException("Failed to fetch student details");
                    }
                    Student studentEntity = (Student) studentResponse.get(Constants.RESPONSE);

                    ResponseDto courseResponse = fetchCourseByName(getStringValue(row.getCell(5)));
                    if (courseResponse.getResponseCode() != HttpStatus.OK) {
                        throw new RuntimeException("Failed to fetch course details");
                    }
                    Course courseEntity = (Course) courseResponse.get(Constants.RESPONSE);

                    ResponseDto examResponse = fetchExamByName(getStringValue(row.getCell(7)));
                    if (examResponse.getResponseCode() != HttpStatus.OK) {
                        throw new RuntimeException("Failed to fetch exam details");
                    }
                    Exam examEntity = (Exam) examResponse.get(Constants.RESPONSE);

                    result.setStudent(studentEntity);
                    result.setCourse(courseEntity);
                    result.setExam(examEntity);
                    result.setInternalMarks(getBigDecimalValue(row.getCell(8)));
                    result.setPassingInternalMarks(getBigDecimalValue(row.getCell(9)));
                    result.setInternalMarksObtained(getBigDecimalValue(row.getCell(10)));

                    result.setExternalMarks(getBigDecimalValue(row.getCell(11)));
                    result.setPassingExternalMarks(getBigDecimalValue(row.getCell(12)));
                    result.setExternalMarksObtained(getBigDecimalValue(row.getCell(13)));

                    result.setPracticalMarks(getBigDecimalValue(row.getCell(14)));
                    result.setPassingPracticalMarks(getBigDecimalValue(row.getCell(15)));
                    result.setPracticalMarksObtained(getBigDecimalValue(row.getCell(16)));

                    result.setOtherMarks(getBigDecimalValue(row.getCell(17)));
                    result.setPassingOtherMarks(getBigDecimalValue(row.getCell(18)));
                    result.setOtherMarksObtained(getBigDecimalValue(row.getCell(19)));

                    result.setTotalMarks(getBigDecimalValue(row.getCell(20)));
                    result.setPassingTotalMarks(getBigDecimalValue(row.getCell(21)));
                    result.setTotalMarksObtained(getBigDecimalValue(row.getCell(22)));
                    result.setGrade(getStringValue(row.getCell(23)));
                    result.setResult(getStringValue(row.getCell(24)));

                    if (validateStudentResult(result)) {
                        resultList.add(result);
                    } else {
                        errors.add("Validation failed for enrolment number: " + studentEntity.getEnrollmentNumber());
                    }
                } catch (Exception e) {
                    log.error("Error processing row in Excel file", e);
                    errors.add("Error processing row for enrolment number: " + getStringValue(row.getCell(0)));
                }
            }
        } catch (Exception e) {
            log.error("Error parsing Excel file", e);
            throw new RuntimeException("Failed to parse the Excel file", e);
        }

        if (!errors.isEmpty()) {
            errors.forEach(log::warn);
            throw new RuntimeException("Failed to process some rows in the Excel file. Check logs for more details.");
        }

        return resultList;
    }
    public static boolean isRowEmpty(Row row) {
        if (row == null) {
            return true;
        }
        for (int c = row.getFirstCellNum(); c < row.getLastCellNum(); c++) {
            Cell cell = row.getCell(c);
            if (cell != null && cell.getCellType() != CellType.BLANK && StringUtils.isNotBlank(cell.toString())) {
                return false;
            }
        }
        return true;
    }

    public ResponseDto getStudentResult(Long id) {
        ResponseDto response = new ResponseDto("API_GET_STUDENT_RESULT");

        Optional<StudentResult> resultOpt = studentResultRepository.findById(id);

        if (resultOpt.isPresent()) {
            response.put(Constants.MESSAGE, "Successful.");
            response.put(Constants.RESPONSE, resultOpt.get());
            response.setResponseCode(HttpStatus.OK);
        } else {
            ResponseDto.setErrorResponse(response, "RESULT_NOT_FOUND", "Result not found with ID: " + id, HttpStatus.NOT_FOUND);
        }

        return response;
    }

    public ResponseDto getAllStudentResults() {
        ResponseDto response = new ResponseDto("API_GET_ALL_STUDENT_RESULTS");
        List<StudentResult> results = studentResultRepository.findAll();

        if (!results.isEmpty()) {
            response.put(Constants.MESSAGE, "Successful.");
            response.put(Constants.RESPONSE, results);
            response.setResponseCode(HttpStatus.OK);
        } else {
            ResponseDto.setErrorResponse(response, "NO_RESULTS_FOUND", "No student results found.", HttpStatus.NOT_FOUND);
        }

        return response;
    }

    public ResponseDto fetchStudentByEnrollmentNumber(String enrollmentNumber) {
        ResponseDto response = new ResponseDto("API_FETCH_STUDENT_BY_ENROLLMENT_NUMBER");
        Optional<Student> studentOpt = studentRepository.findByEnrollmentNumber(enrollmentNumber);

        if (studentOpt.isPresent()) {
            response.put(Constants.MESSAGE, "Successful.");
            response.put(Constants.RESPONSE, studentOpt.get());
            response.setResponseCode(HttpStatus.OK);
        } else {
            ResponseDto.setErrorResponse(response, "STUDENT_NOT_FOUND", "Student not found with enrollment number: " + enrollmentNumber, HttpStatus.NOT_FOUND);
        }

        return response;
    }


    public ResponseDto fetchCourseByName(String courseName) {
        ResponseDto response = new ResponseDto("API_FETCH_COURSE_BY_NAME");
        Optional<Course> courseOpt = courseRepository.findByCourseName(courseName);

        if (courseOpt.isPresent()) {
            response.put(Constants.MESSAGE, "Successful.");
            response.put(Constants.RESPONSE, courseOpt.get());
            response.setResponseCode(HttpStatus.OK);
        } else {
            ResponseDto.setErrorResponse(response, "COURSE_NOT_FOUND", "Course not found with name: " + courseName, HttpStatus.NOT_FOUND);
        }

        return response;
    }

    public ResponseDto fetchExamByName(String examName) {
        ResponseDto response = new ResponseDto("API_FETCH_EXAM_BY_NAME");
        Optional<Exam> examOpt = examRepository.findByExamName(examName);

        if (examOpt.isPresent()) {
            response.put(Constants.MESSAGE, "Successful.");
            response.put(Constants.RESPONSE, examOpt.get());
            response.setResponseCode(HttpStatus.OK);
        } else {
            ResponseDto.setErrorResponse(response, "EXAM_NOT_FOUND", "Exam not found with name: " + examName, HttpStatus.NOT_FOUND);
        }

        return response;
    }

    private String getStringValue(Cell cell) {
        if (cell == null) return null;
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                return String.valueOf((int) cell.getNumericCellValue());
            default:
                return null;
        }
    }

    private BigDecimal getBigDecimalValue(Cell cell) {
        if (cell == null || cell.getCellType() != CellType.NUMERIC) return null;
        return BigDecimal.valueOf(cell.getNumericCellValue());
    }
    private boolean validateStudentResult(StudentResult result) {
        // Validate mandatory fields
        if (result.getStudent() == null || result.getCourse() == null || result.getExam() == null) {
            return false;
        }

        // Validate marks
        if (!isValidMark(result.getInternalMarks())
                || !isValidMark(result.getPassingInternalMarks())
                || !isValidMark(result.getInternalMarksObtained())
                || !isValidMark(result.getExternalMarks())
                || !isValidMark(result.getPassingExternalMarks())
                || !isValidMark(result.getExternalMarksObtained())
                || !isValidMark(result.getPracticalMarks())
                || !isValidMark(result.getPassingPracticalMarks())
                || !isValidMark(result.getPracticalMarksObtained())
                || !isValidMark(result.getOtherMarks())
                || !isValidMark(result.getPassingOtherMarks())
                || !isValidMark(result.getOtherMarksObtained())
                || !isValidMark(result.getTotalMarks())
                || !isValidMark(result.getPassingTotalMarks())
                || !isValidMark(result.getTotalMarksObtained())) {
            return false;
        }
        return true;
    }

    private boolean isValidMark(BigDecimal mark) {
        if (mark == null) {
            return true;  // Assuming marks can be null (not entered)
        }
        return mark.compareTo(BigDecimal.ZERO) >= 0 && mark.compareTo(new BigDecimal("100")) <= 0;
    }
    public ResponseDto publishResultsForCourseWithinCycle(Long courseId, Long examCycleId) {
        ResponseDto response = new ResponseDto("API_PUBLISH_RESULTS_FOR_COURSE_WITHIN_CYCLE");
        List<StudentResult> resultsForCourse = studentResultRepository.findByCourse_IdAndExam_ExamCycleIdAndPublished(courseId, examCycleId, false);

        if (!resultsForCourse.isEmpty()) {
            for (StudentResult result : resultsForCourse) {
                result.setPublished(true);
            }
            studentResultRepository.saveAll(resultsForCourse);
            response.put(Constants.MESSAGE, "Successfully published results.");
            response.setResponseCode(HttpStatus.OK);
        } else {
            ResponseDto.setErrorResponse(response, "NO_RESULTS_FOUND", "No results found for the given course and exam cycle.", HttpStatus.NOT_FOUND);
        }

        return response;
    }

    public ResponseDto findByEnrollmentNumberAndDateOfBirth(String enrollmentNumber, LocalDate dateOfBirth) {
        ResponseDto response = new ResponseDto("API_FIND_BY_ENROLLMENT_NUMBER_AND_DOB");
        Optional<StudentResult> studentResultOpt = Optional.ofNullable(studentResultRepository.findByStudent_EnrollmentNumberAndStudent_DateOfBirthAndPublished(enrollmentNumber, dateOfBirth, true));

        if (studentResultOpt.isPresent()) {
            response.put(Constants.MESSAGE, "Successful.");
            response.put(Constants.RESPONSE, studentResultOpt.get());
            response.setResponseCode(HttpStatus.OK);
        } else {
            ResponseDto.setErrorResponse(response, "RESULT_NOT_FOUND", "No result found for the given enrollment number and date of birth.", HttpStatus.NOT_FOUND);
        }

        return response;
    }
    public ResponseDto updateResultAfterRetotalling(StudentResult updatedResult) {
        ResponseDto response = new ResponseDto("API_UPDATE_RESULT_AFTER_RETOTALLING");

        if (updatedResult.getId() == null) {
            ResponseDto.setErrorResponse(response, "INVALID_RESULT_ID", "Updated result must have a valid ID", HttpStatus.BAD_REQUEST);
            return response;
        }

        Optional<StudentResult> existingResultOpt = studentResultRepository.findById(updatedResult.getId());
        if (existingResultOpt.isEmpty()) {
            ResponseDto.setErrorResponse(response, "RESULT_NOT_FOUND", "No StudentResult found with ID " + updatedResult.getId(), HttpStatus.NOT_FOUND);
            return response;
        }

        StudentResult existingResult = existingResultOpt.get();
        existingResult.setInternalMarksObtained(updatedResult.getInternalMarksObtained());
        existingResult.setExternalMarksObtained(updatedResult.getExternalMarksObtained());
        existingResult.setPracticalMarksObtained(updatedResult.getPracticalMarksObtained());
        existingResult.setOtherMarksObtained(updatedResult.getOtherMarksObtained());
        existingResult.setTotalMarksObtained(updatedResult.getTotalMarksObtained());
        existingResult.setGrade(updatedResult.getGrade());
        existingResult.setResult(updatedResult.getResult());
        existingResult.setStatus(ResultStatus.REVALUATED);

        studentResultRepository.save(existingResult);

        Optional<RetotallingRequest> retotallingRequestOpt = retotallingRequestRepository.findByStudentAndExams(existingResult.getStudent(), updatedResult.getExam());
        if (!retotallingRequestOpt.isPresent()) {
            ResponseDto.setErrorResponse(response, "RETOTALLING_REQUEST_NOT_FOUND", "No RetotallingRequest found for student and exam", HttpStatus.NOT_FOUND);
            return response;
        }

        RetotallingRequest retotallingRequest = retotallingRequestOpt.get();
        retotallingRequest.setStatus(RetotallingStatus.COMPLETED);
        retotallingRequestRepository.save(retotallingRequest);

        response.put(Constants.MESSAGE, "Successfully updated result after retotalling.");
        response.setResponseCode(HttpStatus.OK);
        return response;
    }

    public ResponseDto getResultsByExamCycleAndExamGroupedByInstitute(ExamCycle examCycle, Exam exam) {
        ResponseDto response = new ResponseDto("API_GET_RESULTS_BY_EXAM_CYCLE_AND_EXAM_GROUPED_BY_INSTITUTE");
        List<StudentResult> results;
        if (examCycle != null && exam != null) {
            results = studentResultRepository.findByExamCycleAndExam(examCycle, exam);
        } else if (examCycle != null) {
            results = studentResultRepository.findByExamCycle(examCycle);
        } else if (exam != null) {
            results = studentResultRepository.findByExam(exam);
        } else {
            results = studentResultRepository.findAll();
        }

        if (results.isEmpty()) {
            ResponseDto.setErrorResponse(response, "NO_RESULTS_FOUND", "No results found for the given criteria.", HttpStatus.NOT_FOUND);
            return response;
        }

        Map<Institute, List<StudentResult>> groupedResults = results.stream()
                .collect(Collectors.groupingBy(result -> result.getStudent().getInstitute()));

        response.put(Constants.MESSAGE, "Successful.");
        response.put(Constants.RESPONSE, groupedResults);
        response.setResponseCode(HttpStatus.OK);
        return response;
    }


    private ExamResultDTO convertToDTO(StudentResult result) {
        ExamResultDTO dto = new ExamResultDTO();

        dto.setInstituteName(result.getStudent().getInstitute().getInstituteName());
        dto.setInstituteId(result.getStudent().getInstitute().getId());
        dto.setStudentName(result.getFirstName() + " " + result.getLastName());
        dto.setCourseName(result.getCourse().getCourseName());
        dto.setExamName(result.getExam().getExamName());
        dto.setInternalMarks(result.getInternalMarksObtained());

        return dto;
    }
    public ResponseDto processBulkResultUpload(MultipartFile file, String fileType) {
        ResponseDto response = new ResponseDto("API_BULK_UPLOAD_RESULTS");

        try {
            JSONArray jsonArray;

            switch (fileType.toLowerCase()) {
                case Constants.CSV:
                    jsonArray = dataImporterService.csvToJson(file);
                    break;
                case Constants.EXCEL:
                    jsonArray = dataImporterService.excelToJson(file);
                    break;
                default:
                    // Handle unsupported file type
                    return ResponseDto.setErrorResponse(response, "UNSUPPORTED_FILE_TYPE", "Unsupported file type", HttpStatus.BAD_REQUEST);
            }

            List<StudentResult> dtoList = dataImporterService.convertJsonToDtoList(jsonArray, StudentResult.class);
            Boolean success = dataImporterService.saveDtoListToPostgres(dtoList, studentResultRepository);

            if (success) {
                response.put(Constants.MESSAGE, "File processed successfully.");
                response.setResponseCode(HttpStatus.OK);
            } else {
                return ResponseDto.setErrorResponse(response, "FILE_PROCESSING_FAILED", "File processing failed.", HttpStatus.INTERNAL_SERVER_ERROR);
            }

        } catch (Exception e) {
            log.error("Error processing bulk result upload", e);
            return ResponseDto.setErrorResponse(response, "INTERNAL_ERROR", "An unexpected error occurred.", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return response;
    }
}