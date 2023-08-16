package com.tarento.upsmf.examsAndAdmissions.controller;

import com.tarento.upsmf.examsAndAdmissions.model.ExamCycle;
import com.tarento.upsmf.examsAndAdmissions.model.dto.ExamCycleDTO;
import com.tarento.upsmf.examsAndAdmissions.service.ExamCycleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/exam-cycles")
public class ExamCycleController {

    @Autowired
    private ExamCycleService examCycleService;

    @PostMapping("admin/enrollment/exam-cycles/institute")
    public ResponseEntity<ExamCycle> createExamCycle(@RequestBody ExamCycleDTO examCycleDTO) {
        return ResponseEntity.ok(examCycleService.createExamCycle(examCycleDTO));
    }

    @GetMapping
    public ResponseEntity<List<ExamCycle>> getAllExamCycles() {
        return ResponseEntity.ok(examCycleService.getAllExamCycles());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExamCycle> getExamCycleById(@PathVariable Long id) {
        return ResponseEntity.ok(examCycleService.getExamCycleById(id));
    }
}
