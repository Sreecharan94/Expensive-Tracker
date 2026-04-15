package com.expensetracker.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

@WritingConverter
public class LocalDateToDateConverter implements Converter<LocalDate, Date> {

    @Override
    public Date convert(LocalDate source) {
        if (source == null) {
            return null;
        }
        return Date.from(source.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }
}
