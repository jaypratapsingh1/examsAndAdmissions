package com.tarento.upsmf.examsAndAdmissions.service;

import com.tarento.upsmf.examsAndAdmissions.model.AttendanceRecord;
import com.tarento.upsmf.examsAndAdmissions.model.ExamCycle;
import com.tarento.upsmf.examsAndAdmissions.repository.AttendanceRepository;
import com.tarento.upsmf.examsAndAdmissions.repository.ExamCycleRepository;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class AttendanceService {

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private ExamCycleRepository examCycleRepository;
    public void uploadAttendanceRecords(MultipartFile file) throws IOException {
        XSSFWorkbook workbook = new XSSFWorkbook(file.getInputStream());
        XSSFSheet sheet = workbook.getSheetAt(0);
        List<AttendanceRecord> records = new ArrayList<>();

        for (int i = 1; i < sheet.getPhysicalNumberOfRows(); i++) {
            XSSFRow row = sheet.getRow(i);

            if (isRowEmpty(row)) {
                continue;  // Skip this iteration if the row is empty
            }
            String studentEnrollmentNumber = getStringValue(row.getCell(2));
            AttendanceRecord record;

            // If record already exists, fetch it, otherwise create a new one
            if (attendanceRepository.existsByStudentEnrollmentNumber(studentEnrollmentNumber)) {
                record = attendanceRepository.findByStudentEnrollmentNumber(studentEnrollmentNumber);
            } else {
                record = new AttendanceRecord();
                record.setStudentEnrollmentNumber(studentEnrollmentNumber);
            }
            record.setFirstName(getStringValue(row.getCell(0)));
            record.setLastName(getStringValue(row.getCell(1)));
            //record.setStudentEnrollmentNumber(getStringValue(row.getCell(2)));
            record.setMothersName(getStringValue(row.getCell(3)));
            record.setFathersName(getStringValue(row.getCell(4)));
            record.setCourseName(getStringValue(row.getCell(5)));

            // Fetch the ExamCycle from the database
            String examCycleName = getStringValue(row.getCell(6));
            ExamCycle examCycle = examCycleRepository.findByExamCycleName(examCycleName);
            record.setExamCycle(examCycle);

            record.setStartDate(getDateValue(row.getCell(7)));
            record.setEndDate(getDateValue(row.getCell(8)));
            record.setTotalDays(getIntValue(row.getCell(9)));
            record.setPresent(getIntValue(row.getCell(10)));
            record.setAbsent(getIntValue(row.getCell(11)));
            //record.setAttendancePercentage(getDoubleValue(row.getCell(12)));

            records.add(record);
        }

        attendanceRepository.saveAll(records);
        workbook.close();
    }
    public static boolean isRowEmpty(XSSFRow row) {
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

    private Date getDateValue(XSSFCell cell) {
        if (cell == null) return null;

        switch (cell.getCellType()) {
            case STRING:
                try {
                    // Convert the string to a date using a simple format, adjust format as needed
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd"); // adjust if necessary
                    return sdf.parse(cell.getStringCellValue());
                } catch (ParseException e) {
                    // Log error or handle exception as necessary
                    return null;
                }
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue();
                }
                return null;
            default:
                return null;
        }
    }

    private String getStringValue(XSSFCell cell) {
        if (cell.getCellType() == CellType.STRING) {
            return cell.getStringCellValue();
        } else if (cell.getCellType() == CellType.NUMERIC) {
            return String.valueOf((int) cell.getNumericCellValue());
        }
        return "";
    }

    private int getIntValue(XSSFCell cell) {
        if (cell.getCellType() == CellType.NUMERIC) {
            return (int) cell.getNumericCellValue();
        }
        return 0;
    }

    private double getDoubleValue(XSSFCell cell) {
        if (cell.getCellType() == CellType.NUMERIC) {
            return cell.getNumericCellValue();
        }
        return 0.0;
    }
    public List<AttendanceRecord> getAllAttendanceRecords() {
        return attendanceRepository.findAll();
    }
    public AttendanceRecord getRecordById(Long id) {
        return attendanceRepository.findById(id).orElse(null);
    }

    public AttendanceRecord saveRecord(AttendanceRecord record) {
        return attendanceRepository.save(record);
    }
}
