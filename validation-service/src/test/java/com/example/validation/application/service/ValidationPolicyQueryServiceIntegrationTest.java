package com.example.validation.application.service;

import com.example.validation.application.shared.dto.ValidationPolicySearchedResult;
import com.example.validation.application.shared.query.ListValidationPolicyQuery;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb;MODE=MySQL;DB_CLOSE_DELAY=-1",
        "spring.jpa.hibernate.ddl-auto=none"
})
@Sql(scripts = "file:init-schema.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(statements = "DROP TABLE IF EXISTS `validation_policy`;", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class ValidationPolicyQueryServiceIntegrationTest {

    @Autowired
    private ValidationPolicyQueryService queryService;

    @Test
    public void testListValidationPolicy_ByTemplateName() {
        // Arrange
        ListValidationPolicyQuery query = new ListValidationPolicyQuery("DEPT_PROFILE");

        // Act
        List<ValidationPolicySearchedResult> results = queryService.listValidationPolicy(query);

        // Assert
        assertThat(results).isNotNull();
        assertThat(results).hasSize(8); // DEPT_PROFILE 有 8 筆

        // Verify some data mappings
        ValidationPolicySearchedResult firstResult = results.stream()
                .filter(r -> r.getId() == 1L)
                .findFirst()
                .orElseThrow();
        
        assertThat(firstResult.getTemplateName()).isEqualTo("DEPT_PROFILE");
        assertThat(firstResult.getTemplateSheetName()).isEqualTo("MIS");
        assertThat(firstResult.getMappingFieldName()).isEqualTo("NAME");
        assertThat(firstResult.getRule()).isEqualTo("ENFORCE_ROW_VALIDATION");
        assertThat(firstResult.getPriorityNo()).isEqualTo(1);
    }

    @Test
    public void testListValidationPolicy_All() {
        // Arrange
        ListValidationPolicyQuery query = new ListValidationPolicyQuery(null);

        // Act
        List<ValidationPolicySearchedResult> results = queryService.listValidationPolicy(query);

        // Assert
        assertThat(results).isNotNull();
        assertThat(results).hasSize(15); // 全部共有 15 筆資料
    }
}
