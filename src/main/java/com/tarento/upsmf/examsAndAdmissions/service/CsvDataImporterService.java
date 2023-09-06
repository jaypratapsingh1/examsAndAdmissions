package com.tarento.upsmf.examsAndAdmissions.service;

import com.tarento.upsmf.examsAndAdmissions.model.ExamCycle;
import com.tarento.upsmf.examsAndAdmissions.repository.ExamCycleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ResourceUtils;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Field;

@Service
public class CsvDataImporterService {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private ExamCycleRepository examCycleRepository;


// ...

    @Transactional
    public void importCsvData(ExamCycle examCycle, String fileName) throws Exception {
        File csvFile = ResourceUtils.getFile("classpath:" + fileName);
        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            String headerLine = br.readLine(); // Read the header line
            String[] headers = headerLine.split(","); // Split the headers
            String line;
            while ((line = br.readLine()) != null) {
                String[] fields = line.split(",");

                // Create a new entity
                ExamCycle entity = new ExamCycle();

                // Iterate over the headers and set entity properties dynamically
                for (int i = 0; i < headers.length; i++) {
                    String header = headers[i];
                    String fieldValue = fields[i];
                    setEntityProperty(entity, header, fieldValue);
                }

                // Save the entity to the database with the specified table name
                entityManager.persist(entity);
            }
        }
    }

    private void setEntityProperty(ExamCycle examCycle, String header, String fieldValue) throws Exception {
        Field field = ExamCycle.class.getDeclaredField(header);
        field.setAccessible(true);

        // Determine the data type of the field and set its value accordingly
        if (field.getType().equals(String.class)) {
            field.set(examCycle, fieldValue);
        } else if (field.getType().equals(Integer.class)) {
            field.set(examCycle, Integer.parseInt(fieldValue));
        } else if (field.getType().equals(Double.class)) {
            field.set(examCycle, Double.parseDouble(fieldValue));
        }
        field.setAccessible(false);
    }

}
