package com.tarento.upsmf.examsAndAdmissions.controller;

import com.tarento.upsmf.examsAndAdmissions.exception.ServiceException;
import com.tarento.upsmf.examsAndAdmissions.model.ExamCycle;
import com.tarento.upsmf.examsAndAdmissions.model.dto.ExamCycleDTO;
import com.tarento.upsmf.examsAndAdmissions.service.ExamCycleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/examCycle")
public class ExamCycleController {

    @Autowired
    private ExamCycleService examCycleService;

    @PostMapping("/create")
    public ResponseEntity<ExamCycle> createExamCycle(@RequestBody ExamCycleDTO examCycleDTO) {
        try {
            ExamCycle createdExamCycle = examCycleService.createExamCycle(examCycleDTO);
            return new ResponseEntity<>(createdExamCycle, HttpStatus.CREATED);
        } catch (Exception e) {
            throw new ServiceException("Failed to create ExamCycle.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @GetMapping("/list")
    public ResponseEntity<List<ExamCycle>> getAllExamCycles() {
        try {
            List<ExamCycle> examCycles = examCycleService.getAllExamCycles();
            if (examCycles.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }
            return new ResponseEntity<>(examCycles, HttpStatus.OK);
        } catch (Exception e) {
            throw new ServiceException("Failed to fetch all ExamCycles.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExamCycle> getExamCycleById(@PathVariable Long id) {
        try {
            ExamCycle examCycle = examCycleService.getExamCycleById(id);
            if (examCycle == null) {
                throw new ServiceException("ExamCycle not found with ID: " + id, HttpStatus.NOT_FOUND);
            }
            return new ResponseEntity<>(examCycle, HttpStatus.OK);
        } catch (ServiceException se) {
            throw se;
        } catch (Exception e) {
            throw new ServiceException("Failed to fetch ExamCycle with ID: " + id, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteExamCycle(@PathVariable Long id) {
        try {
            examCycleService.deleteExamCycleById(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            throw new ServiceException("Failed to delete ExamCycle with ID: " + id, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<ExamCycle> updateExamCycle(@PathVariable Long id, @RequestBody ExamCycleDTO examCycleDTO) {
        try {
            ExamCycle updatedExamCycle = examCycleService.updateExamCycle(id, examCycleDTO);
            if (updatedExamCycle == null) {
                throw new ServiceException("ExamCycle not found with ID: " + id, HttpStatus.NOT_FOUND);
            }
            return new ResponseEntity<>(updatedExamCycle, HttpStatus.OK);
        } catch (ServiceException se) {
            throw se;
        } catch (Exception e) {
            throw new ServiceException("Failed to update ExamCycle with ID: " + id, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}