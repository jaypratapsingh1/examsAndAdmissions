package com.tarento.upsmf.examsAndAdmissions.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.tarento.upsmf.examsAndAdmissions.service.CsvDataImporterService;
import org.apache.commons.csv.CSVRecord;
import org.codehaus.jettison.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class FileUploadController {

    @Autowired
    private CsvDataImporterService csvDataImporterService;

    String csvFilePath = "/home/radhesh/Downloads/artifact_bug_igot-kb.csv";
    String excelFilePath = "/home/radhesh/Desktop/examsAndAdmissions/src/main/resources/Template_Examcycle_Creation.xlsx";

    JSONArray jsonArray = null;

    @PostMapping("/fileTest")
    public ResponseEntity<Map<String, Object>> handleFileUploads(@RequestParam("file") MultipartFile file, @RequestParam("fileType") String fileType) {
        Map<String, Object> response = new HashMap<>();

        try {
            if ("csv".equalsIgnoreCase(fileType)) {
                // Call the CSV processing method
                jsonArray = csvDataImporterService.csvToJson(csvFilePath);

                response.put("data", jsonArray.toString());
                return ResponseEntity.ok(response);
            } else if ("excel".equalsIgnoreCase(fileType)) {
                // Call the Excel processing method
                jsonArray = csvDataImporterService.excelToJson(excelFilePath);

                response.put("data", jsonArray.toString());
                return ResponseEntity.ok(response);
            } else {
                // Handle unsupported file type
                response.put("error", "Unsupported file type");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
        } catch (Exception e) {
            response.put("error", "An error occurred while processing the file");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}