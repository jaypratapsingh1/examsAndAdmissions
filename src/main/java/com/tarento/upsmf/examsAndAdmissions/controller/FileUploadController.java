package com.tarento.upsmf.examsAndAdmissions.controller;

import com.tarento.upsmf.examsAndAdmissions.service.CsvDataImporterService;
import com.tarento.upsmf.examsAndAdmissions.util.Constants;
import org.codehaus.jettison.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@Controller
public class FileUploadController {

    @Autowired
    private CsvDataImporterService csvDataImporterService;

    @PostMapping("/fileTest")
    public ResponseEntity<Map<String, Object>> handleFileUploads(@RequestParam("file") MultipartFile file, @RequestParam("fileType") String fileType) {
        Map<String, Object> response = new HashMap<>();
        JSONArray jsonArray = null;

        try {
            switch (fileType.toLowerCase()) {
                case Constants.CSV:
                    jsonArray = csvDataImporterService.csvToJson(file);
                    break;
                case Constants.EXCEL:
                    jsonArray = csvDataImporterService.excelToJson(file);
                    break;
                default:
                    // Handle unsupported file type
                    response.put("error", "Unsupported file type");
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            response.put("data", jsonArray.toString());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", "An error occurred while processing the file");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}