package com.dms.template.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI (Swagger) 配置
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI templateServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Template Service API")
                        .description("文件管理系統 (DMS) 範本服務")
                        .version("v1.0.0"));
    }
}
