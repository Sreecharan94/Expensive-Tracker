package com.expensetracker.converter;

import org.springframework.core.convert.converter.Converter;
import java.time.LocalDate;
import java.util.Date;
import java.time.ZoneId;

public class LocalDateToDateConverter implements Converter<LocalDate, Date> {
    @Override
    public Date convert(LocalDate source) {
        return Date.from(source.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }
}
