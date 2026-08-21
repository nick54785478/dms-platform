package com.dms.template;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class SwaggerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void testSwagger() {
        try {
            ResponseEntity<String> response = restTemplate.getForEntity("/v3/api-docs", String.class);
            System.out.println("STATUS: " + response.getStatusCode());
            System.out.println("BODY: " + response.getBody());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
