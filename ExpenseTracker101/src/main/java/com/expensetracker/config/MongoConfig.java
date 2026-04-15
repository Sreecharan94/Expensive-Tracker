package com.expensetracker.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.convert.DefaultDbRefResolver;
import org.springframework.data.mongodb.core.convert.DefaultMongoTypeMapper;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;

import java.util.Arrays;

@Configuration
public class MongoConfig {

    // Custom converters for LocalDate <-> Date
    @Bean
    public MongoCustomConversions mongoCustomConversions() {
        return new MongoCustomConversions(Arrays.asList(
                new LocalDateToDateConverter(),
                new DateToLocalDateConverter()
        ));
    }

    // Removes the _class field from MongoDB documents
    @Bean
    public MongoTemplate mongoTemplate(MongoDatabaseFactory mongoDbFactory, MongoMappingContext context) {
        MappingMongoConverter converter = new MappingMongoConverter(
                new DefaultDbRefResolver(mongoDbFactory), context);
        converter.setCustomConversions(mongoCustomConversions()); // Ensure custom conversions are set
        converter.setTypeMapper(new DefaultMongoTypeMapper(null));
        converter.afterPropertiesSet(); // Important to finalize converter setup
        return new MongoTemplate(mongoDbFactory, converter);
    }
}
