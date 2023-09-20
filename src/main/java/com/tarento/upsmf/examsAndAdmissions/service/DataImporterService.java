package com.tarento.upsmf.examsAndAdmissions.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tarento.upsmf.examsAndAdmissions.model.ExamUploadData;
import com.tarento.upsmf.examsAndAdmissions.repository.ExamCycleRepository;
import com.tarento.upsmf.examsAndAdmissions.repository.ExamEntityRepository;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

@Service
public class DataImporterService {
    @Autowired
    private ObjectMapper objectMapper;


    public JSONArray excelToJson(MultipartFile excelFile) throws IOException, JSONException {
        try (InputStream inputStream = excelFile.getInputStream();
             Workbook workbook = new XSSFWorkbook(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0);
            List<String> columnNames = StreamSupport.stream(sheet.getRow(0).spliterator(), false)
                    .map(Cell::getStringCellValue)
                    .collect(Collectors.toList());

            JSONArray jsonArray = StreamSupport.stream(sheet.spliterator(), false)
                    .skip(1) // Skip the header row
                    .map(row -> {
                        JSONObject jsonObject = new JSONObject();
                        IntStream.range(0, row.getLastCellNum())
                                .forEach(columnIndex -> {
                                    Cell currentCell = row.getCell(columnIndex);
                                    String columnName = columnNames.get(columnIndex);

                                    try {
                                        switch (currentCell.getCellType()) {
                                            case STRING:
                                                jsonObject.put(columnName, currentCell.getStringCellValue());
                                                break;
                                            case NUMERIC:
                                                if (DateUtil.isCellDateFormatted(currentCell)) {
                                                    jsonObject.put(columnName, currentCell.getDateCellValue());
                                                } else {
                                                    jsonObject.put(columnName, currentCell.getNumericCellValue());
                                                }
                                                break;
                                            case BOOLEAN:
                                                jsonObject.put(columnName, currentCell.getBooleanCellValue());
                                                break;
                                            case BLANK:
                                                jsonObject.put(columnName, "");
                                                break;
                                            default:
                                                jsonObject.put(columnName, "");
                                        }
                                    } catch (JSONException e) {
                                        // Handle JSONException if needed
                                    }
                                });
                        return jsonObject;
                    })
                    .collect(Collectors.collectingAndThen(Collectors.toList(), JSONArray::new));

            return jsonArray;
        }
    }
    public JSONArray csvToJson(MultipartFile csvFile) throws IOException {
        try (Reader reader = new InputStreamReader(csvFile.getInputStream());
             CSVParser csvParser = CSVFormat.DEFAULT.withHeader().parse(reader)) {

            List<Map<String, String>> records = csvParser.getRecords().stream()
                    .map(CSVRecord::toMap)
                    .collect(Collectors.toList());
            try {
                return new JSONArray(objectMapper.writeValueAsString(records));
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public <T> List<T> convertJsonToDtoList(JSONArray jsonArray, Class<T> dtoClass) {
        try {
            System.out.println("JSON data before deserialization: " + jsonArray.toString());
            List<T> dtoList = objectMapper.readValue(jsonArray.toString(), objectMapper.getTypeFactory().constructCollectionType(List.class, dtoClass));
            System.out.println("DTOs after deserialization: " + dtoList);
            return dtoList;
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert JSON to DTO list: " + e.getMessage(), e);
        }
    }

    public Boolean saveDtoListToPostgres(List<ExamUploadData> dtoList, ExamEntityRepository repository) {
        try {
            List<ExamUploadData> entityList = convertDtoListToEntities(dtoList);
            repository.saveAll(entityList);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    private List<ExamUploadData> convertDtoListToEntities(List<ExamUploadData> dtoList) {
        List<ExamUploadData> entityList = new ArrayList<>();

        for (ExamUploadData dto : dtoList) {
            ExamUploadData entity = new ExamUploadData();

            // Map ExamUploadData fields to ExamEntity fields
            entity.setExamcycleName(dto.getExamcycleName());
            entity.setCourse(dto.getCourse());
            entity.setStartDate(dto.getStartDate());
            entity.setEndDate(dto.getEndDate());
            entity.setExamName(dto.getExamName());
            entity.setDate(dto.getDate());
            entity.setStartTime(dto.getStartTime());
            entity.setEndTime(dto.getEndTime());
            entity.setMaximumMarks(dto.getMaximumMarks());

            entityList.add(entity);
        }

        return entityList;
    }

}