package com.tarento.upsmf.examsAndAdmissions.controller;

import com.tarento.upsmf.examsAndAdmissions.model.Exam;
import com.tarento.upsmf.examsAndAdmissions.model.ExamCycle;
import com.tarento.upsmf.examsAndAdmissions.model.ExamUploadData;
import com.tarento.upsmf.examsAndAdmissions.model.ResponseDto;
import com.tarento.upsmf.examsAndAdmissions.model.dto.ExamCycleWithExamsDTO;

import com.tarento.upsmf.examsAndAdmissions.model.dto.SearchExamCycleDTO;
import com.tarento.upsmf.examsAndAdmissions.repository.CourseRepository;
import com.tarento.upsmf.examsAndAdmissions.repository.ExamEntityRepository;
import com.tarento.upsmf.examsAndAdmissions.service.DataImporterService;
import com.tarento.upsmf.examsAndAdmissions.service.ExamCycleService;
import com.tarento.upsmf.examsAndAdmissions.util.Constants;
import org.codehaus.jettison.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/examCycle")
public class ExamCycleController {

    @Autowired
    private ExamCycleService service;

    @Autowired
    private DataImporterService dataImporterService;

    @Autowired
    ExamEntityRepository repository;

    @Autowired
    private CourseRepository courseRepository;

    @PostMapping("/create")
    public ResponseEntity<ResponseDto> createExamCycle(@RequestBody ExamCycle examCycle, @RequestAttribute(Constants.Parameters.USER_ID) String userId) {
        ResponseDto response = service.createExamCycle(examCycle,userId);
        return new ResponseEntity<>(response, response.getResponseCode());
    }

    @GetMapping("/list")
    public ResponseEntity<ResponseDto> getAllExamCycles() {
        ResponseDto response = service.getAllExamCycles();
        return new ResponseEntity<>(response, response.getResponseCode());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseDto> getExamCycleById(@PathVariable Long id) {
        ResponseDto response = service.getExamCycleById(id);
        return new ResponseEntity<>(response, response.getResponseCode());
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<ResponseDto> updateExamCycle(@PathVariable Long id, @RequestBody ExamCycle examCycle, @RequestAttribute(Constants.Parameters.USER_ID) String userId) {
        ResponseDto response = service.updateExamCycle(id, examCycle, userId);
        return new ResponseEntity<>(response, response.getResponseCode());
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ResponseDto> deleteExamCycle(@PathVariable Long id) {
        ResponseDto response = service.deleteExamCycle(id);
        return new ResponseEntity<>(response, response.getResponseCode());
    }

    @PutMapping("/{id}/restore")
    public ResponseEntity<ResponseDto> restoreExamCycle(@PathVariable Long id) {
        ResponseDto response = service.restoreExamCycle(id);
        return new ResponseEntity<>(response, response.getResponseCode());
    }

    @PostMapping("/{id}/addExam")
    public ResponseEntity<ResponseDto> addExamToCycle(@PathVariable Long id, @RequestBody List<Exam> exams, @RequestAttribute(Constants.Parameters.USER_ID) String userId) {
        ResponseDto response = service.addExamsToCycle(id, exams, userId);
        return new ResponseEntity<>(response, response.getResponseCode());
    }

    @DeleteMapping("/{id}/removeExam")
    public ResponseEntity<ResponseDto> removeExamFromCycle(@PathVariable Long id, @RequestBody Exam exam) {
        ResponseDto response = service.removeExamFromCycle(id, exam);
        return new ResponseEntity<>(response, response.getResponseCode());
    }

    @PutMapping("/{id}/publish")
    public ResponseEntity<ResponseDto> publishExamCycle(@PathVariable Long id) {
        ResponseDto response = service.publishExamCycle(id);
        return new ResponseEntity<>(response, response.getResponseCode());
    }

    @PostMapping("/createExamCycleWithExams")
    public ResponseEntity<ResponseDto> createExamCycleWithExams(@RequestBody ExamCycleWithExamsDTO examCycleWithExamsDTO, @RequestAttribute(Constants.Parameters.USER_ID) String userId) {
        ResponseDto response = service.createExamCycle(examCycleWithExamsDTO.getExamCycle(),userId);
        return new ResponseEntity<>(response, response.getResponseCode());
    }

    @PostMapping("/bulkUpload")
    public ResponseEntity<?> processBulkExamUploads(@RequestParam("file") MultipartFile file, @RequestParam("fileType") String fileType) {
        JSONArray jsonArray = null;
        ResponseDto response = new ResponseDto(Constants.API_EXAMCYCLE_BULK_UPLOAD);
        Class<ExamUploadData> dtoClass = ExamUploadData.class;
        try {
            switch (fileType.toLowerCase()) {
                case Constants.CSV:
                    jsonArray = dataImporterService.csvToJson(file);
                    break;
                case Constants.EXCEL:
                    jsonArray = dataImporterService.excelToJson(file);
                    break;
                default:
                    // Handle unsupported file type
                    response.put("error", "Unsupported file type");
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            List<ExamUploadData> dtoList = dataImporterService.convertJsonToDtoList(jsonArray, ExamUploadData.class);
            Boolean success = dataImporterService.saveDtoListToPostgres(dtoList, repository);

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
    @PutMapping("/{id}/updateExams")
    public ResponseEntity<ResponseDto> updateExamsForCycle(@PathVariable Long id,
                                                           @RequestBody List<Exam> updatedExams) {
        ResponseDto response = service.updateExamsForCycle(id, updatedExams);
        return new ResponseEntity<>(response, response.getResponseCode());
    }

    @PostMapping("/search")
    public ResponseEntity<ResponseDto> searchExamCycleByCourseIdAndYear(@RequestBody SearchExamCycleDTO searchExamCycleDTO) {
        ResponseDto response = service.searchExamCycle(searchExamCycleDTO);
        return new ResponseEntity<>(response, response.getResponseCode());
    }


}