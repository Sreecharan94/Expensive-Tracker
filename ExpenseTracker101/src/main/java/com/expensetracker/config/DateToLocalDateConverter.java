package com.expensetracker.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

import java.time.LocalDate;
import java.util.Date;

@ReadingConverter
public class DateToLocalDateConverter implements Converter<Date, LocalDate> {
    @Override
    public LocalDate convert(Date source) {
        return source == null ? null : source.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
    }
}