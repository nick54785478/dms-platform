package com.example.dms.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        // 自動尋找並註冊所有的 Jackson 模組，例如支援 Java 8 的 LocalDateTime (JavaTimeModule)
        objectMapper.findAndRegisterModules();
        // 預設將時間序列化為 ISO-8601 字串，而不是時間戳記陣列
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        return objectMapper;
    }
}
