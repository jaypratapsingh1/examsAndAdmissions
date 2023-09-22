package com.tarento.upsmf.examsAndAdmissions.controller;

import com.tarento.upsmf.examsAndAdmissions.model.Exam;
import com.tarento.upsmf.examsAndAdmissions.model.ExamCycle;
import com.tarento.upsmf.examsAndAdmissions.model.ExamUploadData;
import com.tarento.upsmf.examsAndAdmissions.repository.ExamEntityRepository;
import com.tarento.upsmf.examsAndAdmissions.service.DataImporterService;
import com.tarento.upsmf.examsAndAdmissions.service.ExamCycleService;
import com.tarento.upsmf.examsAndAdmissions.util.Constants;
import org.codehaus.jettison.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import com.tarento.upsmf.examsAndAdmissions.util.Constants;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.tarento.upsmf.examsAndAdmissions.model.dto.*;
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


    @PostMapping("/create")
    public ResponseEntity<?> createExamCycle(@RequestBody ExamCycle examCycle, @RequestAttribute(Constants.Parameters.USER_ID) String userId) {
        try {
            ExamCycle createdExamCycle = service.createExamCycle(examCycle,userId);
            return new ResponseEntity<>(createdExamCycle, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to create ExamCycle.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @GetMapping("/list")
    public ResponseEntity<?> getAllExamCycles() {
        try {
            List<ExamCycle> examCycles = service.getAllExamCycles();
            if (examCycles.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }
            return new ResponseEntity<>(examCycles, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to fetch all ExamCycles.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getExamCycleById(@PathVariable Long id) {
        try {
            ExamCycle examCycle = service.getExamCycleById(id);
            if (examCycle == null) {
                return new ResponseEntity<>("ExamCycle not found with ID: " + id, HttpStatus.NOT_FOUND);
            }
            return new ResponseEntity<>(examCycle, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to fetch ExamCycle with ID: " + id, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateExamCycle(@PathVariable Long id, @RequestBody ExamCycle examCycle, @RequestAttribute(Constants.Parameters.USER_ID) String userId) {
        try {
            ExamCycle updatedExamCycle = service.updateExamCycle(id, examCycle, userId);
            if (updatedExamCycle == null) {
                return new ResponseEntity<>("ExamCycle not found with ID: " + id, HttpStatus.NOT_FOUND);
            }
            return new ResponseEntity<>(updatedExamCycle, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to update ExamCycle with ID: " + id, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteExamCycle(@PathVariable Long id) {
        try {
            service.deleteExamCycle(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to delete ExamCycle with ID: " + id, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    // Restore a soft-deleted exam
    @PutMapping("/{id}/restore")
    public ResponseEntity<?> restoreExamCycle(@PathVariable Long id) {
        try {
            service.restoreExamCycle(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to restore ExamCycle with ID: " + id, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/{id}/addExam")
    public ResponseEntity<?> addExamToCycle(@PathVariable Long id, @RequestBody List<Exam> exams, @RequestAttribute(Constants.Parameters.USER_ID) String userId) {
        try {
            ExamCycle examCycle = service.addExamsToCycle(id, exams, userId);
            if (examCycle == null) {
                return new ResponseEntity<>("ExamCycle not found with ID: " + id, HttpStatus.NOT_FOUND);
            }
            return new ResponseEntity<>(exams, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to add exam to ExamCycle with ID: " + id, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @DeleteMapping("/{id}/removeExam")
    public ResponseEntity<?> removeExamFromCycle(@PathVariable Long id, @RequestBody Exam exam) {
        try {
            ExamCycle examCycle = service.removeExamFromCycle(id, exam);
            if (examCycle == null) {
                return new ResponseEntity<>("ExamCycle not found with ID: " + id, HttpStatus.NOT_FOUND);
            }
            return new ResponseEntity<>(exam, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to remove exam from ExamCycle with ID: " + id, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @PutMapping("/{id}/publish")
    public ResponseEntity<?> publishExamCycle(@PathVariable Long id) {
        try {
            ExamCycle publishedExamCycle = service.publishExamCycle(id);
            if (publishedExamCycle == null) {
                return new ResponseEntity<>("ExamCycle not found with ID: " + id, HttpStatus.NOT_FOUND);
            }
            return new ResponseEntity<>(publishedExamCycle, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to publish ExamCycle with ID: " + id, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @PostMapping("/createExamCycleWithExams")
    public ResponseEntity<?> createExamCycleWithExams(@RequestBody ExamCycleWithExamsDTO examCycleWithExamsDTO, @RequestAttribute(Constants.Parameters.USER_ID) String userId) {
        try {
            // Create the ExamCycle
            ExamCycle createdExamCycle = service.createExamCycle(examCycleWithExamsDTO.getExamCycle(), userId);

            // Add Exams to the ExamCycle
            service.addExamsToCycle(createdExamCycle.getId(), examCycleWithExamsDTO.getExams(), userId);

            return ResponseEntity.ok("ExamCycle with Exams created successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to create ExamCycle with Exams.");
    @PostMapping("/bulkUpload")
    public ResponseEntity<Map<String, Object>> processBulkExamUploads(@RequestParam("file") MultipartFile file, @RequestParam("fileType") String fileType) {
        Map<String, Object> response = new HashMap<>();
        JSONArray jsonArray = null;
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
                response.put("message", "File processed successfully.");
                return ResponseEntity.ok(response);
            } else {
                response.put("error", "File processing failed.");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
        } catch (Exception e) {
            response.put("error", "An error occurred while processing the file: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}