package com.tarento.upsmf.examsAndAdmissions.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class LocalTimeDeserializer extends JsonDeserializer<LocalTime> {
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss zzz yyyy", Locale.US);

    @Override
    public LocalTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String timeText = p.getText();
        return LocalTime.parse(timeText, TIME_FORMATTER);
    }
}