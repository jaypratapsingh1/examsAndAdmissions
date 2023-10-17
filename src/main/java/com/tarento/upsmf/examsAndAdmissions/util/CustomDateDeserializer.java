package com.tarento.upsmf.examsAndAdmissions.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class CustomDateDeserializer extends JsonDeserializer<Date> {

    private static final List<SimpleDateFormat> DATE_FORMATS = Arrays.asList(
            new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy"),
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ"),
            new SimpleDateFormat("yyyy-MM-dd"),
            new SimpleDateFormat("dd.MM.yyyy")
            // Add more formats as needed
    );

    @Override
    public Date deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        String dateStr = jsonParser.getText();

        // Handle empty date strings by returning null or some default value
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }

        for (SimpleDateFormat dateFormat : DATE_FORMATS) {
            try {
                return dateFormat.parse(dateStr);
            } catch (ParseException ignored) {
            }
        }
        throw new IOException("Failed to parse date: " + dateStr + ". None of the supported date formats matched.");
    }
}
