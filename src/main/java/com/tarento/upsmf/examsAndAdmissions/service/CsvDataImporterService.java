package com.tarento.upsmf.examsAndAdmissions.service;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Service
public class CsvDataImporterService {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public JSONArray excelToJson(String filePath) throws IOException, JSONException {
        JSONArray jsonArray = new JSONArray();

        try (InputStream inputStream = new FileInputStream(filePath);
             Workbook workbook = new XSSFWorkbook(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> iterator = sheet.iterator();
            List<String> columnNames = new ArrayList<>();

            if (iterator.hasNext()) {
                Row headerRow = iterator.next();
                Iterator<Cell> cellIterator = headerRow.iterator();

                while (cellIterator.hasNext()) {
                    Cell currentCell = cellIterator.next();
                    columnNames.add(currentCell.getStringCellValue());
                }
            }

            while (iterator.hasNext()) {
                Row currentRow = iterator.next();
                JSONObject jsonObject = new JSONObject();

                Iterator<Cell> cellIterator = currentRow.iterator();
                int columnIndex = 0;

                while (cellIterator.hasNext()) {
                    Cell currentCell = cellIterator.next();

                    String columnName = columnNames.get(columnIndex);

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

                    columnIndex++;
                }

                jsonArray.put(jsonObject);
                jsonArray.put("");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return jsonArray;
    }

    public JSONArray csvToJson(String filePath) throws IOException {
        JSONArray jsonArray = new JSONArray();

        try (Reader reader = new FileReader(filePath);
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withHeader())) {

            Map<String, Integer> headerMap = csvParser.getHeaderMap();

            for (CSVRecord csvRecord : csvParser) {
                JSONObject jsonObject = getJsonObject(csvRecord, headerMap);
                jsonArray.put(jsonObject);
            }
        }
        return jsonArray;
    }

    private static JSONObject getJsonObject(CSVRecord csvRecord, Map<String, Integer> headerMap) {
        JSONObject jsonObject = new JSONObject();

        for (Map.Entry<String, Integer> entry : headerMap.entrySet()) {
            String header = entry.getKey();
            int columnIndex = entry.getValue();
            String value = csvRecord.get(columnIndex);
            try {
                jsonObject.put(header, value);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
        return jsonObject;
    }
}