package com.tarento.upsmf.examsAndAdmissions.controller;

import com.tarento.upsmf.examsAndAdmissions.model.ExamCycle;
import com.tarento.upsmf.examsAndAdmissions.service.CsvDataImporterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin")
public class CsvImportController {

    @Autowired
    private CsvDataImporterService csvDataImporterService;
    @Autowired
    private ExamCycle examCycle;

    @GetMapping("/importCsv")
    public String importCsvData(Model model) {
        try {

            csvDataImporterService.importCsvData(examCycle, "sample.csv");
            model.addAttribute("message", "CSV data imported successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("message", "Failed to import CSV data.");
        }
        return "import-result";
    }
}
